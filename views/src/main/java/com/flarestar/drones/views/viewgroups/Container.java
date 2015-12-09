
package com.flarestar.drones.views.viewgroups;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

// TODO: profile all drones after this all works.
/**
 * CHANGES:
 * - removing getTopFadingEdgeStrength() and getBottomFadingEdgeStrength(). neither seem to be used in
 *   android source code. though it's possible for vendors to use them? code is still in ScrollingAspect.
 */
public class Container extends BoxModelNode {
    public Container(Context context) {
        super(context);
    }

    public Container(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    // TODO: should return true if scrolling enabled (ie, enabled + child size is greater than this size). can be put in BoxModelNode
    @Override
    public boolean shouldDelayChildPressedState() {
        return true;
    }

    @Override
    public void addView(View child) {
        throwIfAddingMoreThanOneView();
        super.addView(child);
    }

    @Override
    public void addView(View child, int index) {
        throwIfAddingMoreThanOneView();
        super.addView(child, index);
    }

    @Override
    public void addView(View child, ViewGroup.LayoutParams params) {
        throwIfAddingMoreThanOneView();
        super.addView(child, params);
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        throwIfAddingMoreThanOneView();
        super.addView(child, index, params);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int availableWidth = getAvailableSize(widthMeasureSpec, true);
        int availableHeight = getAvailableSize(heightMeasureSpec, true);

        int width = availableWidth;
        int height = availableHeight;

        final View child = getChildAt(0);

        if (child.getVisibility() != View.GONE) {
            BoxModelNode.LayoutParams layoutParams = getChildLayoutParams(child);
            measureBoxModelNodeChild(layoutParams, child, availableWidth, availableHeight);

            final int totalAvailableHeight = availableHeight == -1 ? 0 : availableHeight;
            final int totalAvailableWidth = availableWidth == -1 ? 0 : availableWidth;

            int childHeightAdjustment = 0;
            int childWidthAdjustment = 0;

            if (layoutParams != null) {
                childHeightAdjustment = computeChildHeightAdjustment(layoutParams, totalAvailableHeight, child);
                childWidthAdjustment = computeChildWidthAdjustment(layoutParams, totalAvailableWidth, child);
            }

            if (availableWidth == -1) {
                width = child.getMeasuredWidth() + childWidthAdjustment;
            }

            if (availableHeight == -1) {
                height = child.getMeasuredHeight() + childHeightAdjustment;
            }
        }

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        final View child = getChildAt(0);

        if (child.getVisibility() != View.GONE) {
            int top = 0;
            int left = 0;

            BoxModelNode.LayoutParams layoutParams = getChildLayoutParams(child);
            if (layoutParams != null) {
                top += getComputedBoxModelSize(layoutParams.marginTop) + getComputedBoxModelSize(layoutParams.paddingTop);
                left += getComputedBoxModelSize(layoutParams.marginLeft) + getComputedBoxModelSize(layoutParams.paddingLeft);
            }

            child.layout(top, left, child.getMeasuredWidth(), child.getMeasuredHeight());
        }

        // TODO: is this necessary? old comment:
        // Calling this with the present values causes it to re-claim them
        if (isHorizontalScrollBarEnabled() || isVerticalScrollBarEnabled()) {
            scrollTo(getScrollX(), getScrollY());
        }
    }

    private void throwIfAddingMoreThanOneView() {
        if (getChildCount() > 0) {
            throw new IllegalStateException("Container can host only one direct child");
        }
    }

    public int getAggregateChildHeight() {
        return getChildAt(0).getHeight();
    }

    public int getAggregateChildWidth() {
        return getChildAt(0).getWidth();
    }
}
