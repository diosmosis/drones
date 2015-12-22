package com.flarestar.drones.views.aspect;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.view.*;
import android.widget.OverScroller;
import com.flarestar.drones.views.aspect.scrolling.DragDetector;
import com.flarestar.drones.views.aspect.scrolling.DragVector;
import com.flarestar.drones.views.aspect.scrolling.EdgeEffects;
import com.flarestar.drones.views.viewgroups.BoxModelNode;

/**
 * TODO: unimplemented features:
 * - page scrolling (page up/down)
 * - key scrolling (arrow down/up/left/right)
 * - home/end scrolling (scroll to top/bottom)
 * - accessibility operations
 * - programattic flinging
 * - "smooth" scrolling (can probably made more generic than what is in ScrollView)
 *
 * TODO: note somewhere that part of the philosophy of the views drone is that some properties are assumed to be
 *       immutable after construction (ie, are set while building a view, then ignored afterwards), like vertical/horizontal
 *       scrolling enabled/disabled.
 *     - another assumption: no viewgroup will ever have 0 children (let's add a sanity check for this)
 *
 * TODO: profiling. important since i'm removing all the micro-optimizations in favor of clean code
 */
public class ScrollingAspect extends ViewAspect {

    public static final int INVALID_POINTER = -1;

    private EdgeEffects edgeEffects;
    private DragDetector dragDetector;

    public int overscrollDistance;
    private int overflingDistance;

    public OverScroller scroller;

    private BoxModelNode view;

    private int scrollRangeX;
    private int scrollRangeY;

    public ScrollingAspect(BoxModelNode view) {
        this.view = view;

        final Context context = view.getContext();

        scroller = new OverScroller(context);

        final ViewConfiguration configuration = ViewConfiguration.get(context);
        final int touchSlop = configuration.getScaledTouchSlop();
        final int minimumVelocityForFling = configuration.getScaledMinimumFlingVelocity();
        final int maximumVelocityForFling = configuration.getScaledMaximumFlingVelocity();
        overscrollDistance = configuration.getScaledOverscrollDistance();
        overflingDistance = configuration.getScaledOverflingDistance();

        DragVector dragVector = new DragVector(touchSlop, view.isHorizontalScrollBarEnabled(), view.isVerticalScrollBarEnabled());
        dragDetector = new DragDetector(dragVector, minimumVelocityForFling, maximumVelocityForFling);
        edgeEffects = new EdgeEffects(view, view.getOverScrollMode(), view.isHorizontalScrollBarEnabled(),
            view.isVerticalScrollBarEnabled());

        view.setFocusable(true);
        view.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
        view.setWillNotDraw(false);
    }

    /**
     * Update the computed scroll range after a measure pass has been done on the view.
     */
    @Override
    public void onLayoutStarted() {
        if (view.isVerticalScrollBarEnabled()) {
            scrollRangeY = Math.max(0, view.getAggregateChildHeight() - (view.getHeight() - view.getPaddingBottom() - view.getPaddingTop()));
        } else {
            scrollRangeY = 0;
        }

        if (view.isHorizontalScrollBarEnabled()) {
            scrollRangeX = Math.max(0, view.getAggregateChildWidth() - (view.getWidth() - view.getPaddingLeft() - view.getPaddingRight()));
        } else {
            scrollRangeX = 0;
        }

        int overscrollMode = view.getOverScrollMode();

        boolean canOverscroll = overscrollMode == View.OVER_SCROLL_ALWAYS
            || (overscrollMode == View.OVER_SCROLL_IF_CONTENT_SCROLLS && (scrollRangeY > 0 || scrollRangeX > 0));
        if (canOverscroll) {
            edgeEffects.enable();
        } else {
            edgeEffects.disable();
        }
    }

    // TODO: ok if we only allow scrolling behavior on viewgroups? should change this later.

