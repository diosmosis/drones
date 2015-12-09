package com.flarestar.drones.layout.view.scope;

public class Event {

    public final Class<? extends com.flarestar.drones.views.scope.Event> eventClass;
    public final String expression;

    public Event(Class<? extends com.flarestar.drones.views.scope.Event> eventClass, String expression) {
        this.eventClass = eventClass;
        this.expression = expression;
    }
}
