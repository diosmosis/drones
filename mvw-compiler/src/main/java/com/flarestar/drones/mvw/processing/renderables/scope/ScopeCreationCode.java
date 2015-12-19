package com.flarestar.drones.mvw.processing.renderables.scope;

import com.flarestar.drones.base.generation.Renderable;
import com.flarestar.drones.mvw.model.scope.ScopeDefinition;

import java.util.Collection;
import java.util.List;

/**
 * TODO
 */
public class ScopeCreationCode implements Renderable {
    private ScopeLocals scopeLocals;
    private boolean isInMakeDirectiveViewMethod;
    private boolean isViewHasIsolateDirective;
    private String scopeClassName;
    private boolean hasParentScope;
    private boolean isPassthroughScope;
    private Collection<ScopePropertyRenderable> ownProperties;

    public ScopeCreationCode(String scopeClassName, boolean isPassthroughScope, boolean hasParentScope,
                             boolean isInMakeDirectiveViewMethod, boolean isViewHasIsolateDirective,
                             Collection<ScopePropertyRenderable> ownProperties, ScopeLocals scopeLocals) {
        this.scopeClassName = scopeClassName;
        this.isPassthroughScope = isPassthroughScope;
        this.hasParentScope = hasParentScope;
        this.isInMakeDirectiveViewMethod = isInMakeDirectiveViewMethod;
        this.isViewHasIsolateDirective = isViewHasIsolateDirective;
        this.scopeLocals = scopeLocals;
        this.ownProperties = ownProperties;
    }

    public boolean shouldCreateNewScopeInstance() {
        return isViewHasIsolateDirective() || !(isPassthroughScope || isInMakeDirectiveViewMethod());
    }

    public boolean hasParentScopeArgument() {
        return isInMakeDirectiveViewMethod() || hasParentScope;
    }

    public ScopeLocals getScopeLocals() {
        return scopeLocals;
    }

    public boolean isInMakeDirectiveViewMethod() {
        return isInMakeDirectiveViewMethod;
    }

    @Override
    public String getTemplate() {
        return "templates/scopeCreationCode.twig";
    }

    @Override
    public String getModelAttribute() {
        return "definition";
    }

    public boolean isViewHasIsolateDirective() {
        return isViewHasIsolateDirective;
    }

    public String getScopeClassName() {
        return scopeClassName;
    }

    public Collection<ScopePropertyRenderable> getOwnProperties() {
        return ownProperties;
    }
}
