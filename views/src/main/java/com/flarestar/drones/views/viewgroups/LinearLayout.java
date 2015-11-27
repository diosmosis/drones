package com.flarestar.drones.views.viewgroups;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import com.flarestar.drones.views.viewgroups.BoxModelNode;

/**
 * TODO
 */
public class LinearLayout extends BoxModelNode {
    protected boolean isHorizontal = false;

    public LinearLayout(Context context) {
        super(context);
    }

    public LinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int count = getChildCount();

        int availableWidth = getAvailableSize(widthMeasureSpec, isHorizontal);
        int availableHeight = getAvailableSize(heightMeasureSpec, !isHorizontal);

        int aggregateWidth = 0;
        int aggregateHeight = 0;

        // first pass, we measure each child
        for (int i = 0; i != count; ++i) {
            final View child = getChildAt(i);

            if (child.getVisibility() == View.GONE) {
                continue;
            }

            BoxModelNode.LayoutParams layoutParams = getChildLayoutParams(child);
            measureBoxModelNodeChild(layoutParams, child, availableWidth, availableHeight);

            if (isHorizontal) {
                aggregateWidth += child.getMeasuredWidth();
                aggregateHeight = Math.max(aggregateHeight, child.getMeasuredHeight());
            } else {
                aggregateWidth = Math.max(aggregateWidth, child.getMeasuredWidth());
                aggregateHeight += child.getMeasuredHeight();
            }
        }

        int adjustedAggregateWidth = aggregateWidth;
        int adjustedAggregateHeight = aggregateHeight;

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
            int extraAvailableWidth;
            int extraAvailableHeight;

            if (isHorizontal) {
                extraAvailableWidth = availableWidth == -1 ? 0 : (availableWidth - aggregateWidth);
                extraAvailableHeight = (availableHeight == -1 ? aggregateHeight : availableHeight) - child.getMeasuredHeight();

                int childHeightAdjustment = getChildHeightAdjustment(layoutParams, extraAvailableHeight, child);
                int childWidthAdjustment = getChildWidthAdjustment(layoutParams, extraAvailableWidth, child);

                adjustedAggregateWidth += childWidthAdjustment;
                adjustedAggregateHeight = Math.max(adjustedAggregateHeight, childHeightAdjustment + child.getMeasuredHeight());
            } else {
                extraAvailableWidth = (availableWidth == -1 ? aggregateWidth : availableWidth) - child.getMeasuredWidth();
                extraAvailableHeight = availableHeight == -1 ? 0 : (availableHeight - aggregateHeight);

                int childHeightAdjustment = getChildHeightAdjustment(layoutParams, extraAvailableHeight, child);
                int childWidthAdjustment = getChildWidthAdjustment(layoutParams, extraAvailableWidth, child);

                adjustedAggregateWidth = Math.max(adjustedAggregateWidth, childWidthAdjustment + child.getMeasuredWidth());
                adjustedAggregateHeight += childHeightAdjustment;
            }
        }

        int width = availableWidth == -1 ? adjustedAggregateWidth : availableWidth;
        int height = availableHeight == -1 ? adjustedAggregateHeight : availableHeight;

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
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
    }
}
