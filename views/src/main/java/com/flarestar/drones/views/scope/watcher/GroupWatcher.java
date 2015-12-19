package com.flarestar.drones.views.scope.watcher;

import com.flarestar.drones.views.scope.Scope;

public abstract class GroupWatcher extends Watcher {
    @Override
    final public Object getWatchValue(Scope<?> scope) {
        return getWatchValues(scope);
    }

    @Override
    final public void onValueChanged(Object newValue, Object oldValue, Scope<?> scope) {
        onValuesChanged((Object[])newValue, (Object[])oldValue, scope);
    }

    @Override
    public boolean areValuesEqual(Object newValue, Object lastValue) {
        Object[] newValueArray = (Object[])newValue;
        Object[] lastValueArray = (Object[])lastValue;

        if (newValueArray.length != lastValueArray.length) {
            return false;
        }

        for (int i = 0; i != newValueArray.length; ++i) {
            if (!super.areValuesEqual(newValueArray[i], lastValueArray[i])) {
                return false;
            }
        }

        return true;
    }

    protected abstract Object[] getWatchValues(Scope<?> scope);
    protected abstract void onValuesChanged(Object[] newValues, Object[] oldValues, Scope<?> scope);
}
