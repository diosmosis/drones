package com.flarestar.drones.views.scope.events;

import android.view.View;
import com.flarestar.drones.views.scope.Event;

public abstract class ViewEvent extends Event {

    private final View context;

    public ViewEvent(View context) {
        this.context = context;
    }

    public View getContext() {
        return context;
    }
}
