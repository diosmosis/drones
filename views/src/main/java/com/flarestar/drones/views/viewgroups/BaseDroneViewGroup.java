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
    }

    public BaseDroneViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BaseDroneViewGroup(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
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

    // TODO: what about linking listeners for the root view? should be done in the LayoutBuilder.
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

    @Override
    public void addView(View child) {
        super.addView(child);

        linkListenersToScopeEvents(child);
    }

    @Override
    public void addView(View child, int index) {
        super.addView(child, index);

        linkListenersToScopeEvents(child);
    }

    @Override
    public void addView(View child, int width, int height) {
        super.addView(child, width, height);

        linkListenersToScopeEvents(child);
    }

    @Override
    public void addView(View child, LayoutParams params) {
        super.addView(child, params);

        linkListenersToScopeEvents(child);
    }

    @Override
    public void addView(View child, int index, LayoutParams params) {
        super.addView(child, index, params);

        linkListenersToScopeEvents(child);
    }

    @Override
    public void removeView(View view) {
        removeChildScope(view);

        super.removeView(view);
    }

    @Override
    public void removeViewAt(int index) {
        removeChildScope(getChildAt(index));

        super.removeViewAt(index);
    }

    @Override
    public void removeViews(int start, int count) {
        for (int i = 0; i != start + count; ++i) {
            removeChildScope(getChildAt(i));
        }

        super.removeViews(start, count);
    }

    @Override
    public void removeAllViews() {
        for (int i = 0; i != getChildCount(); ++i) {
            removeChildScope(getChildAt(i));
        }

        super.removeAllViews();
    }

    private void removeChildScope(View child) {
        if (scope == null) {
            return;
        }

        scope.detachChild(child);
    }
}
