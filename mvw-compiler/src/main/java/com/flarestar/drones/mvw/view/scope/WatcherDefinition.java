package com.flarestar.drones.mvw.view.scope;

import com.flarestar.drones.views.scope.Watcher;

/**
 * TODO
 */
public class WatcherDefinition {
    private final Class<? extends Watcher> watcherClass;
    protected final String getWatchValueCode;
    protected final String onValueChangedCode;

    public WatcherDefinition(Class<? extends Watcher> watcherClass, String getWatchValueCode, String onValueChangedCode) {
        this.watcherClass = watcherClass;
        this.getWatchValueCode = getWatchValueCode;
        this.onValueChangedCode = onValueChangedCode;
    }

    public WatcherDefinition(String getWatchValueCode, String onValueChangedCode) {
        this(Watcher.class, getWatchValueCode, onValueChangedCode);
    }

    public Class<? extends Watcher> getWatcherClass() {
        return watcherClass;
    }

    public String getCodeForGetWatchValue() {
        return getWatchValueCode;
    }

    public String getCodeForOnValueChanged() {
        return onValueChangedCode;
    }
}
