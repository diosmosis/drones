package com.flarestar.drones.views.scope;

import com.flarestar.drones.views.Scope;

import java.io.*;

/**
 * TODO
 */
public abstract class Watcher {

    public final static Object INITIAL_VALUE = new Object();

    /**
     * TODO
     */
    private Object lastValue = INITIAL_VALUE;

    /**
     * TODO
     */
    private boolean useValueComparison = false;

    public Watcher() {
        // empty
    }

    public Watcher(boolean useValueComparison) {
        this.useValueComparison = useValueComparison;
    }

    /**
     * TODO
     *
     * @param scope
     * @return
     */
    public abstract Object getWatchValue(Scope<?> scope);

    /**
     * TODO
     *
     * @param newValue
     * @param oldValue
     */
    public abstract void onValueChanged(Object newValue, Object oldValue, Scope<?> scope);

    /**
     * TODO
     *
     * @return
     */
    public Object getLastValue() {
        return lastValue;
    }

    /**
     * TODO
     *
     * @param lastValue
     */
    public void setLastValue(Object lastValue) {
        if (useValueComparison) {
            this.lastValue = getClone(lastValue);
        } else {
            this.lastValue = lastValue;
        }
    }

    // TODO: should be in a helper class. unless android includes something that does this.
    private Object getClone(Object value) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();

            ObjectOutputStream out = new ObjectOutputStream(bos);
            out.writeObject(value);
            out.flush();
            out.close();

            ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());

            ObjectInputStream in = new ObjectInputStream(bis);
            return in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Unexpected error", e);
        }
    }

    /**
     * TODO
     *
     * @param newValue
     * @param lastValue
     * @return
     */
    public boolean areValuesEqual(Object newValue, Object lastValue) {
        if (newValue == lastValue) {
            return true;
        }

        if (newValue == null || lastValue == null) {
            return false;
        }

        if (useValueComparison || newValue.getClass().isPrimitive() || newValue instanceof String) {
            return !newValue.equals(lastValue);
        } else {
            return false;
        }
    }
}
