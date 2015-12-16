package com.flarestar.drones.mvw.view.scope;

import com.flarestar.drones.base.generation.Renderable;

public class Event implements Renderable {

    private final Class<? extends com.flarestar.drones.views.scope.Event> eventClass;
    private final String expression;

    public Event(Class<? extends com.flarestar.drones.views.scope.Event> eventClass, String expression) {
        this.eventClass = eventClass;
        this.expression = expression;
    }

    public Class<? extends com.flarestar.drones.views.scope.Event> getEventClass() {
        return eventClass;
    }

    public String getExpression() {
        return expression;
    }

    @Override
    public String getTemplate() {
        return "templates/event.twig";
    }

    @Override
    public String getModelAttribute() {
        return "event";
    }
}
