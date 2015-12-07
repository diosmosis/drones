package com.flarestar.drones.views.aspect;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.view.*;
import android.widget.EdgeEffect;
import android.widget.OverScroller;
import com.flarestar.drones.views.viewgroups.BoxModelNode;

/**
 * TODO: unimplemented features:
 * - page scrolling (page up/down)
 * - key scrolling (arrow down/up/left/right)
 * - home/end scrolling (scroll to top/bottom)
 * - accessibility operations
 * - programattic flinging
 * - "smooth" scrolling (can probably made more generic than what is in ScrollView)
 */
public class ScrollingAspect extends ViewAspect {

    /**
     * Sentinel value for no current active pointer.
     * Used by {@link #activePointerId}.
     */
    public static final int INVALID_POINTER = -1;

    /**
     * ID of the active pointer. This is used to retain consistency during
     * drags/flings if multiple pointers are used.
     */
    public int activePointerId = INVALID_POINTER;

    /**
     * Position of the last motion event.
     */
    public int lastMotionY;

    /**
     * TODO: wtf is a 'touch slop'?
     */
    public int touchSlop;

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

    // TODO: needs to be initialized properly.
    private EdgeEffect edgeGlowTop;
    private EdgeEffect edgeGlowBottom;

    public ScrollingAspect(BoxModelNode view) {
        this.view = view;

        final Context context = view.getContext();

        scroller = new OverScroller(context);

        final ViewConfiguration configuration = ViewConfiguration.get(context);
        touchSlop = configuration.getScaledTouchSlop();
        minimumVelocityForFling = configuration.getScaledMinimumFlingVelocity();
        maximumVelocityForFling = configuration.getScaledMaximumFlingVelocity();
        overscrollDistance = configuration.getScaledOverscrollDistance();
        overflingDistance = configuration.getScaledOverflingDistance();

        final int mode = view.getOverScrollMode();
        if (mode != View.OVER_SCROLL_NEVER) {
            edgeGlowTop = new EdgeEffect(context);
            edgeGlowBottom = new EdgeEffect(context);
        }

        view.setFocusable(true);
        view.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
        view.setWillNotDraw(false);
    }

    public boolean isDragging() {
        return isDragging;
    }

