package com.flarestar.drones.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO
 */
public abstract class DynamicViewGroup extends ViewGroup {

    /**
     * TODO
     *
     * holds either Views or ViewFactory's
     */
    private List<Object> childDefinitions = new ArrayList<>();

    private boolean childrenDirty = false;

    public DynamicViewGroup(Context context) {
        super(context);
    }

    public DynamicViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DynamicViewGroup(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public boolean isDirty() {
        return childrenDirty;
    }

    public void markAsDirty() {
        childrenDirty = true;

        ViewParent parent = getParent();
        if (parent instanceof DynamicViewGroup) {
            ((DynamicViewGroup) parent).markAsDirty();
        }
    }

    public void createChildren() {
        childrenDirty = false;

        if (childDefinitions.isEmpty()) {
            return;
        }

        removeAllViews();

        for (Object definition : childDefinitions) {
            if (definition instanceof View) {
                addView((View)definition);
            } else if (definition instanceof ViewFactory) {
                ((ViewFactory)definition).createViews(this);
            } else {
                throw new IllegalStateException("Unexpected child definition type: " + definition.getClass().getName());
            }
        }
    }

    public void addChildDefinition(View child) {
        childDefinitions.add(child);
    }

    public void addChildDefinition(ViewFactory factory) {
        childDefinitions.add(factory);
    }
}
