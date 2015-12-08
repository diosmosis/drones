package com.flarestar.drones.views.viewgroups;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import com.flarestar.drones.views.scope.Scope;
import com.flarestar.drones.views.scope.events.Click;

public abstract class BaseDroneViewGroup extends ViewGroup {

    protected Scope<?> scope = null;

    public BaseDroneViewGroup(Context context) {
        super(context);

        setViewRemovalListener();
    }

    public BaseDroneViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs);

        setViewRemovalListener();
    }

    public BaseDroneViewGroup(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        setViewRemovalListener();
    }

    private void setViewRemovalListener() {
        OnHierarchyChangeListener listener = new OnHierarchyChangeListener() {
            @Override
            public void onChildViewAdded(View parent, View child) {
                // TODO: what about the root view? should be done in the LayoutBuilder.
                linkListenersToScopeEvents(child);
            }

            @Override
            public void onChildViewRemoved(View parent, View child) {
                Scope<?> scope = ((BaseDroneViewGroup)parent).scope;
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

    private void linkListenersToScopeEvents(final View child) {
        // TODO: link all other listeners
        child.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Scope<?> childViewScope = scope.getChildScopeFor(child);
                if (childViewScope == null) {
                    return; // TODO: should we log here?
                }

                childViewScope.emit(new Click(view));
            }
        });
    }

    // TODO: following are methods made public so we can abstract some view logic into another class. it might be possible
    //       to do this another way though, by creating new public methods that call these protected methods. the public
    //       methods would have real purpose (ie, like `scrollTo(...)`.
    @Override
    public void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
    }

    @Override
    public boolean awakenScrollBars() {
        return super.awakenScrollBars();
    }
}
