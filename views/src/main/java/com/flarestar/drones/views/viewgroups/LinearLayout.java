package com.flarestar.drones.views.viewgroups;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

/**
 * TODO
 */
public class LinearLayout extends BoxModelNode {
    protected boolean isHorizontal = false;
    protected int startViewIndex = 0;
    private int aggregateChildHeight = 0;
    private int aggregateChildWidth = 0;

    public LinearLayout(Context context) {
        super(context);
    }

    public LinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (getChildCount() == 0) {
            ChildViewCreatorIterator it = viewCreationIterator();
            for (int i = 0; i < startViewIndex; ++i) {
                if (it.hasNext()) {
                    it.next();
                } else {
                    break;
                }
            }

            for (; it.hasNext(); it.next()) {
                View child = it.makeView();
                addView(child);
            }
        }

        final int count = getChildCount();

        /* if no available width/height, we can't do scrolling, so throw exception in this case.
         * if available width is not -1, then we can go bit by bit. so, let's make some changes:
         *
         * - if availableWidth == -1
         */

        int availableWidth = getAvailableSize(widthMeasureSpec, isHorizontal);
        int availableHeight = getAvailableSize(heightMeasureSpec, !isHorizontal);

        int unadjustedAggregateWidth = 0;
        int unadjustedAggregateHeight = 0;

        // first pass, we measure each child
        int remainingAvailableWidth = availableWidth;
        int remainingAvailableHeight = availableHeight;
        for (int i = 0; i != count; ++i) {
            final View child = getChildAt(i);

            if (child.getVisibility() == View.GONE) {
                continue;
            }

            BoxModelNode.LayoutParams layoutParams = getChildLayoutParams(child);
            measureBoxModelNodeChild(layoutParams, child, remainingAvailableWidth, remainingAvailableHeight);

            if (isHorizontal) {
                unadjustedAggregateWidth += child.getMeasuredWidth();
                remainingAvailableWidth -= child.getMeasuredWidth();

                unadjustedAggregateHeight = Math.max(unadjustedAggregateHeight, child.getMeasuredHeight());
            } else {
                unadjustedAggregateHeight += child.getMeasuredHeight();
                remainingAvailableHeight -= child.getMeasuredHeight();

                unadjustedAggregateWidth = Math.max(unadjustedAggregateWidth, child.getMeasuredWidth());
            }
        }

        aggregateChildHeight = unadjustedAggregateHeight;
        aggregateChildWidth = unadjustedAggregateWidth;

        // second pass, we determine the margin & padding of each child
        for (int i = 0; i != count; ++i) {
            final View child = getChildAt(i);

            if (child.getVisibility() == View.GONE) {
                continue;
            }

            BoxModelNode.LayoutParams layoutParams = getChildLayoutParams(child);
            if (layoutParams == null) {
                continue;
            }

            // TODO: for now we do not allow margin/padding to affect the available width/height. maybe it would be useful
            //       to implement in the future, but it would be rather complicated.
            if (isHorizontal) {
                int extraAvailableWidth = availableWidth == -1 ? 0 : (availableWidth - unadjustedAggregateWidth);
                int extraAvailableHeight = (availableHeight == -1 ? unadjustedAggregateHeight : availableHeight) - child.getMeasuredHeight();

                int childHeightAdjustment = computeChildHeightAdjustment(layoutParams, extraAvailableHeight, child);
                int childWidthAdjustment = computeChildWidthAdjustment(layoutParams, extraAvailableWidth, child);

                aggregateChildWidth += childWidthAdjustment;
                aggregateChildHeight = Math.max(aggregateChildHeight, childHeightAdjustment + child.getMeasuredHeight());
            } else {
                int extraAvailableWidth = (availableWidth == -1 ? unadjustedAggregateWidth : availableWidth) - child.getMeasuredWidth();
                int extraAvailableHeight = availableHeight == -1 ? 0 : (availableHeight - unadjustedAggregateHeight);

                int childHeightAdjustment = computeChildHeightAdjustment(layoutParams, extraAvailableHeight, child);
                int childWidthAdjustment = computeChildWidthAdjustment(layoutParams, extraAvailableWidth, child);

                aggregateChildWidth = Math.max(aggregateChildWidth, childWidthAdjustment + child.getMeasuredWidth());
                aggregateChildHeight += childHeightAdjustment;
            }
        }

        int width = availableWidth == -1 ? aggregateChildWidth : availableWidth;
        int height = availableHeight == -1 ? aggregateChildHeight : availableHeight;

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        final int count = getChildCount();

        int currentChildTop = 0;
        int currentChildLeft = 0;

        for (int i = 0; i != count; ++i) {
            final View child = getChildAt(i);

            if (child.getVisibility() == View.GONE) {
                continue;
            }

            BoxModelNode.LayoutParams layoutParams = getChildLayoutParams(child);

            int left = currentChildLeft;
            int top = currentChildTop;

            if (layoutParams != null) {
                left += getComputedBoxModelSize(layoutParams.marginLeft) + getComputedBoxModelSize(layoutParams.paddingLeft);
                top += getComputedBoxModelSize(layoutParams.marginTop) + getComputedBoxModelSize(layoutParams.paddingTop);
            }

            final int bottom = top + child.getMeasuredHeight();
            final int right = left + child.getMeasuredWidth();

            // TODO: how do we handle background color for padding?

            child.layout(left, top, right, bottom);

            if (isHorizontal) {
                currentChildLeft = right;

                if (layoutParams != null) {
                    currentChildLeft += getComputedBoxModelSize(layoutParams.paddingRight) + getComputedBoxModelSize(layoutParams.marginRight);
                }
            } else {
                currentChildTop = bottom;

                if (layoutParams != null) {
                    currentChildTop += getComputedBoxModelSize(layoutParams.paddingBottom) + getComputedBoxModelSize(layoutParams.marginBottom);
                }
            }
        }

        // TODO: is this necessary? old comment:
        // Calling this with the present values causes it to re-claim them
        if (isHorizontalScrollBarEnabled() || isVerticalScrollBarEnabled()) {
            scrollTo(getScrollX(), getScrollY());
        }
    }

    @Override
    public int getAggregateChildHeight() {
        return aggregateChildHeight;
    }

    @Override
    public int getAggregateChildWidth() {
        return aggregateChildWidth;
    }
}
