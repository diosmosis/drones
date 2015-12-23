package com.flarestar.drones.views.aspect;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.*;
import android.widget.OverScroller;
import com.flarestar.drones.views.aspect.scrolling.DragDetector;
import com.flarestar.drones.views.aspect.scrolling.DragVector;
import com.flarestar.drones.views.aspect.scrolling.EdgeEffects;
import com.flarestar.drones.views.aspect.scrolling.ScrollableViewport;
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
 *
 * TODO: ok if we only allow scrolling behavior on viewgroups? should change this later.
 */
public class ScrollingAspect extends ViewAspect {

    public static final int INVALID_POINTER = -1;

    private EdgeEffects edgeEffects;
    private DragDetector dragDetector;
    private ScrollableViewport scrollableViewport;

    public int overscrollDistance;
    private int overflingDistance;

    public OverScroller scroller;

    private BoxModelNode view;

    // TODO: make these configurable via LESS
    private float verticalScrollFactor;
    private float horizontalScrollFactor;

    public ScrollingAspect(BoxModelNode view) {
        this.view = view;

        final Context context = view.getContext();

        final ViewConfiguration configuration = ViewConfiguration.get(context);
        final int touchSlop = configuration.getScaledTouchSlop();
        final int minimumVelocityForFling = configuration.getScaledMinimumFlingVelocity();
        final int maximumVelocityForFling = configuration.getScaledMaximumFlingVelocity();
        overscrollDistance = configuration.getScaledOverscrollDistance();
        overflingDistance = configuration.getScaledOverflingDistance();

        scroller = new OverScroller(context);

        DragVector dragVector = new DragVector(touchSlop, view.isHorizontalScrollBarEnabled(), view.isVerticalScrollBarEnabled());
        dragDetector = new DragDetector(dragVector, minimumVelocityForFling, maximumVelocityForFling);

        edgeEffects = new EdgeEffects(view, view.getOverScrollMode(), view.isHorizontalScrollBarEnabled(),
            view.isVerticalScrollBarEnabled());

        scrollableViewport = new ScrollableViewport();

        view.setFocusable(true);
        view.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
        view.setWillNotDraw(false);

        TypedValue value = new TypedValue();
        if (context.getTheme().resolveAttribute(android.R.attr.listPreferredItemHeight, value, true)) {
            DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
            horizontalScrollFactor = verticalScrollFactor = value.getDimension(displayMetrics);
        }
    }

    /**
     * Update the computed scroll range after a measure pass has been done on the view.
     */
    @Override
    public void onLayoutStarted() {
        scrollableViewport.computeScrollRange(view);

        // disable or enable edge effects
        if (scrollableViewport.canOverscrollX() || scrollableViewport.canOverscrollY()) {
            edgeEffects.enable();
        } else {
            edgeEffects.disable();
        }
    }

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
            if (scroller.springBack(view.getScrollX(), view.getScrollY(), 0, scrollableViewport.getScrollRangeX(), 0,
                    scrollableViewport.getScrollRangeY())) {
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

        boolean overScrolled = scrollBy(scrollDx, scrollDy, overscrollDistance, overscrollDistance);
        if (overScrolled) {
            // Break our velocity if we hit a scroll barrier.
            dragDetector.resetVelocityTracker();
        }

        view.onScrollChanged(view.getScrollX(), view.getScrollY(), oldX, oldY);

        edgeEffects.onDrag(oldX, oldY, scrollDx, scrollDy, scrollableViewport.getScrollRangeX(),
            scrollableViewport.getScrollRangeY());
    }

    private boolean scrollBy(int scrollDx, int scrollDy, int maxOverScrollDeltaX, int maxOverScrollDeltaY) {
        boolean overScrolled = scrollableViewport.scrollBy(view, scrollDx, scrollDy, maxOverScrollDeltaX, maxOverScrollDeltaY);

        final int newScrollX = scrollableViewport.getNewScrollX();
        final int newScrollY = scrollableViewport.getNewScrollY();

        if (isFlinging() && overScrolled) {
            scroller.springBack(newScrollX, newScrollY, 0, scrollableViewport.getScrollRangeX(), 0, scrollableViewport.getScrollRangeY());
        }

        view.scrollTo(newScrollX, newScrollY);

        return overScrolled;
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
            scrollBy(x - oldX, y - oldY, overflingDistance, overflingDistance);

            // TODO: (this is more of a note) removing this since in drones we don't care about View subclasses. there should be none. instead, we'll post a scope event when the scroll changes
            // onScrollChanged(getScrollX(), getScrollY(), oldX, oldY

            edgeEffects.onScroll(oldX, oldY, x, y, scrollableViewport.getScrollRangeX(), scrollableViewport.getScrollRangeY(), scroller);
        }

        if (!view.awakenScrollBars()) {
            // make sure the animation continues (this method should get called again on the next View.draw() call)
            view.postInvalidateOnAnimation();
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        edgeEffects.draw(canvas, scrollableViewport.getScrollRangeX(), scrollableViewport.getScrollRangeY());
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        if (event.getAction() != MotionEvent.ACTION_SCROLL
            || dragDetector.isDragging()
            || (event.getSource() & InputDevice.SOURCE_CLASS_POINTER) == 0
        ) {
            return false;
        }

        int deltaX = 0;
        int deltaY = 0;

        final float vscroll = event.getAxisValue(MotionEvent.AXIS_VSCROLL);
        if (vscroll != 0) {
            deltaY = (int) (-vscroll * verticalScrollFactor);
        }

        final float hscroll = event.getAxisValue(MotionEvent.AXIS_HSCROLL);
        if (hscroll != 0) {
            deltaX = (int) (-hscroll * horizontalScrollFactor);
        }

        scrollBy(deltaX, deltaY, 0, 0);

        return deltaX != 0 || deltaY != 0;
    }

    @Override
    public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        if (disallowIntercept) {
            dragDetector.recycleVelocityTracker();
        }
    }

    public int computeVerticalScrollOffset(int baseScrollOffset) {
        return Math.max(0, baseScrollOffset);
    }

    public int computeHorizontalScrollOffset(int baseScrollOffset) {
        return Math.max(0, baseScrollOffset);
    }

    private Rect tempRect = new Rect(); // TODO: move above

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

    public int computeVerticalScrollRange() {
        if (!view.isVerticalScrollBarEnabled()) {
            return 0;
        }

        return view.getAggregateChildHeight();
    }

    public int computeHorizontalScrollRange() {
        if (!view.isHorizontalScrollBarEnabled()) {
            return 0;
        }

        return view.getAggregateChildWidth();
    }
}
