package com.flarestar.drones.views.viewgroups.dynamic;

import android.view.View;
import com.flarestar.drones.views.ViewFactory;

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
            return RangeViewFactory.this.makeView(currentValue, index);
        }
    }

    protected abstract View makeView(V currentValue, final int index);

    protected abstract Iterable<V> getCollection();

    @Override
    public ViewFactory.Iterator iterator() {
        Iterable<V> collection = getCollection();
        return new Iterator(collection.iterator());
    }
}
