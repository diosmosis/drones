package com.flarestar.drones.views;

import android.os.Handler;
import com.flarestar.drones.views.scope.Watcher;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * TODO
 */
public class Scope<P extends Scope> {

    public static class DigestTtlExceededException extends RuntimeException {
        public DigestTtlExceededException(String message) {
            super(message);
        }
    }

    public static class PhaseAlreadyInProgressException extends RuntimeException {
        public PhaseAlreadyInProgressException(String message) {
            super(message);
        }
    }

    /**
     * TODO
     */
    public interface Runnable {
        /**
         * TODO
         *
         * @param scope
         * @return
         */
        Object run(Scope<?> scope);
    }

    private class QueuedRunnable implements java.lang.Runnable {
        public final Scope<?> scope;
        public final Runnable runnable;

        public QueuedRunnable(Scope<?> scope, Runnable runnable) {
            this.scope = scope;
            this.runnable = runnable;
        }

        @Override
        public void run() {
            scope.eval(runnable);
        }
    }

    public final int DIGEST_TTL = 10;

    // TODO: use same naming as angular ie, $$ prefix
    private List<Watcher> _watchers = new ArrayList<>();
    private LinkedList<QueuedRunnable> _asyncQueue = new LinkedList<>();
    private LinkedList<java.lang.Runnable> _postDigestQueue = new LinkedList<>();
    private String _phase;

    public final P _parent;
    public final Handler _handler;

    public Scope(Handler handler) {
        _parent = null;
        _handler = handler;
    }

    public Scope(Handler handler, P parent) {
        _parent = parent;
        _handler = handler;
    }

    public void watch(Watcher watcher) {
        _watchers.add(watcher);
    }

    public void unwatch(Watcher watcher) {
        _watchers.remove(watcher);
    }

    public void digest() {
        beginPhase("$digest");

        int ttlCounter = DIGEST_TTL;

        Watcher lastDirtyWatcher = null;
        do {
            consumeAsyncQueue();

            lastDirtyWatcher = checkWatchers(lastDirtyWatcher);

            if (lastDirtyWatcher != null || !_asyncQueue.isEmpty()) {
                --ttlCounter;
                if (ttlCounter <= 0) {
                    throw new DigestTtlExceededException("Maximum number of digest iterations (" + DIGEST_TTL +
                        ") reached.");
                }
            }
        } while (lastDirtyWatcher != null || !_asyncQueue.isEmpty());

        clearPhase();

        consumePostDigestQueue();
    }

    // TODO: eval doesn't really make sense for an android implementation, maybe we can remove it
    public Object eval(Runnable runnable) {
        return runnable.run(this);
    }

    public Object apply(Runnable runnable) {
        try {
            beginPhase("$apply");
            return eval(runnable);
        } finally {
            clearPhase();
            digest();
        }
    }

    public void evalAsync(Runnable runnable) {
        if (_phase == null && _asyncQueue.isEmpty()) {
            _handler.post(new java.lang.Runnable() {
                @Override
                public void run() {
                    if (!_asyncQueue.isEmpty()) {
                        digest();
                    }
                }
            });
        }

        _asyncQueue.add(new QueuedRunnable(this, runnable));
    }

    // TODO: not implementing applyAsync right now. not sure if it's necessary.

    public void postDigest(java.lang.Runnable runnable) {
        _postDigestQueue.add(runnable);
    }

    private void beginPhase(String phase) {
        if (_phase != null) {
            throw new PhaseAlreadyInProgressException("Phase '" + phase + "' is already in progress.");
        }
        _phase = phase;
    }

    private void clearPhase() {
        _phase = null;
    }

    // TODO: angular catches & logs exceptions w/o aborting a digest. should do something similar, but
    // logging in android is not as noticeable as in the browser.
    private Watcher checkWatchers(Watcher lastDirtyWatcher) {
        for (Watcher watcher : _watchers) {
            Object newValue = watcher.getWatchValue(this);
            Object lastValue = watcher.getLastValue();
            if (!watcher.areValuesEqual(newValue, lastValue)) {
                lastDirtyWatcher = watcher;

                watcher.setLastValue(lastValue);
                watcher.onValueChanged(newValue, lastValue == Watcher.INITIAL_VALUE ? newValue : lastValue, this);
            } else if (lastDirtyWatcher == watcher) {
                // if this watcher is the last dirty watcher for the last iteration, then we've gone through
                // every watcher (partially in the last iteration) and no watcher is dirty, so we can just
                // stop here
                return null;
            }
        }

        return lastDirtyWatcher;
    }

    private void consumeAsyncQueue() {
        while (!_asyncQueue.isEmpty()) {
            _asyncQueue.removeFirst().run();
        }
    }

    private void consumePostDigestQueue() {
        while (!_postDigestQueue.isEmpty()) {
            _postDigestQueue.removeFirst().run();
        }
    }
}
