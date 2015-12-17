package com.flarestar.drones.mvw.renderables.scope;

import com.flarestar.drones.base.generation.Renderable;

public class ScopeEventListener implements Renderable {

    private final Class<? extends com.flarestar.drones.views.scope.Event> eventClass;
    private final String expression;

    public ScopeEventListener(Class<? extends com.flarestar.drones.views.scope.Event> eventClass, String expression) {
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
