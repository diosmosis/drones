package com.flarestar.drones.views;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import com.flarestar.drones.views.viewgroups.ScopedViewGroup;
import com.google.common.cache.*;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.lang.reflect.Constructor;
import java.util.*;

/**
 * TODO
 */
@Singleton
public class ViewRecycler {

    private static final String TAG = "drones.views";

    private Multimap<String, View> detachedViews = MultimapBuilder.hashKeys().arrayListValues().build();

    private Cache<View, View> detachedViewsLimitedCache = CacheBuilder.newBuilder()
        .maximumSize(64)
        .initialCapacity(8)
        .removalListener(new RemovalListener<View, View>() {
            @Override
            public void onRemoval(RemovalNotification<View, View> removalNotification) {
                View value = removalNotification.getValue();
                detachedViews.remove(value.getClass().getName(), value);
            }
        })
        .build();

    @Inject
    public ViewRecycler() {
        // empty
    }

    /**
     * TODO
     *
     * @param view
     */
    public synchronized void recycleView(View view) {
        detachedViews.put(view.getClass().getName(), view);
        detachedViewsLimitedCache.put(view, view);
    }

    /**
     * TODO
     *
     * @return
     */
    public <V extends  View> V makeView(Class<V> viewClass, Context context) {
        V view = reclaimView(viewClass);
        if (view == null) {
            view = createNewView(viewClass, context);
        }
        return view;
    }

    private <V extends  View> V createNewView(Class<V> viewClass, Context context) {
        Constructor<V> constructor = null;
        try {
            constructor = viewClass.getConstructor(Context.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Trying to construct invalid view class '" + viewClass.getName()
                + "'. Class has no View(Context) constructor.", e);
        }

        try {
            return constructor.newInstance(context);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Could not create new instance of '" + viewClass.getName() + "'.", e);
        }
    }

    private synchronized <V extends  View> V reclaimView(Class<V> viewClass) {
        Collection<View> views = detachedViews.get(viewClass.getName());
        if (views.isEmpty()) {
            return null;
        }

        for (View view : views) {
            if (view == null) {
                continue;
            }

            if (!viewClass.equals(view.getClass())) {
                Log.w(TAG, "Found view of type '" + view.getClass().getName() + "', expected view of type '"
                    + viewClass.getName() + "'.");
                continue;
            }

            detachedViewsLimitedCache.invalidate(view);

            return (V)view;
        }

        return null;
    }
}
