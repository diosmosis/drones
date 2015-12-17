package com.flarestar.drones.mvw.renderables.scope;

import com.flarestar.drones.base.generation.Renderable;
import com.flarestar.drones.mvw.view.scope.ScopeDefinition;

/**
 * TODO
 */
public class ScopeLocals implements Renderable {

    private ScopeDefinition scope;

    public ScopeLocals(ScopeDefinition scope) {
        this.scope = scope;
    }

    @Override
    public String getTemplate() {
        return "templates/scopeLocals.twig";
    }

    @Override
    public String getModelAttribute() {
        return "locals";
    }

    public ScopeDefinition getScope() {
        return scope;
    }
}