    /**
     * Used to intercept events that are meant for child views.
     *
     * Used to check if there is the user is dragging within the scrollable view. If yes,
     * the events are intercepted (by returning true). In this case, future events are
     * sent to the scrollable View's onTouchEvent. When onTouchEvent returns false,
     * the intercepting stops.
     *
     * This method has to handle the event given, it will not be forwarded to onTouchEvent
     * (even if true is returned). Future events may be forwarded, but the given event is not.
     *
     * @param event
     * @return
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        handleTouchEvent(event, true);
        return dragDetector.isDragging();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        handleTouchEvent(event, false);
        handleDragStateChange(event);
        return true;
    }

    private void handleTouchEvent(MotionEvent event, boolean isIntercepted) {
        dragDetector.handleTouchEvent(event, isIntercepted, isFlinging());

        if (dragDetector.isDragStarted()) {
            if (isFlinging()) {
                abortFling();
            }

            final ViewParent parent = view.getParent();
            if (parent != null) {
                parent.requestDisallowInterceptTouchEvent(true);
            }
        } else if (dragDetector.isDragEnded()) {
            if (scroller.springBack(view.getScrollX(), view.getScrollY(), 0, scrollRangeX, 0, scrollRangeY)) {
                view.postInvalidateOnAnimation();
            }

            edgeEffects.release();

            if (dragDetector.isFlungVertical()) {
                startVerticalFling(dragDetector.getInitialVelocityY());
            } else if (dragDetector.isFlungHorizontal()) {
                startHorizontalFling(dragDetector.getInitialVelocityX());
            }
        }
    }

    private void startVerticalFling(int velocityY) {
        int height = view.getHeight() - view.getPaddingBottom() - view.getPaddingTop();
        int bottom = view.getAggregateChildHeight();

        scroller.fling(view.getScrollX(), view.getScrollY(), 0, velocityY, 0, 0, 0, Math.max(0, bottom - height), 0,
            height/2);

        view.postInvalidateOnAnimation();
    }

    private void startHorizontalFling(int velocityX) {
        int width = view.getWidth() - view.getPaddingLeft() - view.getPaddingRight();
        int right = view.getAggregateChildWidth();

        scroller.fling(view.getScrollX(), view.getScrollY(), velocityX, 0, 0, Math.max(0, right - width), 0, 0,
            width/2, 0);

        view.postInvalidateOnAnimation();
    }

    private void handleDragStateChange(MotionEvent event) {
        if (!dragDetector.isDragging()) {
            return;
        }

        final int scrollDx = dragDetector.getScrolledByX();
        final int scrollDy = dragDetector.getScrolledByY();

        if (scrollDx == 0 && scrollDy == 0) {
            return;
        }

        final int oldX = view.getScrollX();
        final int oldY = view.getScrollY();

        boolean overScrolled = scrollBy(scrollDx, scrollDy, view.getScrollX(), view.getScrollY(),
            scrollRangeX, scrollRangeY, overscrollDistance, overscrollDistance, true);
        if (overScrolled) {
            // Break our velocity if we hit a scroll barrier.
            dragDetector.resetVelocityTracker();
        }

        view.onScrollChanged(view.getScrollX(), view.getScrollY(), oldX, oldY);

        edgeEffects.onDrag(oldX, oldY, scrollDx, scrollDy, scrollRangeX, scrollRangeY);
    }

    private void abortFling() {
        scroller.abortAnimation();
    }

    private boolean isFlinging() {
        return !scroller.isFinished();
    }

    @Override
    public void checkScrollPositionDuringDraw() {
        if (!scroller.computeScrollOffset()) {
            return;
        }

        final int oldX = view.getScrollX();
        final int oldY = view.getScrollY();
        final int x = scroller.getCurrX();
        final int y = scroller.getCurrY();

        if (oldX != x || oldY != y) {
            scrollBy(x - oldX, y - oldY, oldX, oldY, scrollRangeX, scrollRangeY, 0, overflingDistance, false);

            // TODO: (this is more of a note) removing this since in drones we don't care about View subclasses. there should be none. instead, we'll post a scope event when the scroll changes
            // onScrollChanged(getScrollX(), getScrollY(), oldX, oldY

            edgeEffects.onScroll(oldX, oldY, x, y, scrollRangeX, scrollRangeY, scroller);
        }

        if (!view.awakenScrollBars()) {
            // make sure the animation continues (this method should get called again on the next View.draw() call)
            view.postInvalidateOnAnimation();
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        edgeEffects.draw(canvas, scrollRangeX, scrollRangeY);
    }

    public void onOverScrolled(int scrollX, int scrollY, boolean clampedX, boolean clampedY) {
        if (!scroller.isFinished() && clampedY) {
            scroller.springBack(scrollX, scrollY, 0, scrollRangeX, 0, scrollRangeY);
            /* TODO: for now commenting this stuff out, let's see if we can live w/o it.
        // Treat animating scrolls differently; see #computeScroll() for why.
            viewInternalsManipulator.setScrollXRaw(this, scrollX);
            viewInternalsManipulator.setScrollYRaw(this, scrollY);
            viewInternalsManipulator.invalidateParentIfNeeded(this);
            */
        }

        view.scrollTo(scrollX, scrollY);
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        if ((event.getSource() & InputDevice.SOURCE_CLASS_POINTER) == 0) {
            return false;
        }

        if (event.getAction() != MotionEvent.ACTION_SCROLL) {
            return false;
        }

        if (dragDetector.isDragging()) {
            return false;
        }

        int oldScrollY = view.getScrollY();
        int newScrollY = oldScrollY;

        int oldScrollX = view.getScrollX();
        int newScrollX = oldScrollX;

        final float vscroll = event.getAxisValue(MotionEvent.AXIS_VSCROLL);
        if (vscroll != 0) {
            final int delta = (int) (vscroll * 3.0);//getVerticalScrollFactor() TODO

            newScrollY = oldScrollY - delta;
            if (newScrollY < 0) {
                newScrollY = 0;
            } else if (newScrollY > scrollRangeY) {
                newScrollY = scrollRangeY;
            }
        }

        final float hscroll = event.getAxisValue(MotionEvent.AXIS_HSCROLL);
        if (hscroll != 0) {
            final int delta = (int) (hscroll * 3.0); // TODO: getHorizontalScrollFactor()

            newScrollX = oldScrollX - delta;
            if (oldScrollX < 0) {
                oldScrollX = 0;
            } else if (oldScrollX > scrollRangeX) {
                oldScrollX = scrollRangeX;
            }
        }

        if (newScrollY != oldScrollY || newScrollX != oldScrollX) {
            view.scrollTo(newScrollX, newScrollY);
            return true;
        }

        return false;
    }

    @Override
    public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        if (disallowIntercept) {
            dragDetector.recycleVelocityTracker();
        }
    }

    public int computeVerticalScrollRange() {
        if (!view.isVerticalScrollBarEnabled()) {
            return 0;
        }

        final int contentHeight = view.getHeight() - view.getPaddingBottom() - view.getPaddingTop();

        int scrollRange = view.getAggregateChildHeight();

        final int scrollY = view.getScrollY();
        final int overscrollBottom = Math.max(0, scrollRange - contentHeight);
        if (scrollY < 0) {
            scrollRange -= scrollY;
        } else if (scrollY > overscrollBottom) {
            scrollRange += scrollY - overscrollBottom;
        }

        return scrollRange;
    }

    public int computeHorizontalScrollRange() {
        if (!view.isHorizontalScrollBarEnabled()) {
            return 0;
        }

        final int contentWidth = view.getWidth() - view.getPaddingLeft() - view.getPaddingRight();

        int scrollRange = view.getAggregateChildWidth();

        final int scrollX = view.getScrollX();
        final int overscrollRight = Math.max(0, scrollRange - contentWidth);
        if (scrollX < 0) {
            scrollRange -= scrollX;
        } else if (scrollX > overscrollRight) {
            scrollRange += scrollX - overscrollRight;
        }

        return scrollRange;
    }

    private Rect tempRect = new Rect(); // TODO: move above

    public int computeVerticalScrollOffset(int baseScrollOffset) {
        return Math.max(0, baseScrollOffset);
    }

    public int computeHorizontalScrollOffset(int baseScrollOffset) {
        return Math.max(0, baseScrollOffset);
    }

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        View currentFocused = view.findFocus();
        if (null == currentFocused || view == currentFocused)
            return;

        // If the currently-focused view was visible on the screen when the
        // screen was at the old height, then scroll the screen to make that
        // view visible with the new screen height.
        if (isWithinDeltaOfScreen(currentFocused, 0, oldh)) {
            currentFocused.getDrawingRect(tempRect);
            view.offsetDescendantRectToMyCoords(currentFocused, tempRect);
            int scrollDelta = computeScrollDeltaToGetChildRectOnScreen(tempRect);
            doScrollY(scrollDelta);
        }

        // TODO: w/ horizontal scrolling, need to handle width. need to think about how. applies also to following methods:
        //       computeScrollDeltaToGetChildRectOnScreen(), isWithinDeltaOfScreen(), doScrollY()
    }

    /**
     * Compute the amount to scroll in the Y direction in order to get
     * a rectangle completely on the screen (or, if taller than the screen,
     * at least the first screen size chunk of it).
     *
     * NOTE: only used by onSizeChanged
     *
     * @param rect The rect.
     * @return The scroll delta.
     */
    protected int computeScrollDeltaToGetChildRectOnScreen(Rect rect) {
        int height = view.getHeight();
        int screenTop = view.getScrollY();
        int screenBottom = screenTop + height;

        int fadingEdge = view.getVerticalFadingEdgeLength();

        // leave room for top fading edge as long as rect isn't at very top
        if (rect.top > 0) {
            screenTop += fadingEdge;
        }

        // leave room for bottom fading edge as long as rect isn't at very bottom
        if (rect.bottom < view.getAggregateChildHeight()) {
            screenBottom -= fadingEdge;
        }

        int scrollYDelta = 0;

        if (rect.bottom > screenBottom && rect.top > screenTop) {
            // need to move down to get it in view: move down just enough so
            // that the entire rectangle is in view (or at least the first
            // screen size chunk).

            if (rect.height() > height) {
                // just enough to get screen size chunk on
                scrollYDelta += (rect.top - screenTop);
            } else {
                // get entire rect at bottom of screen
                scrollYDelta += (rect.bottom - screenBottom);
            }

            // make sure we aren't scrolling beyond the end of our content
            int bottom = view.getAggregateChildHeight();
            int distanceToBottom = bottom - screenBottom;
            scrollYDelta = Math.min(scrollYDelta, distanceToBottom);

        } else if (rect.top < screenTop && rect.bottom < screenBottom) {
            // need to move up to get it in view: move up just enough so that
            // entire rectangle is in view (or at least the first screen
            // size chunk of it).

            if (rect.height() > height) {
                // screen size chunk
                scrollYDelta -= (screenBottom - rect.bottom);
            } else {
                // entire rect at top
                scrollYDelta -= (screenTop - rect.top);
            }

            // make sure we aren't scrolling any further than the top our content
            scrollYDelta = Math.max(scrollYDelta, -view.getScrollY());
        }
        return scrollYDelta;
    }

    /**
     * @return whether the descendant of this scroll view is within delta
     *  pixels of being on the screen.
     */
    private boolean isWithinDeltaOfScreen(View descendant, int delta, int height) {
        descendant.getDrawingRect(tempRect);
        view.offsetDescendantRectToMyCoords(descendant, tempRect);

        return (tempRect.bottom + delta) >= view.getScrollY()
            && (tempRect.top - delta) <= (view.getScrollY() + height);
    }

    /**
     * Smooth scroll by a Y delta
     *
     * @param delta the number of pixels to scroll by on the Y axis
     */
    private void doScrollY(int delta) {
        if (delta != 0) {
            view.scrollTo(view.getScrollX(), view.getScrollY() + delta);
        }
    }

    @Override
    public void manipulateScrollToCoords(Point point) {
        point.x = clamp(point.x, view.getWidth() - view.getPaddingRight() - view.getPaddingLeft(), view.getAggregateChildWidth());
        point.y = clamp(point.y, view.getHeight() - view.getPaddingBottom() - view.getPaddingTop(), view.getAggregateChildHeight());
    }

    public float getTopFadingEdgeStrength() {
        final int length = view.getVerticalFadingEdgeLength();
        if (view.getScrollY() < length) {
            return view.getScrollY() / (float) length;
        }

        return 1.0f;
    }

    private static int clamp(int n, int my, int child) {
        if (my >= child || n < 0) {
            /* my >= child is this case:
             *                    |--------------- me ---------------|
             *     |------ child ------|
             * or
             *     |--------------- me ---------------|
             *            |------ child ------|
             * or
             *     |--------------- me ---------------|
             *                                  |------ child ------|
             *
             * n < 0 is this case:
             *     |------ me ------|
             *                    |-------- child --------|
             *     |-- getScrollX() --|
             */
            return 0;
        }
        if ((my+n) > child) {
            /* this case:
             *                    |------ me ------|
             *     |------ child ------|
             *     |-- getScrollX() --|
             */
            return child-my;
        }
        return n;
    }

    public float getBottomFadingEdgeStrength() {
        final int length = view.getVerticalFadingEdgeLength();
        final int bottomEdge = view.getHeight() - view.getPaddingBottom();
        final int span = view.getAggregateChildHeight() - view.getScrollY() - bottomEdge;
        if (span < length) {
            return span / (float) length;
        }

        return 1.0f;
    }

    protected boolean scrollBy(int deltaX, int deltaY, int scrollX, int scrollY, int scrollRangeX, int scrollRangeY,
                               int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent) {
        final int overScrollMode = view.getOverScrollMode();
        final boolean canScrollHorizontal = view.getWidth() > view.getScrollX();
        final boolean canScrollVertical = computeVerticalScrollRange() > view.getHeight();
        final boolean overScrollHorizontal = overScrollMode == View.OVER_SCROLL_ALWAYS ||
            (overScrollMode == View.OVER_SCROLL_IF_CONTENT_SCROLLS && canScrollHorizontal);
        final boolean overScrollVertical = overScrollMode == View.OVER_SCROLL_ALWAYS ||
            (overScrollMode == View.OVER_SCROLL_IF_CONTENT_SCROLLS && canScrollVertical);

        int newScrollX = scrollX + deltaX;
        if (!overScrollHorizontal) {
            maxOverScrollX = 0;
        }

        int newScrollY = scrollY + deltaY;
        if (!overScrollVertical) {
            maxOverScrollY = 0;
        }

        // Clamp values if at the limits and record
        final int left = -maxOverScrollX;
        final int right = maxOverScrollX + scrollRangeX;
        final int top = -maxOverScrollY;
        final int bottom = maxOverScrollY + scrollRangeY;

        boolean clampedX = false;
        if (newScrollX > right) {
            newScrollX = right;
            clampedX = true;
        } else if (newScrollX < left) {
            newScrollX = left;
            clampedX = true;
        }

        boolean clampedY = false;
        if (newScrollY > bottom) {
            newScrollY = bottom;
            clampedY = true;
        } else if (newScrollY < top) {
            newScrollY = top;
            clampedY = true;
        }

        onOverScrolled(newScrollX, newScrollY, clampedX, clampedY);

        return clampedX || clampedY;
    }
}
