package com.flarestar.drones.mvw.processing.renderables.scope;

import com.flarestar.drones.base.generation.Renderable;
import com.flarestar.drones.views.scope.watcher.Watcher;

/**
 * TODO
 */
public class WatcherDefinition implements Renderable {
    private final Class<? extends Watcher> watcherClass;
    protected final String getWatchValueCode;
    protected final String onValueChangedCode;
    private final boolean isOnParentScope;

    public WatcherDefinition(Class<? extends Watcher> watcherClass, String getWatchValueCode, String onValueChangedCode,
                             boolean isOnParentScope) {
        this.watcherClass = watcherClass;
        this.getWatchValueCode = getWatchValueCode;
        this.onValueChangedCode = onValueChangedCode;
        this.isOnParentScope = isOnParentScope;
    }

    public WatcherDefinition(Class<? extends Watcher> watcherClass, String getWatchValueCode, String onValueChangedCode) {
        this(watcherClass, getWatchValueCode, onValueChangedCode, false);
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

    public boolean isOnParentScope() {
        return isOnParentScope;
    }

    @Override
    public String getTemplate() {
        return "templates/watcher.twig";
    }

    @Override
    public String getModelAttribute() {
        return "watcher";
    }
}
