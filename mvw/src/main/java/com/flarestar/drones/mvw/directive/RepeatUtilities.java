package com.flarestar.drones.mvw.directive;

import android.view.View;
import com.flarestar.drones.views.scope.Scope;
import com.flarestar.drones.views.viewgroups.BaseDroneViewGroup;
import com.flarestar.drones.views.viewgroups.dynamic.RangeViewFactory;

/**
 * TODO
 */
public class RepeatUtilities {

    public static <V> void queueOnValuesChanged(final BaseDroneViewGroup parent, final RangeViewFactory<V> viewFactory) {
        parent.getScope().postDigest(new Scope.PostDigestRunnable() {
            @Override
            public void run() {
                onValuesChanged(parent, viewFactory);
            }
        });
    }

    public static <V> void onValuesChanged(BaseDroneViewGroup parent, RangeViewFactory<V> viewFactory) {
        final int startChildIndex = Math.max(getViewIndexOf(parent, viewFactory.getStartView()), 0);
        int endChildIndex = getViewIndexOf(parent, viewFactory.getEndView(), startChildIndex) + 1;

        Iterable<V> currentValueCollection = viewFactory.getCollection();

        int index = startChildIndex;
        for (V item : currentValueCollection) {
            int existingViewIndex = findChildWithItem(viewFactory, parent, item, index, endChildIndex);
            if (existingViewIndex == index) {
                ++index;
                continue;
            }

            View view;
            if (existingViewIndex == -1) {
                // couldn't find existing view, so create new one
                view = viewFactory.makeView(item, index);
                parent.addView(view, index);

                ++endChildIndex; // update the end index since we're adding a child to the view
            } else {
                // there's already a view w/ this item, so just move it
                view = parent.getChildAt(existingViewIndex);
                Scope<?> scope = parent.getScope().getChildScopeFor(view);

                viewFactory.setScopeProperties(scope, item, index);

                parent.moveView(existingViewIndex, index);
            }

            if (index == startChildIndex) {
                viewFactory.setStartView(view);
            }

            ++index;
        }

        int newEndIndex = index - 1;

        // remove other views since they are not in the collection anymore
        for (int i = index; i < endChildIndex; ++i) {
            parent.removeViewAt(index);
        }

        if (index == 0) { // all the views were removed
            viewFactory.setStartView(null);
        }

        viewFactory.setEndView(parent.getChildAt(newEndIndex));
    }

    private static int getViewIndexOf(BaseDroneViewGroup parent, View view) {
        return getViewIndexOf(parent, view, 0);
    }

    private static int getViewIndexOf(BaseDroneViewGroup parent, View view, int index) {
        for (; index != parent.getChildCount(); ++index) {
            if (parent.getChildAt(index) == view) {
                return index;
            }
        }
        return -1;
    }

    private static <V> int findChildWithItem(RangeViewFactory<V> viewFactory, BaseDroneViewGroup parent, V item,
                                             int index, int endChildIndex) {
        for (; index < endChildIndex; ++index) {
            Scope<?> childScope = parent.getScope().getChildScopeFor(parent.getChildAt(index));
            if (childScope == null) {
                continue;
            }

            if (item.equals(viewFactory.getItem(childScope))) {
                return index;
            }
        }
        return -1;
    }
}
