package com.flarestar.drones.views.viewgroups;

import android.view.View;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * TODO
 */
public class ViewInternalsManipulator {

    private Field mScrollX;
    private Field mScrollY;
    private Method invalidateParentIfNeeded;
    private int scrollViewStyle;

    public ViewInternalsManipulator() {
        Class<?> viewClass = View.class;
        try {
            mScrollX = viewClass.getDeclaredField("mScrollX");
            mScrollX.setAccessible(true);

            mScrollY = viewClass.getDeclaredField("mScrollY");
            mScrollY.setAccessible(true);

            invalidateParentIfNeeded = viewClass.getDeclaredMethod("invalidateParentIfNeeded");
            invalidateParentIfNeeded.setAccessible(true);

            Class<?> klass = Class.forName("com.android.internal.R$attr");
            scrollViewStyle = (int)klass.getField("scrollViewStyle").get(null);
        } catch (NoSuchFieldException | NoSuchMethodException | ClassNotFoundException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public void setScrollYRaw(View view, int scrollY) {
        try {
            mScrollY.set(view, scrollY);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public void setScrollXRaw(View view, int scrollX) {
        try {
            mScrollX.set(view, scrollX);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public void invalidateParentIfNeeded(View view) {
        try {
            invalidateParentIfNeeded.invoke(view);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public int getScrollViewStyleViaReflection() {
        return scrollViewStyle;
    }
}