    /**
     * Can be used to intercept events that are meant for child views.
     *
     * Used to check if there is the user is dragging within the scrollable view. If yes,
     * the events are intercepted (by returning true). In this case, future events are
     * sent to the scrollable View's onTouchEvent. When onTouchEvent returns false,
     * the intercepting stops.
     *
     * @param event
     * @return
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        // TODO: ok if we only allow scrolling behavior on viewgroups? should change this later.

        final int action = event.getAction();

        // most common case (supposedly), action is ongoing move, and we are currently dragging
        if (action == MotionEvent.ACTION_MOVE && isDragging()) {
            return true;
        }

        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                if (!inChild((int) event.getX(), (int) event.getY())) {
                    abortDrag();
                    return false;
                }

                cleanVelocityTracker();

                handleTouchDown(event);
                break;
            case MotionEvent.ACTION_MOVE:
                handleTouchMove(event);
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                handleTouchCancel(event);
                break;
            case MotionEvent.ACTION_POINTER_UP:
                onSecondaryPointerUp(event);
                break;
        }

        return isDragging();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        getVelocityTracker().addMovement(event); // TODO: move to individual handling methods

        final int action = event.getAction();

        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                handleTouchDown(event);
                break;
            case MotionEvent.ACTION_MOVE:
                handleTouchMove(event);
                break;
            case MotionEvent.ACTION_UP:
                handleTouchUp(event);
                break;
            case MotionEvent.ACTION_CANCEL:
                handleTouchCancel(event);
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                saveMotionState(event, event.getActionIndex());
                break;
            case MotionEvent.ACTION_POINTER_UP:
                onSecondaryPointerUp(event);
                saveMotionState(event);
                break;
        }

        return true;
    }

    private void handleTouchDown(MotionEvent event) {
        saveMotionState(event);
        getVelocityTracker().addMovement(event);

        // If being flinged and user touches the screen, initiate drag.
        if (isFlinging()) {
            startDrag();
        }
    }

    private void handleTouchMove(MotionEvent event) {
        if (activePointerId == INVALID_POINTER) {
            // If we don't have a valid id, the initial touch down was on content outside of this view.
            return;
        }

        final int activePointerIndex = event.findPointerIndex(activePointerId);
        final int y = (int) event.getY(activePointerIndex);
        int deltaY = lastMotionY - y;
        if (!isDragging() && Math.abs(deltaY) > touchSlop) {
            startDrag();

            if (deltaY > 0) {
                deltaY -= touchSlop;
            } else {
                deltaY += touchSlop;
            }
        }

        if (!isDragging()) {
            return;
        }

        saveMotionState(event);

        getVelocityTracker().addMovement(event);

        final int oldX = view.getScrollX();
        final int oldY = view.getScrollY();
        final int range = getScrollRange(); // TODO: handle non ViewGroups please.

        // TODO: don't depend on overScrollBy/onOverScrolled in View. it's fucking weird. instead let's re-implement scrolling ourselves.
        if (scrollBy(0, deltaY, 0, view.getScrollY(), 0, range, 0, overscrollDistance, true)) {
            // Break our velocity if we hit a scroll barrier.
            resetVelocityTracker();
        }

        view.onScrollChanged(view.getScrollX(), view.getScrollY(), oldX, oldY);

        if (canOverscroll()) {
            final int pulledToY = oldY + deltaY;
            if (pulledToY < 0) {
                edgeGlowTop.onPull((float) deltaY / view.getHeight());
                if (!edgeGlowBottom.isFinished()) {
                    edgeGlowBottom.onRelease();
                }
            } else if (pulledToY > range) {
                edgeGlowBottom.onPull((float) deltaY / view.getHeight());
                if (!edgeGlowTop.isFinished()) {
                    edgeGlowTop.onRelease();
                }
            }

            if (!edgeGlowTop.isFinished() || !edgeGlowBottom.isFinished()) {
                view.postInvalidateOnAnimation();
            }
        }
    }

    private void handleTouchCancel(MotionEvent event) {
        if (!isDragging()) {
            return;
        }

        abortDrag();

        if (scroller.springBack(view.getScrollX(), view.getScrollY(), 0, 0, 0, getScrollRange())) {
            view.postInvalidateOnAnimation();
        }
    }

    private void handleTouchUp(MotionEvent event) {
        if (!isDragging()) {
            return;
        }

        final VelocityTracker velocityTracker = getVelocityTracker();
        velocityTracker.computeCurrentVelocity(1000, maximumVelocityForFling);
        int initialVelocity = (int) velocityTracker.getYVelocity(activePointerId);

        if ((Math.abs(initialVelocity) > minimumVelocityForFling)) {
            startFling(-initialVelocity);
        } else {
            if (scroller.springBack(view.getScrollX(), view.getScrollY(), 0, 0, 0, getScrollRange())) {
                view.postInvalidateOnAnimation();
            }

            abortDrag();
        }
    }

    public void startFling(int velocityY) {
        int height = view.getHeight() - view.getPaddingBottom() - view.getPaddingTop();
        int bottom = view.getAggregateChildHeight();

        scroller.fling(view.getScrollX(), view.getScrollY(), 0, velocityY, 0, 0, 0,
            Math.max(0, bottom - height), 0, height/2);

        view.postInvalidateOnAnimation();
    }

    public void startDrag() {
        if (isFlinging()) {
            abortFling();
        }

        isDragging = true;

        final ViewParent parent = view.getParent();
        if (parent != null) {
            parent.requestDisallowInterceptTouchEvent(true); // TODO: do this when starting dragbehavior? (done once below)
        }
    }

    private void abortFling() {
        scroller.abortAnimation();
    }

    public void abortDrag() {
        isDragging = false;

        activePointerId = INVALID_POINTER;

        recycleVelocityTracker();

        if (edgeGlowTop != null) {
            edgeGlowTop.onRelease();
            edgeGlowBottom.onRelease();
        }
    }

    // TODO: copied methods below. they all seem strange and possibly unnecessary. let's try to remove them.
    private int getScrollRange() {
        return Math.max(0, view.getAggregateChildHeight() - (view.getHeight() - view.getPaddingBottom() - view.getPaddingTop()));
    }

    /**
     * Returns true if a point relative to the scrollable view's viewport is within the scrollable contents.
     *
     * TODO: this is weird code. i don't think it's even needed. won't a point in the motion event ALWAYS be in the view?
     *
     * @param x
     * @param y
     * @return
     */
    private boolean inChild(int x, int y) {
        // make x & y relative to the scroll position
        x += view.getScrollX();
        y += view.getScrollY();

        return y >= 0 && y < view.getAggregateChildHeight() && x >= 0 && x < view.getAggregateChildWidth();
    }


    private void onSecondaryPointerUp(MotionEvent ev) {
        final int pointerIndex = (ev.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >>
            MotionEvent.ACTION_POINTER_INDEX_SHIFT;
        final int pointerId = ev.getPointerId(pointerIndex);
        if (pointerId == activePointerId) {
            // This was our active pointer going up. Choose a new
            // active pointer and adjust accordingly.
            // TODO: Make this decision more intelligent.
            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            saveMotionState(ev, newPointerIndex);
            resetVelocityTracker();
        }
    }


    public VelocityTracker getVelocityTracker() {
        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain();
        }
        return velocityTracker;
    }

