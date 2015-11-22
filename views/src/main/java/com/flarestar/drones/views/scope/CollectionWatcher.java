package com.flarestar.drones.views.scope;

import java.lang.reflect.Array;
import java.util.Collection;

// TODO: will this work w/ Maps? need to test
// TODO: this doesn't count # number of changes. i think the angular code does. not sure if it's important.
public abstract class CollectionWatcher extends Watcher {
    @Override
    public void setLastValue(Object lastValue) {
        if (lastValue == null) {
            this.lastValue = null;
        } else if (lastValue instanceof Collection) {
            Collection collection = (Collection) lastValue;
            this.lastValue = collection.toArray(new Object[collection.size()]);
        } else if (lastValue.getClass().isArray()) {
            Object[] copy = new Object[Array.getLength(lastValue)];
            for (int i = 0; i != copy.length; ++i) {
                copy[i] = Array.get(lastValue, i);
            }
            this.lastValue = copy;
        } else {
            throw new RuntimeException("Unexpected error, invalid type in CollectionWatcher: "
                + lastValue.getClass().getName() + " (" + lastValue.toString() + ")");
        }
    }

    @Override
    public boolean areValuesEqual(Object newValue, Object lastValue) {
        if (lastValue == Watcher.INITIAL_VALUE) {
            return false;
        }

        if (newValue == lastValue) {
            return true;
        }

        if (newValue == null || lastValue == null) {
            return false;
        }

        Object[] lastValueArray = (Object[])lastValue;
        if (newValue instanceof Collection) {
            return isCollectionEqualTo((Collection)newValue, lastValueArray);
        } else if (newValue.getClass().isArray()) {
            return isArrayEqualTo(newValue, lastValueArray);
        } else {
            throw new RuntimeException("Unexpected error, invalid new value type in CollectionWatcher: "
                + newValue.getClass().getName());
        }
    }

    private boolean isArrayEqualTo(Object newValue, Object[] lastValue) {
        if (Array.getLength(newValue) != lastValue.length) {
            return false;
        }

        for (int i = 0; i != lastValue.length; ++i) {
            if (!super.areValuesEqual(Array.get(newValue, i), lastValue[i])) {
                return false;
            }
        }

        return true;
    }

    private boolean isCollectionEqualTo(Collection newValue, Object[] lastValue) {
        if (newValue.size() != lastValue.length) {
            return false;
        }

        int i = 0;
        for (Object item : newValue) {
            if (!super.areValuesEqual(item, lastValue[i])) {
                return false;
            }

            ++i;
        }

        return true;
    }
}
