package com.flarestar.drones.mvw.directive;

import android.view.View;
import com.flarestar.drones.views.scope.Scope;
import com.flarestar.drones.views.viewgroups.DynamicViewGroup;
import com.flarestar.drones.views.viewgroups.dynamic.RangeViewFactory;

/**
 * TODO
 */
public class RepeatUtilities {

    public static <V> void queueOnValuesChanged(final DynamicViewGroup parent, final RangeViewFactory<V> viewFactory) {
        parent.getScope().postDigest(new Scope.PostDigestRunnable() {
            @Override
            public void run() {
                onValuesChanged(parent, viewFactory);
            }
        });
    }

    public static <V> void onValuesChanged(DynamicViewGroup parent, RangeViewFactory<V> viewFactory) {
        final int startChildIndex = getViewIndexOf(parent, viewFactory.getStartView());
        final int endChildIndex = getViewIndexOf(parent, viewFactory.getEndView(), startChildIndex) + 1;

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
            } else {
                // there's already a view w/ this item, so just move it
                view = parent.getChildAt(existingViewIndex);
                Scope<?> scope = parent.getScope().getChildScopeFor(view);

                viewFactory.setScopeProperties(scope, index);

                parent.moveView(existingViewIndex, index);
            }

            if (index == startChildIndex) {
                viewFactory.setStartView(view);
            }

            ++index;
        }

        int newEndIndex = index;

        // remove other views since they are not in the collection anymore
        for (; index < endChildIndex; ++index) {
            parent.removeViewAt(index);
        }

        viewFactory.setEndView(parent.getChildAt(newEndIndex));
    }

    private static int getViewIndexOf(DynamicViewGroup parent, View view) {
        return getViewIndexOf(parent, view, 0);
    }

    private static int getViewIndexOf(DynamicViewGroup parent, View view, int index) {
        for (; index != parent.getChildCount(); ++index) {
            if (parent.getChildAt(index) == view) {
                return index;
            }
        }
        return 0;
    }

    private static <V> int findChildWithItem(RangeViewFactory<V> viewFactory, DynamicViewGroup parent, V item,
                                             int index, int endChildIndex) {
        for (; index != endChildIndex; ++index) {
            Scope<?> childScope = parent.getScope().getChildScopeFor(parent.getChildAt(index));
            if (childScope == null) {
                continue;
            }

            if (item == viewFactory.getItem(childScope)) {
                return index;
            }
        }
        return -1;
    }
}
