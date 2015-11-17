
package com.flarestar.drones.views;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * TODO
 */
public class Container extends BoxModelNode {
    public Container(Context context) {
        super(context);
    }

    public Container(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public Container(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int availableWidth = getAvailableSize(widthMeasureSpec, true);
        int availableHeight = getAvailableSize(heightMeasureSpec, true);

        int width = availableWidth;
        int height = availableHeight;

        final View child = getChildAt(0);

        if (child.getVisibility() != GONE) {
            BoxModelNode.LayoutParams layoutParams = getChildLayoutParams(child);
            measureBoxModelNodeChild(layoutParams, child, availableWidth, availableHeight);

            final int extraAvailableHeight = availableHeight == -1 ? 0 : availableHeight;
            final int extraAvailableWidth = availableWidth == -1 ? 0 : availableWidth;

            int childHeightAdjustment = 0;
            int childWidthAdjustment = 0;

            if (layoutParams != null) {
                childHeightAdjustment = getChildHeightAdjustment(layoutParams, extraAvailableHeight, child);
                childWidthAdjustment = getChildWidthAdjustment(layoutParams, extraAvailableWidth, child);
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
        final View child = getChildAt(0);

        if (child.getVisibility() == GONE) {
            return;
        }

        int top = 0;
        int left = 0;

        BoxModelNode.LayoutParams layoutParams = getChildLayoutParams(child);
        if (layoutParams != null) {
            top += getComputedBoxModelSize(layoutParams.marginTop) + getComputedBoxModelSize(layoutParams.paddingTop);
            left += getComputedBoxModelSize(layoutParams.marginLeft) + getComputedBoxModelSize(layoutParams.paddingLeft);
        }

        child.layout(top, left, child.getMeasuredWidth(), child.getMeasuredHeight());
    }

    @Override
    public void addView(View child) {
        if (getChildCount() > 1) {
            throw new RuntimeException("Trying to add more than one child view to Container!");
        }

        super.addView(child);
    }
}
