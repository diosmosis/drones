package com.flarestar.drones.views.viewgroups.dynamic;

import android.view.View;
import android.view.ViewGroup;
import com.flarestar.drones.views.ViewFactory;

public abstract class SingleViewFactory implements ViewFactory {

    public abstract View makeView();

    @Override
    public void makeViews(ViewGroup parent) {
        View view = makeView();
        parent.addView(view);
    }
}
