package com.flarestar.drones.mvw.processing.renderables.scope;

import com.flarestar.drones.base.generation.Renderable;

/**
 * TODO
 */
public class ScopeCreationCode implements Renderable {
    private com.flarestar.drones.mvw.model.scope.ScopeDefinition definition;
    private ScopeLocals scopeLocals;
    private boolean isInMakeDirectiveViewMethod;
    private boolean isViewHasIsolateDirective;

    public ScopeCreationCode(com.flarestar.drones.mvw.model.scope.ScopeDefinition definition, boolean isInMakeDirectiveViewMethod,
                             boolean isViewHasIsolateDirective) {
        this.definition = definition;
        this.isInMakeDirectiveViewMethod = isInMakeDirectiveViewMethod;
        this.isViewHasIsolateDirective = isViewHasIsolateDirective;
        this.scopeLocals = new ScopeLocals(definition);
    }

    public boolean shouldCreateNewScopeInstance() {
        com.flarestar.drones.mvw.model.scope.ScopeDefinition definition = getScope();
        return isViewHasIsolateDirective() || !(definition.isPassthroughScope() || isInMakeDirectiveViewMethod());
    }

    public boolean hasParentScope() {
        return isInMakeDirectiveViewMethod() || getScope().getParentScope() != null;
    }

    public ScopeLocals getScopeLocals() {
        return scopeLocals;
    }

    public com.flarestar.drones.mvw.model.scope.ScopeDefinition getScope() {
        return definition;
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
}