    public void recycleVelocityTracker() {
        if (velocityTracker != null) {
            velocityTracker.recycle();
            velocityTracker = null;
        }
    }

    public VelocityTracker getCleanVelocityTracker() {
        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain();
        } else {
            velocityTracker.clear();
        }
        return velocityTracker;
    }

    public void cleanVelocityTracker() {
        if (velocityTracker != null) {
            velocityTracker.clear();
        }
    }

    public void resetVelocityTracker() {
        if (velocityTracker != null) {
            velocityTracker.clear();
        }
    }

    public void saveMotionState(MotionEvent event) {
        if (activePointerId == INVALID_POINTER) {
            lastMotionY = (int) event.getY();
            activePointerId = event.getPointerId(0);
        } else {
            final int pointerIndex = event.findPointerIndex(activePointerId);
            lastMotionY = (int) event.getY(pointerIndex);
        }
    }

    public void saveMotionState(MotionEvent event, int pointerIndex) {
        assert activePointerId == INVALID_POINTER;

        lastMotionY = (int) event.getY();
        activePointerId = event.getPointerId(pointerIndex);
    }

    public boolean isFlinging() {
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
            final int range = getScrollRange();
            scrollBy(x - oldX, y - oldY, oldX, oldY, 0, range, 0, overflingDistance, false);

            // TODO: (this is more of a note) removing this since in drones we don't care about View subclasses. there should be none. instead, we'll post a scope event when the scroll changes
            //       we do want to make sure views are created if scroll changes though. so i guess it should be posted. for now, comment it out.
            // onScrollChanged(getScrollX(), getScrollY(), oldX, oldY

            if (canOverscroll()) {
                if (y < 0 && oldY >= 0) {
                    edgeGlowTop.onAbsorb((int) scroller.getCurrVelocity());
                } else if (y > range && oldY <= range) {
                    edgeGlowBottom.onAbsorb((int) scroller.getCurrVelocity());
                }
            }
        }

        if (!view.awakenScrollBars()) {
            // make sure the animation continues (this method should get called again on the next View.draw() call)
            view.postInvalidateOnAnimation();
        }
    }

    public boolean canOverscroll() {
        final int overscrollMode = view.getOverScrollMode();
        final int range = getScrollRange();

        return overscrollMode == View.OVER_SCROLL_ALWAYS
            || (overscrollMode == View.OVER_SCROLL_IF_CONTENT_SCROLLS && range > 0);
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (edgeGlowTop == null) {
            return;
        }

        final int width = view.getWidth() - view.getPaddingLeft() - view.getPaddingRight();
        final int height = view.getHeight();

        final int scrollY = view.getScrollY();
        if (!edgeGlowTop.isFinished()) {
            final int restoreCount = canvas.save();

            canvas.translate(view.getPaddingLeft(), Math.min(0, scrollY));

            edgeGlowTop.setSize(width, height);
            if (edgeGlowTop.draw(canvas)) {
                view.postInvalidateOnAnimation();
            }

            canvas.restoreToCount(restoreCount);
        }

        if (!edgeGlowBottom.isFinished()) {
            final int restoreCount = canvas.save();

            canvas.translate(-width + view.getPaddingLeft(), Math.max(getScrollRange(), scrollY) + height);
            canvas.rotate(180, width, 0);

            edgeGlowBottom.setSize(width, height);
            if (edgeGlowBottom.draw(canvas)) {
                view.postInvalidateOnAnimation();
            }

            canvas.restoreToCount(restoreCount);
        }
    }

    public void onOverScrolled(int scrollX, int scrollY, boolean clampedX, boolean clampedY) {
        // Treat animating scrolls differently; see #computeScroll() for why.
        if (!scroller.isFinished() && clampedY) {
            scroller.springBack(scrollX, scrollY, 0, 0, 0, getScrollRange());
            /* TODO: for now commenting this stuff out, let's see if we can live w/o it.
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

        if (isDragging()) {
            return false;
        }

        final float vscroll = event.getAxisValue(MotionEvent.AXIS_VSCROLL);
        if (vscroll != 0) {
            final int delta = (int) (vscroll * 3.0);//getVerticalScrollFactor() TODO
            final int range = getScrollRange();
            int oldScrollY = view.getScrollY();
            int newScrollY = oldScrollY - delta;
            if (newScrollY < 0) {
                newScrollY = 0;
            } else if (newScrollY > range) {
                newScrollY = range;
            }
            if (newScrollY != oldScrollY) {
                view.scrollTo(view.getScrollX(), newScrollY);
                return true;
            }
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

    private Rect tempRect = new Rect();

    public int computeVerticalScrollOffset(int baseScrollOffset) {
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
    }

    /**
     * Compute the amount to scroll in the Y direction in order to get
     * a rectangle completely on the screen (or, if taller than the screen,
     * at least the first screen size chunk of it).
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
