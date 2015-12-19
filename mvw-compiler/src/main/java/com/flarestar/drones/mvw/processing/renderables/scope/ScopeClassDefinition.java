package com.flarestar.drones.mvw.processing.renderables.scope;

import com.flarestar.drones.base.generation.Renderable;
import java.util.Collection;

/**
 * TODO
 *
 * TODO:
 * -
 */
public class ScopeClassDefinition implements Renderable {

    private boolean isForDirectiveRoot;
    private boolean hasParentScope;
    private String scopeClassName;
    private String parentScopeClassName;
    private boolean ownerHasIsolateDirective;
    private String ownerIsolateDirectiveName;
    private Collection<ScopePropertyRenderable> ownScopeProperties;
    private Collection<ScopePropertyRenderable> inheritedProperties;
    private Collection<ScopePropertyRenderable> isolateDirectiveProperties;

    public ScopeClassDefinition(boolean isForDirectiveRoot, String scopeClassName, String parentScopeClassName,
                                String isolateDirectiveName, Collection<ScopePropertyRenderable> ownScopeProperties,
                                Collection<ScopePropertyRenderable> inheritedProperties,
                                Collection<ScopePropertyRenderable> isolateDirectiveProperties) {
        this.isForDirectiveRoot = isForDirectiveRoot;
        this.scopeClassName = scopeClassName;

        this.hasParentScope = parentScopeClassName != null;
        this.parentScopeClassName = parentScopeClassName;

        this.ownerIsolateDirectiveName = isolateDirectiveName;
        this.ownerHasIsolateDirective = ownerIsolateDirectiveName != null;

        this.ownScopeProperties = ownScopeProperties;
        this.inheritedProperties = inheritedProperties;
        this.isolateDirectiveProperties = isolateDirectiveProperties;
    }

    @Override
    public String getTemplate() {
        return "templates/scopeDefinition.twig";
    }

    @Override
    public String getModelAttribute() {
        return "scope";
    }

    public boolean isForDirectiveRoot() {
        return isForDirectiveRoot;
    }

    public String getScopeClassName() {
        return scopeClassName;
    }

    public boolean hasParentScope() {
        return hasParentScope;
    }

    public String getParentScopeClassName() {
        return parentScopeClassName;
    }

    public boolean ownerHasIsolateDirective() {
        return ownerHasIsolateDirective;
    }

    public String getOwnerIsolateDirectiveName() {
        return ownerIsolateDirectiveName;
    }

    public Collection<ScopePropertyRenderable> getOwnScopeProperties() {
        return ownScopeProperties;
    }

    public Collection<ScopePropertyRenderable> getInheritedProperties() {
        return inheritedProperties;
    }

    public Collection<ScopePropertyRenderable> getIsolateDirectiveProperties() {
        return isolateDirectiveProperties;
    }
}
