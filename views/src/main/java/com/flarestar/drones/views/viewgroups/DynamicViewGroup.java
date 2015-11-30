package com.flarestar.drones.views.viewgroups;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import com.flarestar.drones.views.ViewFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO
 */
public abstract class DynamicViewGroup extends ScopedViewGroup {

    private boolean isDirty = false;

    /**
     * TODO
     */
    private List<ViewFactory> childDefinitions = new ArrayList<>();

    public DynamicViewGroup(Context context) {
        super(context);
    }

    public DynamicViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DynamicViewGroup(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    // TODO: automated tests should detect if memory leaks exist
    public void createChildren() {
        isDirty = false;

        if (childDefinitions.isEmpty()) {
            return;
        }

        removeAllViews();

        for (ViewFactory definition : childDefinitions) {
            definition.createViews(this);
        }
    }

    public void addChildDefinition(ViewFactory factory) {
        childDefinitions.add(factory);
    }

    public void markDirty() {
        isDirty = true;
    }

    public boolean isDirty() {
        return isDirty;
    }
}
