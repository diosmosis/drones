package com.flarestar.drones.mvw.renderables.scope;

import com.flarestar.drones.base.generation.Renderable;

/**
 * TODO
 */
public class ScopeDefinition implements Renderable {

    private com.flarestar.drones.mvw.view.scope.ScopeDefinition definition;

    public ScopeDefinition(com.flarestar.drones.mvw.view.scope.ScopeDefinition definition) {
        this.definition = definition;
    }

    @Override
    public String getTemplate() {
        return "templates/scopeDefinition.twig";
    }

    @Override
    public String getModelAttribute() {
        return "scope";
    }

    public com.flarestar.drones.mvw.view.scope.ScopeDefinition getDefinition() {
        return definition;
    }
}