package com.flarestar.drones.views.viewgroups;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import com.flarestar.drones.views.ViewFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * TODO
 */
public abstract class DynamicViewGroup extends BaseDroneViewGroup {

    /**
     * TODO
     */
    protected List<ViewFactory> childDefinitions = new ArrayList<>();

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

    public void addChildDefinition(ViewFactory factory) {
        childDefinitions.add(factory);
    }

    @Override
    public void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
    }

    public void moveView(int from, int to) {
        View view = getChildAt(from);
        removeViewAt(from);
        addView(view, to);
    }

    public void createChildren() {
        for (ViewFactory factory : childDefinitions) {
            factory.makeViews(this);
        }
    }
}
