package com.flarestar.drones.views.aspect.scrolling;

import android.content.Context;
import android.graphics.Canvas;
import android.view.View;
import android.widget.EdgeEffect;
import android.widget.OverScroller;

/**
 * TODO
 */
public class EdgeEffects {
    private EdgeEffect edgeGlowTop;
    private EdgeEffect edgeGlowBottom;
    private EdgeEffect edgeGlowLeft;
    private EdgeEffect edgeGlowRight;

    private View view;
    private boolean disabled = true;

    public EdgeEffects(View view, int overscrollMode, boolean isHorizontalScrollingEnabled,
                       boolean isVerticalScrollingEnabled) {
        this.view = view;

        if (overscrollMode == View.OVER_SCROLL_NEVER) {
            return;
        }

        final Context context = view.getContext();
        if (isHorizontalScrollingEnabled) {
            edgeGlowLeft = new EdgeEffect(context);
            edgeGlowRight = new EdgeEffect(context);
        }

        if (isVerticalScrollingEnabled) {
            edgeGlowTop = new EdgeEffect(context);
            edgeGlowBottom = new EdgeEffect(context);
        }
    }

    public void enable() {
        disabled = false;
    }

    public void disable() {
        disabled = true;
    }

    public void release() {
        if (edgeGlowTop != null) {
            edgeGlowTop.onRelease();
            edgeGlowBottom.onRelease();
        }

        if (edgeGlowLeft != null) {
            edgeGlowLeft.onRelease();
            edgeGlowRight.onRelease();
        }
    }

    public void onDrag(int oldX, int oldY, int deltaX, int deltaY, int scrollRangeX, int scrollRangeY) {
        if (disabled) {
            return;
        }

        boolean shouldInvalidate = updateEdgeEffectsOnOverscroll(oldY, deltaY, scrollRangeY, view.getHeight(),
            edgeGlowTop, edgeGlowBottom);

        shouldInvalidate |= updateEdgeEffectsOnOverscroll(oldX, deltaX, scrollRangeX, view.getWidth(), edgeGlowLeft,
            edgeGlowRight);

        if (!shouldInvalidate) {
            view.postInvalidateOnAnimation();
        }
    }

    private boolean updateEdgeEffectsOnOverscroll(int old, int delta, int max, int viewDimension,
                                                  EdgeEffect zeroBoundary, EdgeEffect maxBoundary) {
        if (zeroBoundary == null) {
            return false;
        }

        final int newScroll = old + delta;
        if (newScroll < 0) {
            zeroBoundary.onPull((float) delta / viewDimension);
            if (!maxBoundary.isFinished()) {
                maxBoundary.onRelease();
            }
        } else if (newScroll > max) {
            maxBoundary.onPull((float) delta / viewDimension);
            if (!zeroBoundary.isFinished()) {
                zeroBoundary.onRelease();
            }
        }

        return !zeroBoundary.isFinished() || !maxBoundary.isFinished();
    }

    public void onScroll(int oldX, int oldY, int x, int y, int scrollRangeX, int scrollRangeY, OverScroller scroller) {
        if (disabled) {
            return;
        }

        int currentVelocity = (int) scroller.getCurrVelocity();
        updateEdgeEffectsIfReachScrollBoundary(oldY, y, scrollRangeY, edgeGlowTop, edgeGlowBottom, currentVelocity);
        updateEdgeEffectsIfReachScrollBoundary(oldX, x, scrollRangeX, edgeGlowLeft, edgeGlowRight, currentVelocity);
    }

    private void updateEdgeEffectsIfReachScrollBoundary(int old, int current, int max, EdgeEffect zeroBoundary,
                                                        EdgeEffect maxBoundary, int currentVelocity) {
        if (zeroBoundary == null) {
            return;
        }

        if (current < 0 && old >= 0) {
            zeroBoundary.onAbsorb(currentVelocity);
        } else if (current > max && old <= max) {
            maxBoundary.onAbsorb(currentVelocity);
        }
    }

    public void draw(Canvas canvas, int scrollRangeX, int scrollRangeY) {
        drawVerticalEdgesIfAnimationOngoing(canvas, scrollRangeY);
        drawHorizontalEdgesIfAnimationOngoing(canvas, scrollRangeX);
    }

    private void drawVerticalEdgesIfAnimationOngoing(Canvas canvas, int scrollRangeY) {
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

            canvas.translate(-width + view.getPaddingLeft(), Math.max(scrollRangeY, scrollY) + height);
            canvas.rotate(180, width, 0);

            edgeGlowBottom.setSize(width, height);
            if (edgeGlowBottom.draw(canvas)) {
                view.postInvalidateOnAnimation();
            }

            canvas.restoreToCount(restoreCount);
        }
    }

    // TODO: drawing left/right edges doesn't seem to work... it gets to the canvas bit, but nothing shows up
    //       at least in the emulator.
    private void drawHorizontalEdgesIfAnimationOngoing(Canvas canvas, int scrollRangeX) {
        if (edgeGlowLeft == null) {
            return;
        }

        final int width = view.getWidth();
        final int height = view.getHeight() - view.getPaddingBottom() - view.getPaddingTop();

        final int scrollX = view.getScrollX();
        if (!edgeGlowLeft.isFinished()) {
            final int restoreCount = canvas.save();

            canvas.rotate(270);
            canvas.translate(Math.min(0, scrollX), view.getPaddingTop());

            edgeGlowLeft.setSize(width, height);
            if (edgeGlowLeft.draw(canvas)) {
                view.postInvalidateOnAnimation(); // TODO: this can be called 4 times, i don't think that's necessary.
            }

            canvas.restoreToCount(restoreCount);
        }

        if (!edgeGlowRight.isFinished()) {
            final int restoreCount = canvas.save();

            canvas.translate(Math.max(scrollRangeX, scrollX) + width, -height + view.getPaddingTop());
            canvas.rotate(90, 0, height);

            edgeGlowRight.setSize(width, height);
            if (edgeGlowRight.draw(canvas)) {
                view.postInvalidateOnAnimation();
            }

            canvas.restoreToCount(restoreCount);
        }
    }
}
