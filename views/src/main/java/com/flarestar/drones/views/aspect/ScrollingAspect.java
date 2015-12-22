package com.flarestar.drones.views.aspect;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.view.*;
import android.widget.OverScroller;
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

    // TODO: let's move these inner classes out of here eventually.
    public class DragVector {
        private int activePointerId = INVALID_POINTER;

        private int x1;
        private int y1;

        public int dx;
        public int dy;

        private int touchSlop;

        public DragVector(int touchSlop) {
            this.touchSlop = touchSlop;
        }

        public void setStart(MotionEvent motion) {
            assert activePointerId != INVALID_POINTER;

            final int activePointerIndex = motion.findPointerIndex(activePointerId);
            x1 = (int) motion.getX(activePointerIndex);
            y1 = (int) motion.getY(activePointerIndex);

            dx = dy = 0;
        }

        public void setStart(MotionEvent motion, int pointerIndex) {
            x1 = (int) motion.getX();
            y1 = (int) motion.getY();
            activePointerId = motion.getPointerId(pointerIndex);

            dx = dy = 0;
        }

        public void setEnd(MotionEvent motion) {
            assert activePointerId != INVALID_POINTER; // TODO: use BuildConfig?

            final int activePointerIndex = motion.findPointerIndex(activePointerId);
            dx = view.isHorizontalScrollBarEnabled() ? ((int) motion.getX(activePointerIndex) - x1) : 0;
            dy = view.isVerticalScrollBarEnabled() ? ((int) motion.getY(activePointerIndex) - y1) : 0;
        }

        public int magnitude() {
            return (int)Math.sqrt(dx * dx + dy * dy);
        }

        public int pointer() {
            return activePointerId;
        }

        public void advance() {
            x1 += dx;
            dx = 0;

            y1 += dy;
            dy = 0;
        }

        public void clear() {
            activePointerId = INVALID_POINTER;
        }

        /**
         * Dragging doesn't start until the user has moved a certain distance (the distance is
         * called the 'touch slop'). When dragging starts, we want to *start* scrolling, as
         * if the user has not moved their finger at all until now. So once the drag has started, we remove
         * the slop from the deltas then move the viewport.
         *
         * This only happens when starting a drag via a finger movement.
         */
        public int withoutSlop(int delta) {
            if (delta == 0) {
                return 0;
            } else if (delta > 0) {
                return delta - touchSlop;
            } else {
                return delta + touchSlop;
            }
        }

        public boolean hasDragStarted() {
            return magnitude() > touchSlop;
        }
    }

    private DragVector dragVector;
    private EdgeEffects edgeEffects;

    public int minimumVelocityForFling;
    public int maximumVelocityForFling;
    public int overscrollDistance;
    private int overflingDistance;

    public OverScroller scroller;

    /**
     * Determines speed during touch scrolling
     */
    private VelocityTracker velocityTracker;

    private BoxModelNode view;

    private boolean isDragging = false;

    private int scrollRangeX;
    private int scrollRangeY;

    public ScrollingAspect(BoxModelNode view) {
        this.view = view;

        final Context context = view.getContext();

        scroller = new OverScroller(context);

        final ViewConfiguration configuration = ViewConfiguration.get(context);
        final int touchSlop = configuration.getScaledTouchSlop();
        minimumVelocityForFling = configuration.getScaledMinimumFlingVelocity();
        maximumVelocityForFling = configuration.getScaledMaximumFlingVelocity();
        overscrollDistance = configuration.getScaledOverscrollDistance();
        overflingDistance = configuration.getScaledOverflingDistance();

        dragVector = new DragVector(touchSlop);
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
        handleInterceptedTouchEvent(event);
        return isDragging;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        getVelocityTracker().addMovement(event);
        handleTouchEvent(event, false);
        return true;
    }

    private void handleTouchDown(MotionEvent event) {
        dragVector.setStart(event, 0);

        // If being flinged and user touches the screen, initiate drag.
        if (isFlinging()) {
            startDrag();
        }
    }

    private void handleInterceptedTouchEvent(MotionEvent event) {
        handleTouchEvent(event, true);
    }

    private void handleTouchEvent(MotionEvent event, boolean isForChildView) {
        final int action = event.getAction();

        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                handleTouchDown(event);
                break;
            case MotionEvent.ACTION_MOVE:
                // not sure why, but if we're currently dragging & intercepting, we just intercept the move w/o handling it
                // after some light experimentation, this does not appear to ever occur. i think it is here just in case
                // android sends events in a strange order?
                // TODO: review event handling logic, make sure if events are sent to different methods in a strange way
                //       things still work as expected. we can use unit tests for this i think?
                if (isForChildView && isDragging) {
                    break;
                }

                handleTouchMove(event);
                break;
            case MotionEvent.ACTION_UP:
                if (isForChildView) {
                    handleTouchCancel(event);
                } else {
                    handleTouchUp(event);
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                handleTouchCancel(event);
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                if (!isForChildView) {
                    dragVector.setStart(event, event.getActionIndex());
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
                handleSecondaryPointerUp(event);
                break;
        }
    }

    private void handleTouchMove(MotionEvent event) {
        if (dragVector.pointer() == INVALID_POINTER) {
            // If we don't have a valid id, the initial touch down was on content outside of this view.
            return;
        }

        dragVector.setEnd(event);

        int scrollDeltaY = dragVector.dy;
        int scrollDeltaX = dragVector.dx;

        if (!isDragging && dragVector.hasDragStarted()) {
            startDrag();

            scrollDeltaX = dragVector.withoutSlop(scrollDeltaX);
            scrollDeltaY = dragVector.withoutSlop(scrollDeltaY);
        }

        if (!isDragging) {
            return;
        }

        dragVector.advance();

        getVelocityTracker().addMovement(event);

        final int oldX = view.getScrollX();
        final int oldY = view.getScrollY();

        boolean overScrolled = scrollBy(scrollDeltaX, scrollDeltaY, view.getScrollX(), view.getScrollY(), scrollRangeX,
            scrollRangeY, overscrollDistance, overscrollDistance, true);
        if (overScrolled) {
            // Break our velocity if we hit a scroll barrier.
            resetVelocityTracker();
        }

        view.onScrollChanged(view.getScrollX(), view.getScrollY(), oldX, oldY);

        edgeEffects.onDrag(oldX, oldY, scrollDeltaX, scrollDeltaY, scrollRangeX, scrollRangeY);
    }

    private void handleTouchCancel(MotionEvent event) {
        if (!isDragging) {
            return;
        }

        abortDrag();
    }

    private void handleTouchUp(MotionEvent event) {
        if (!isDragging) {
            return;
        }

        final VelocityTracker velocityTracker = getVelocityTracker();
        velocityTracker.computeCurrentVelocity(1000, maximumVelocityForFling);
        int initialVelocityY = (int) velocityTracker.getYVelocity(dragVector.pointer());
        int initialVelocityX = (int) velocityTracker.getXVelocity(dragVector.pointer());

        abortDrag();

        if (Math.abs(initialVelocityY) > minimumVelocityForFling) {
            startVerticalFling(initialVelocityY);
        } else if (Math.abs(initialVelocityX) > minimumVelocityForFling) {
            startHorizontalFling(initialVelocityX);
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

    private void startDrag() {
        if (isFlinging()) {
            abortFling();
        }

        isDragging = true;

        final ViewParent parent = view.getParent();
        if (parent != null) {
            parent.requestDisallowInterceptTouchEvent(true);
        }

        cleanVelocityTracker();
    }

    private void abortFling() {
        scroller.abortAnimation();
    }

    private void abortDrag() {
        if (scroller.springBack(view.getScrollX(), view.getScrollY(), 0, scrollRangeX, 0, scrollRangeY)) {
            view.postInvalidateOnAnimation();
        }

        isDragging = false;
        dragVector.clear();
        recycleVelocityTracker();
        edgeEffects.release();
    }

    private void handleSecondaryPointerUp(MotionEvent ev) {
        final int pointerIndex = (ev.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >>
            MotionEvent.ACTION_POINTER_INDEX_SHIFT;
        final int pointerId = ev.getPointerId(pointerIndex);
        if (pointerId == dragVector.pointer()) {
            // This was our active pointer going up. Choose a new
            // active pointer and adjust accordingly.
            // TODO: Make this decision more intelligent.
            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            dragVector.setStart(ev, newPointerIndex);
            resetVelocityTracker();
        } else {
            dragVector.setStart(ev);
        }
    }


    private VelocityTracker getVelocityTracker() {
        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain();
        }
        return velocityTracker;
    }

    private void recycleVelocityTracker() {
        if (velocityTracker != null) {
            velocityTracker.recycle();
            velocityTracker = null;
        }
    }

    private void cleanVelocityTracker() {
        if (velocityTracker != null) {
            velocityTracker.clear();
        }
    }

    private void resetVelocityTracker() {
        if (velocityTracker != null) {
            velocityTracker.clear();
        }
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

        if (isDragging) {
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
            recycleVelocityTracker();
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
