package com.flarestar.drones.views.viewgroups;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import com.flarestar.drones.base.BaseScreen;
import com.flarestar.drones.base.Drone;
import com.flarestar.drones.views.LayoutBuilder;
import com.flarestar.drones.views.ViewRecycler;
import com.flarestar.drones.views.scope.Scope;
import com.flarestar.drones.views.scope.events.Click;

public abstract class ScopedViewGroup extends ViewGroup {
    @Override
    public int hashCode() {
        return super.hashCode();
    }

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
        final ViewRecycler recycler = findViewRecycler();
        OnHierarchyChangeListener listener = new OnHierarchyChangeListener() {
            @Override
            public void onChildViewAdded(View parent, View child) {
                // TODO: what about the root view? should be done in the LayoutBuilder.
                linkListenersToScopeEvents(child);
            }

            @Override
            public void onChildViewRemoved(View parent, View child) {
                Scope<?> scope = ((ScopedViewGroup)parent).scope;
                if (scope == null) {
                    return;
                }

                scope.detachChild(child);

                recycler.recycleView(child);
            }
        };

        setOnHierarchyChangeListener(listener);
    }

    private ViewRecycler findViewRecycler() {
        Context context = getContext();
        for (Drone drone : ((BaseScreen)context).drones()) {
            if (drone instanceof LayoutBuilder) {
                return ((LayoutBuilder)drone).getViewRecycler();
            }
        }

        throw new RuntimeException("Unexpected state: could not find LayoutBuilder");
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
}
