package com.flarestar.drones.views.aspect.scrolling;

import android.view.MotionEvent;
import android.view.VelocityTracker;
import com.flarestar.drones.views.aspect.ScrollingAspect;

/**
 * TODO
 */
public class DragDetector {
    /**
     * Determines speed during touch scrolling
     */
    private VelocityTracker velocityTracker;

    private boolean isDragging = false;
    private boolean dragStarted = false;
    private boolean dragEnded = false;
    private boolean isFlungVertical = false;
    private boolean isFlungHorizontal = false;

    private DragVector dragVector;

    private int minimumVelocityForFling;
    private int maximumVelocityForFling;
    private int initialVelocityY;
    private int initialVelocityX;

    public DragDetector(DragVector dragVector, int minimumVelocityForFling, int maximumVelocityForFling) {
        this.dragVector = dragVector;
        this.minimumVelocityForFling = minimumVelocityForFling;
        this.maximumVelocityForFling = maximumVelocityForFling;
    }

    public boolean isDragging() {
        return isDragging;
    }

    // TODO: review event handling logic, make sure if events are sent to different methods in a strange way
    //       things still work as expected. we can use unit tests for this i think?
    public void handleTouchEvent(MotionEvent event, boolean isIntercepting, boolean isFlinging) {
        dragStarted = false;
        dragEnded = false;

        if (!isIntercepting) {
            isFlungVertical = false;
            isFlungHorizontal = false;

            getVelocityTracker().addMovement(event);
        }

        final int action = event.getAction();

        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                handleTouchDown(event, isFlinging);
                break;
            case MotionEvent.ACTION_MOVE:
                handleTouchMove(event);
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                handleTouchUp(event, isIntercepting);
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                dragVector.setStart(event, event.getActionIndex());
                break;
            case MotionEvent.ACTION_POINTER_UP:
                handleSecondaryPointerUp(event);
                break;
        }
    }

    private void handleTouchDown(MotionEvent event, boolean isFlinging) {
        dragVector.setStart(event, 0);

        // If being flinged and user touches the screen, initiate drag.
        if (isFlinging) {
            startDrag();
        }
    }

    private void handleTouchMove(MotionEvent event) {
        if (dragVector.pointer() == ScrollingAspect.INVALID_POINTER) {
            // If we don't have a valid id, the initial touch down was on content outside of this view.
            return;
        }

        dragVector.setEnd(event);

        if (!isDragging && dragVector.hasDragStarted()) {
            startDrag();
            dragVector.onTouchDragStart();
        }

        if (isDragging) {
            dragVector.advance();
        }
    }

    private void handleTouchUp(MotionEvent event, boolean isIntercepting) {
        if (!isDragging) {
            return;
        }

        if (!isIntercepting) {
            final VelocityTracker velocityTracker = getVelocityTracker();
            velocityTracker.computeCurrentVelocity(1000, maximumVelocityForFling);
            initialVelocityY = (int) velocityTracker.getYVelocity(dragVector.pointer());
            initialVelocityX = (int) velocityTracker.getXVelocity(dragVector.pointer());

            if (Math.abs(initialVelocityY) > minimumVelocityForFling) {
                isFlungVertical = true;
            } else if (Math.abs(initialVelocityX) > minimumVelocityForFling) {
                isFlungHorizontal = true;
            }
        }

        abortDrag();
    }

    private void handleSecondaryPointerUp(MotionEvent ev) {
        final int pointerIndex = (ev.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >>
            MotionEvent.ACTION_POINTER_INDEX_SHIFT;
        final int pointerId = ev.getPointerId(pointerIndex);
        if (pointerId == dragVector.pointer()) {
            // Our active pointer is no longer touching the screen. Choose a new
            // active pointer to control the current scrolling action.
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

    public void recycleVelocityTracker() {
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

    public void resetVelocityTracker() {
        if (velocityTracker != null) {
            velocityTracker.clear();
        }
    }

    private void startDrag() {
        isDragging = true;

        dragStarted = true;
        dragEnded = false;

        cleanVelocityTracker();
    }

    private void abortDrag() {
        isDragging = false;

        dragStarted = false;
        dragEnded = true;

        dragVector.clear();
        recycleVelocityTracker();
    }

    public boolean isDragStarted() {
        return dragStarted;
    }

    public boolean isDragEnded() {
        return dragEnded;
    }

    public boolean isFlungVertical() {
        return isFlungVertical;
    }

    public boolean isFlungHorizontal() {
        return isFlungHorizontal;
    }

    public int getInitialVelocityY() {
        return initialVelocityY;
    }

    public int getInitialVelocityX() {
        return initialVelocityX;
    }

    public int getScrolledByX() {
        return dragVector.scrollDx;
    }

    public int getScrolledByY() {
        return dragVector.scrollDy;
    }
}
