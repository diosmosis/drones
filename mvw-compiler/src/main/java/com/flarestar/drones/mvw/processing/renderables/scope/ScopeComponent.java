package com.flarestar.drones.mvw.processing.renderables.scope;

import com.flarestar.drones.base.generation.Renderable;

import java.util.Collection;

/**
 * TODO
 */
public class ScopeComponent implements Renderable {

    private String activityModuleClassName;
    private Collection<String> scopeClassNames;
    private Collection<String> genericScopeClassNames;

    public ScopeComponent(String activityModuleClassName, Collection<String> scopeClassNames,
                          Collection<String> genericScopeClassNames) {
        this.activityModuleClassName = activityModuleClassName;
        this.scopeClassNames = scopeClassNames;
        this.genericScopeClassNames = genericScopeClassNames;
    }

    @Override
    public String getTemplate() {
        return "templates/scopeComponent.twig";
    }

    @Override
    public String getModelAttribute() {
        return "component";
    }

    public String getActivityModuleClassName() {
        return activityModuleClassName;
    }

    public Collection<String> getScopeClassNames() {
        return scopeClassNames;
    }

    public Collection<String> getGenericScopeClassNames() {
        return genericScopeClassNames;
    }
}
