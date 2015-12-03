package com.flarestar.drones.views;

import android.view.View;

// TODO: ViewFactory is not a very accurate name
public interface ViewFactory {

    interface Iterator {
        boolean hasNext();

        void next();

        View makeView();
    }

    ViewFactory.Iterator iterator();
}
