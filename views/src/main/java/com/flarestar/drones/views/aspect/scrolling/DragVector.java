package com.flarestar.drones.views.aspect.scrolling;

import android.view.MotionEvent;
import com.flarestar.drones.views.aspect.ScrollingAspect;

/**
 * TODO
 */
public class DragVector {
    private int activePointerId = ScrollingAspect.INVALID_POINTER;

    private int x1;
    private int y1;

    public int dx;
    public int dy;

    public int scrollDx;
    public int scrollDy;

    private int touchSlop;

    private boolean isHorizontalScrollingEnabled;
    private boolean isVerticalScrollingEnabled;

    public DragVector(int touchSlop, boolean isHorizontalScrollingEnabled, boolean isVerticalScrollingEnabled) {
        this.touchSlop = touchSlop;
        this.isHorizontalScrollingEnabled = isHorizontalScrollingEnabled;
        this.isVerticalScrollingEnabled = isVerticalScrollingEnabled;
    }

    public void setStart(MotionEvent motion) {
        assert activePointerId != ScrollingAspect.INVALID_POINTER;

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
        assert activePointerId != ScrollingAspect.INVALID_POINTER; // TODO: use BuildConfig?

        final int activePointerIndex = motion.findPointerIndex(activePointerId);
        scrollDx = dx = isHorizontalScrollingEnabled ? ((int) motion.getX(activePointerIndex) - x1) : 0;
        scrollDy = dy = isVerticalScrollingEnabled ? ((int) motion.getY(activePointerIndex) - y1) : 0;
    }

    public int magnitude() {
        return (int) Math.sqrt(dx * dx + dy * dy);
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
        activePointerId = ScrollingAspect.INVALID_POINTER;
    }

    /**
     * Dragging doesn't start until the user has moved a certain distance (the distance is
     * called the 'touch slop'). When dragging starts, we want to *start* scrolling, as
     * if the user has not moved their finger at all until now. So once the drag has started, we remove
     * the slop from the deltas then move the viewport.
     * <p/>
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

    public void onTouchDragStart() {
        scrollDx = withoutSlop(scrollDx);
        scrollDy = withoutSlop(scrollDy);
    }
}
