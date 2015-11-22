package com.flarestar.drones.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

public abstract class ScopedViewGroup extends ViewGroup {

    protected Scope<?> scope = null;

    public ScopedViewGroup(Context context) {
        super(context);

        setViewRemovalListener();
    }

    public ScopedViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs);

        setViewRemovalListener();
    }

    public ScopedViewGroup(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        setViewRemovalListener();
    }

    private void setViewRemovalListener() {
        OnHierarchyChangeListener listener = new OnHierarchyChangeListener() {
            @Override
            public void onChildViewAdded(View view, View view1) {
                // empty
            }

            @Override
            public void onChildViewRemoved(View parent, View child) {
                Scope<?> scope = ((ScopedViewGroup)parent).scope;
                if (scope == null) {
                    return;
                }

                scope.detachChild(child);
            }
        };

        setOnHierarchyChangeListener(listener);
    }

    public Scope<?> getScope() {
        return scope;
    }

    public void setScope(Scope<?> scope) {
        if (this.scope != null) {
            throw new IllegalStateException("Unexpected state: scope is already set for this ScopedViewGroup.");
        }

        this.scope = scope;
    }
}
