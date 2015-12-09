package com.flarestar.drones.views.viewgroups.dynamic;

import android.view.View;
import android.view.ViewGroup;
import com.flarestar.drones.views.ViewFactory;
import com.flarestar.drones.views.scope.Scope;

public abstract class RangeViewFactory<V> implements ViewFactory {

    public class Iterator implements ViewFactory.Iterator {
        private final java.util.Iterator<V> valueIterator;
        private V currentValue = null;
        private int index = 0;

        public Iterator(java.util.Iterator<V> valueIterator) {
            this.valueIterator = valueIterator;

            if (this.valueIterator.hasNext()) {
                currentValue = this.valueIterator.next();
            }
        }

        @Override
        public boolean hasNext() {
            return currentValue != null;
        }

        @Override
        public void next() {
            if (valueIterator.hasNext()) {
                currentValue = valueIterator.next();
            } else {
                currentValue = null;
            }

            ++index;
        }

        @Override
        public View makeView() {
            View view = RangeViewFactory.this.makeView(currentValue, index);
            if (startView == null) {
                startView = view;
            }

            endView = view;
            return view;
        }
    }

    // TODO: keeping track of the start/end child view index probably shouldn't be here, but if it's not
    //       here, we'll have to create more objects in Views.
    private View startView;
    private View endView;

    public abstract View makeView(final V _item, final int _index);

    public abstract Iterable<V> getCollection();

    public abstract V getItem(Scope<?> scope);

    public abstract void setScopeProperties(Scope<?> scope, int index);

    @Override
    public ViewFactory.Iterator iterator(ViewGroup parent) {
        Iterable<V> collection = getCollection();
        return new Iterator(collection.iterator());
    }

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
}
