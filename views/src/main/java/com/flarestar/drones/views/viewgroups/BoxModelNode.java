package com.flarestar.drones.views.viewgroups;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

/**
 * TODO
 */
public abstract class BoxModelNode extends DynamicViewGroup {
    protected final static int UNSPECIFIED_MEASURE_SPEC = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);

    public static class Size {
        public static final int CONTEXT_AVAILABLE_SPACE = 0; // TODO: use an enum
        public static final int CONTEXT_MEASURED_WIDTH = 1;
        public static final int CONTEXT_MEASURED_HEIGHT = 2;

        public int amount = 0;
        public int context = 0;
        public int computed = 0;

        public Size(int amount, int context) {
            this.amount = amount;
            this.context = context;
        }

        public int compute(int contextAmount) {
            computed = (amount * contextAmount) / 100;
            return computed;
        }

        public boolean needsChildDimension() {
            return context == CONTEXT_MEASURED_HEIGHT || context == CONTEXT_MEASURED_WIDTH;
        }

        public int getContextValue(int availableSpace, View child) {
            switch (context) {
                case CONTEXT_AVAILABLE_SPACE:
                    return availableSpace;
                case CONTEXT_MEASURED_HEIGHT:
                    return child.getMeasuredHeight();
                case CONTEXT_MEASURED_WIDTH:
                    return child.getMeasuredWidth();
                default:
                    throw new RuntimeException("Unknown BoxModelNode.Size context: " + context);
            }
        }
    }

    public static class LayoutParams extends ViewGroup.LayoutParams {
        public Size boxWidth = null;
        public Size boxHeight = null;

        public Size marginTop = null;
        public Size marginBottom = null;
        public Size marginLeft = null;
        public Size marginRight = null;

        public Size paddingTop = null;
        public Size paddingBottom = null;
        public Size paddingLeft = null;
        public Size paddingRight = null;

        public LayoutParams() {
            super(WRAP_CONTENT, WRAP_CONTENT);
        }
    }

    public BoxModelNode(Context context) {
        super(context);
    }

    public BoxModelNode(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BoxModelNode(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    protected int getAvailableSize(int measureSpec, boolean shouldFill) {
        int mode = MeasureSpec.getMode(measureSpec);
        if (mode == MeasureSpec.EXACTLY || (mode == MeasureSpec.AT_MOST && shouldFill)) {
            return MeasureSpec.getSize(measureSpec);
        } else {
            return -1;
        }
    }

    protected int getBoxModelChildMeasureSpec(Size size, int availableSpace, View child, boolean isScrollEnabled) {
        if (size == null) {
            if (availableSpace < 0 || isScrollEnabled) {
                return UNSPECIFIED_MEASURE_SPEC;
            } else {
                return MeasureSpec.makeMeasureSpec(availableSpace, MeasureSpec.AT_MOST);
            }
        }

        int contextValue = size.getContextValue(availableSpace, child);
        if (contextValue < 0) {
            return UNSPECIFIED_MEASURE_SPEC;
        } else {
            return MeasureSpec.makeMeasureSpec(size.compute(contextValue), MeasureSpec.EXACTLY);
        }
    }

    protected LayoutParams getChildLayoutParams(View child) {
        ViewGroup.LayoutParams layoutParams = child.getLayoutParams();

        if (layoutParams instanceof LayoutParams) {
            return (LayoutParams)layoutParams;
        } else {
            return null;
        }
    }

    protected int getBoxModelSize(Size size, int availableSpace, View child) {
        if (size == null) {
            return 0;
        }

        int contextAmount = size.getContextValue(availableSpace, child);
        return size.compute(contextAmount);
    }

    protected int getComputedBoxModelSize(Size size) {
        if (size == null) {
            return 0;
        }

        return size.computed;
    }

    protected void measureBoxModelNodeChild(LayoutParams layoutParams, View child, int availableWidth, int availableHeight) {
        int childWidthMeasureSpec;
        int childHeightMeasureSpec;

        Size width = null;
        Size height = null;

        if (layoutParams != null) {
            if ((layoutParams.boxWidth != null && layoutParams.boxWidth.needsChildDimension())
                || (layoutParams.boxHeight != null && layoutParams.boxHeight.needsChildDimension())
                ) {
                measureChild(child, UNSPECIFIED_MEASURE_SPEC, UNSPECIFIED_MEASURE_SPEC);
            }

            width = layoutParams.boxWidth;
            height = layoutParams.boxHeight;
        }

        childWidthMeasureSpec = getBoxModelChildMeasureSpec(width, availableWidth, child, isHorizontalScrollBarEnabled());
        childHeightMeasureSpec = getBoxModelChildMeasureSpec(height, availableHeight, child, isVerticalScrollBarEnabled());

        child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
    }

    protected int computeChildWidthAdjustment(LayoutParams layoutParams, int availableSpace, View child) {
        int adjustment = 0;

        adjustment += getBoxModelSize(layoutParams.marginLeft, availableSpace, child);
        adjustment += getBoxModelSize(layoutParams.paddingLeft, availableSpace, child);
        adjustment += getBoxModelSize(layoutParams.paddingRight, availableSpace, child);
        adjustment += getBoxModelSize(layoutParams.marginRight, availableSpace, child);

        return adjustment;
    }

    protected int computeChildHeightAdjustment(LayoutParams layoutParams, int availableSpace, View child) {
        int adjustment = 0;

        adjustment += getBoxModelSize(layoutParams.marginTop, availableSpace, child);
        adjustment += getBoxModelSize(layoutParams.paddingTop, availableSpace, child);
        adjustment += getBoxModelSize(layoutParams.paddingBottom, availableSpace, child);
        adjustment += getBoxModelSize(layoutParams.marginBottom, availableSpace, child);

        return adjustment;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (getChildCount() != 0) {
            return;
        }

        ChildViewCreatorIterator it = viewCreationIterator();
        for (int i = 0; i < startViewIndex; ++i) {
            if (it.hasNext()) {
                it.next();
            } else {
                break;
            }
        }

        for (; it.hasNext(); it.next()) {
            View childView = it.makeView();
            addView(childView);
        }
    }
}
