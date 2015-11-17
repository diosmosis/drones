package com.flarestar.drones.views;

/**
 * TODO
 */
public class Scope {

    public interface Watcher {
        // TODO
    }

    public interface Runnable {
        // TODO
    }

    public final Scope _parent;

    public Scope() {
        _parent = null;
    }

    public Scope(Scope parent) {
        _parent = parent;
    }

    public void watch(Watcher watcher) {
        // TODO
    }

    public void unwatch(Watcher watcher) {
        // TODO
    }

    public void digest() {
        // TODO
    }

    public void eval(Runnable runnable) {
        // TODO
    }

    public void apply(Runnable runnable) {
        // TODO
    }

    public void evalAsync(Runnable runnable) {
        // TODO
    }

    public void postDigest(Runnable runnable) {
        // TODO
    }
}
