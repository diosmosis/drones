package com.flarestar.drones.views.scope;

import android.os.Handler;
import android.view.View;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * TODO
 *
 * TODO: should note some things about storing scopes, specifically, should never reference
 *       a Scope directly. all references to scopes should be handled by drones.
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

    public static abstract class PostDigestRunnable implements java.lang.Runnable {
        public boolean isFixed() {
            return false;
        }
    }

    private static class QueuedRunnable implements java.lang.Runnable {
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

    // TODO: there is only one of these per hierarchy. should note this somewhere.
    private final LinkedList<QueuedRunnable> _asyncQueue;
    private final LinkedList<PostDigestRunnable> _postDigestQueue;

    // TODO: use same naming as angular ie, $$ prefix
    private List<Watcher> _watchers = new ArrayList<>();
    public LinkedList<Scope<?>> _children = new LinkedList<>();
    // TODO: in general, scopes will only be visible to views, no other java code, so it may be more efficient
    //       to avoid using a map, and instead override fire in generated code.
    private Multimap<String, Listener> _listeners = MultimapBuilder.hashKeys().arrayListValues().build();
    private String _phase;

    // TODO: should not allow classes other than Scope to set, while still allowing property-like access in generated code
    public P _parent;
    public final Handler _handler;
    private View _owner;

    public Scope(Handler handler, View owner) {
        this(handler, owner, null, new LinkedList<QueuedRunnable>(), new LinkedList<PostDigestRunnable>());
    }

    public Scope(Handler handler, View owner, P parent) {
        this(handler, owner, parent, ((Scope<?>)parent)._asyncQueue, ((Scope)parent)._postDigestQueue);

        if (_parent != null) {
            _parent.addChild(this);
        }
    }

    private Scope(Handler handler, View owner, P parent, LinkedList<QueuedRunnable> asyncQueue,
                  LinkedList<PostDigestRunnable> postDigestQueue) {
        _handler = handler;
        _owner = owner;
        _parent = parent;
        _asyncQueue = asyncQueue;
        _postDigestQueue = postDigestQueue;
    }

    public Scope<?> getRoot() {
        Scope<?> result = this;
        while (result._parent != null) {
            result = result._parent;
        }
        return result;
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
            return runnable == null ? null : eval(runnable);
        } finally {
            clearPhase();
            getRoot().digest();
        }
    }

    public Object apply() {
        return apply(null);
    }

    public void evalAsync(Runnable runnable) {
        if (_phase == null && _asyncQueue.isEmpty()) {
            _handler.post(new java.lang.Runnable() {
                @Override
                public void run() {
                    if (!_asyncQueue.isEmpty()) {
                        getRoot().digest();
                    }
                }
            });
        }

        _asyncQueue.add(new QueuedRunnable(this, runnable));
    }

    // TODO: not implementing applyAsync right now. not sure if it's necessary.

    public void postDigest(PostDigestRunnable runnable) {
        _postDigestQueue.add(runnable);
    }

    public <E extends Event> void on(Class<E> eventClass, Listener<E> listener) {
        _listeners.put(eventClass.getName(), listener);
    }

    public <E extends Event> void removeListener(Class<E> eventClass, Listener<E> listener){
        _listeners.remove(eventClass.getName(), listener);
    }

    public <E extends Event> void emit(E event) {
        Scope scope = this;
        do {
            scope.fire(event);

            if (event.isPropagationEnded()) {
                return;
            }

            scope = scope._parent;
        } while (scope != null);
    }

    public <E extends Event> void broadcast(E event) {
        fire(event);

        for (Scope child : _children) {
            child.broadcast(event);
        }
    }

    private <E extends Event> void fire(E event) {
        for (Listener listener : _listeners.get(event.getClass().getName())) {
            listener.invoke(event);
        }
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
        boolean isDirty = false;
        for (Watcher watcher : _watchers) {
            Object newValue = watcher.getWatchValue(this);
            Object lastValue = watcher.getLastValue();
            if (!watcher.areValuesEqual(newValue, lastValue)) {
                lastDirtyWatcher = watcher;
                isDirty = true;

                watcher.setLastValue(newValue);
                watcher.onValueChanged(newValue, lastValue == Watcher.INITIAL_VALUE ? newValue : lastValue, this);
            } else if (lastDirtyWatcher == watcher) {
                // if this watcher is the last dirty watcher for the last iteration, then we've gone through
                // every watcher (partially in the last iteration) and no watcher is dirty, so we can just
                // stop here
                break;
            }
        }

        if (!isDirty) {
            lastDirtyWatcher = null;
        }

        // TODO: have changed it a bit from angular to work in Java, which may result in some performance
        // issues.
        for (Scope<?> child : _children) {
            Watcher lastDirtyChildWatcher = child.checkWatchers(null);
            if (lastDirtyChildWatcher != null) {
                // child scopes can watch values in a parent scope, so if one was executed then it's possible
                // that a value in the current scope has been changed. by setting the last dirty watcher to
                // the last dirty watcher in the child scope, we ensure the entire list of watchers will be run
                // on the next iteration.
                lastDirtyWatcher = lastDirtyChildWatcher;
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
        Iterator<PostDigestRunnable> it = _postDigestQueue.iterator();
        while (it.hasNext()) {
            PostDigestRunnable runnable = it.next();
            runnable.run();

            if (!runnable.isFixed()) {
                it.remove();
            }
        }
    }

    protected void addChild(Scope<?> child) {
        _children.add(child);
    }

    // TODO: we use this method of searching for a child before detaching in order to be able to detach in a
    // ViewGroup.OnHierarchyChangeListener object. this ensures scopes are removed even if someone or something
    // outside of the drones library removes a view. perhaps there's a better solution?
    public void detachChild(View owner) {
        Iterator<Scope<?>> it = _children.listIterator();
        while (it.hasNext()) {
            Scope<?> childScope = it.next();
            if (childScope._owner == owner) {
                childScope._parent = null;
                childScope._watchers.clear();
                childScope._listeners.clear();
                childScope._owner = null;
                it.remove();
                return;
            }
        }
    }

    public Scope<?> getChildScopeFor(View child) {
        for (Scope<?> childScope : _children) {
            if (childScope._owner == child) {
                return childScope;
            }
        }
        return null;
    }
}
