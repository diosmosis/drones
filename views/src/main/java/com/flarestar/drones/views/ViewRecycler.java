package com.flarestar.drones.views;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import com.google.common.cache.*;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;

import java.lang.reflect.Constructor;
import java.util.*;

/**
 * TODO
 */
public class ViewRecycler {

    private static final String TAG = "drones.views";

    private Multimap<String, View> detachedViews = MultimapBuilder.hashKeys().arrayListValues().build();

    private Cache<View, Map.Entry<String, View>> detachedViewsLimitedCache = CacheBuilder.newBuilder()
        .maximumSize(64)
        .initialCapacity(8)
        .removalListener(new RemovalListener<View, Map.Entry<String, View>>() {
            @Override
            public void onRemoval(RemovalNotification<View, Map.Entry<String, View>> removalNotification) {
                detachedViews.entries().remove(removalNotification.getValue());
            }
        })
        .build();

    /**
     * TODO
     *
     * @param view
     */
    public void recycleView(View view) {
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        if (!(layoutParams instanceof ScopedViewGroup.LayoutParams)) {
            return;
        }

        String signature = ((ScopedViewGroup.LayoutParams)layoutParams).getSignature();
        cacheView(signature, view);
    }

    private synchronized void cacheView(String signature, View view) {
        Map.Entry<String, View> entry = Maps.immutableEntry(signature, view);
        detachedViews.entries().add(entry);
        detachedViewsLimitedCache.put(view, entry);
    }

    /**
     * TODO
     *
     * @return
     */
    public View makeView(Class<? extends View> viewClass, String viewSignature, Context context) {
        View view = reclaimView(viewClass, viewSignature);
        if (view == null) {
            view = createNewView(viewClass, context);
        }
        return view;
    }

    private View createNewView(Class<? extends View> viewClass, Context context) {
        Constructor<? extends View> constructor = null;
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

    private synchronized View reclaimView(Class<? extends View> viewClass, String viewSignature) {
        Collection<View> views = detachedViews.get(viewSignature);
        if (views.isEmpty()) {
            return null;
        }

        for (View view : views) {
            if (view == null) {
                continue;
            }

            if (!viewClass.isInstance(view)) {
                Log.w(TAG, "Found view of type '" + view.getClass().getName() + "' with unexpected signature: '"
                    + viewSignature + "'. Trying to reclaim view of type '" + viewClass.getName() + "'.");
                continue;
            }

            Map.Entry<String, View> entry = Maps.immutableEntry(viewSignature, view);
            detachedViewsLimitedCache.asMap().remove(entry);

            return view;
        }

        return null;
    }
}
