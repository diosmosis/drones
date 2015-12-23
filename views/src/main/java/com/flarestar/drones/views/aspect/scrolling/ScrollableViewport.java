package com.flarestar.drones.views.aspect.scrolling;

import android.view.View;
import com.flarestar.drones.views.viewgroups.BoxModelNode;

/**
 * TODO
 */
public class ScrollableViewport {
    private int scrollRangeX;
    private int scrollRangeY;

    private boolean canOverscrollX;
    private boolean canOverscrollY;

    private int newScrollX;
    private int newScrollY;

    public void computeScrollRange(BoxModelNode view) {
        // determine scroll range based on new layout
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

        // determine if view can scroll based on new dimensions
        final int overscrollMode = view.getOverScrollMode();
        if (overscrollMode == View.OVER_SCROLL_ALWAYS) {
            canOverscrollX = true;
            canOverscrollY = true;
        } else if (overscrollMode == View.OVER_SCROLL_IF_CONTENT_SCROLLS) {
            canOverscrollX = scrollRangeX > 0;
            canOverscrollY = scrollRangeY > 0;
        }
    }

    public boolean scrollBy(BoxModelNode view, int deltaX, int deltaY, int maxOverScrollDeltaX, int maxOverScrollDeltaY) {
        newScrollX = view.getScrollX() + deltaX;
        if (!canOverscrollX) {
            maxOverScrollDeltaX = 0;
        }

        newScrollY = view.getScrollY() + deltaY;
        if (!canOverscrollY) {
            maxOverScrollDeltaY = 0;
        }

        // Clamp values if at the limits and record
        final int left = -maxOverScrollDeltaX;
        final int right = maxOverScrollDeltaX + scrollRangeX;
        final int top = -maxOverScrollDeltaY;
        final int bottom = maxOverScrollDeltaY + scrollRangeY;

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

        return clampedX || clampedY;
    }

    public int getNewScrollX() {
        return newScrollX;
    }

    public int getNewScrollY() {
        return newScrollY;
    }

    public boolean canOverscrollY() {
        return canOverscrollY;
    }

    public boolean canOverscrollX() {
        return canOverscrollX;
    }

    public int getScrollRangeX() {
        return scrollRangeX;
    }

    public int getScrollRangeY() {
        return scrollRangeY;
    }
}
