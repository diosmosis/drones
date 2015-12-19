package com.flarestar.drones.mvw.processing.renderables.scope;

import com.flarestar.drones.base.generation.Renderable;

import java.util.Collection;

/**
 * TODO
 */
public class ScopeLocals implements Renderable {

    private String parentScopeClassName;
    private Collection<ScopePropertyRenderable> allScopeProperties;

    public ScopeLocals(String parentScopeClassName, Collection<ScopePropertyRenderable> allScopeProperties) {
        this.parentScopeClassName = parentScopeClassName;
        this.allScopeProperties = allScopeProperties;
    }

    @Override
    public String getTemplate() {
        return "templates/scopeLocals.twig";
    }

    @Override
    public String getModelAttribute() {
        return "locals";
    }

    public boolean hasParent() {
        return parentScopeClassName != null;
    }

    public String getParentScopeClassName() {
        return parentScopeClassName;
    }

    public Collection<ScopePropertyRenderable> getAllScopeProperties() {
        return allScopeProperties;
    }
}
