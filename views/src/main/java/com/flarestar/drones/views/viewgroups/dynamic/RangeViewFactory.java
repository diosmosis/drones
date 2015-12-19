package com.flarestar.drones.views.viewgroups.dynamic;

import android.view.View;
import android.view.ViewGroup;
import com.flarestar.drones.views.ViewFactory;
import com.flarestar.drones.views.scope.Scope;

import java.util.Iterator;

public abstract class RangeViewFactory<V> implements ViewFactory {

    // TODO: keeping track of the start/end child view index probably shouldn't be here, but if it's not
    //       here, we'll have to create more small objects in Views.
    private View startView;
    private View endView;

    public abstract View makeView(final V _item, final int _index);

    public abstract Iterable<V> getCollection();

    public abstract V getItem(Scope<?> scope);

    public abstract void setScopeProperties(Scope<?> scope, V item, int index);

    public View getStartView() {
        return startView;
    }

    public View getEndView() {
        return endView;
    }

    public void setEndView(View endView) {
        this.endView = endView;
    }

    public void setStartView(View startView) {
        this.startView = startView;
    }

    @Override
    public void makeViews(ViewGroup parent) {
        final int startViewIndex = parent.getChildCount();

        Iterable<V> collection = getCollection();

        int index = 0;
        for (V value : collection) {
            View view = makeView(value, index);
            parent.addView(view);

            ++index;
        }

        startView = parent.getChildAt(startViewIndex);
        endView = parent.getChildAt(parent.getChildCount() - 1);
    }
}
