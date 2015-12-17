package com.flarestar.drones.mvw.renderables.scope;

import com.flarestar.drones.base.generation.Renderable;

/**
 * TODO
 */
public class ScopeCreationCode implements Renderable {
    private com.flarestar.drones.mvw.view.scope.ScopeDefinition definition;
    private ScopeLocals scopeLocals;
    private boolean isInMakeDirectiveViewMethod;
    private boolean isViewHasIsolateDirective;
    private String afterScopeCreatedCode;

    public ScopeCreationCode(com.flarestar.drones.mvw.view.scope.ScopeDefinition definition, boolean isInMakeDirectiveViewMethod,
                             boolean isViewHasIsolateDirective, String afterScopeCreatedCode) {
        this.definition = definition;
        this.isInMakeDirectiveViewMethod = isInMakeDirectiveViewMethod;
        this.isViewHasIsolateDirective = isViewHasIsolateDirective;
        this.afterScopeCreatedCode = afterScopeCreatedCode;
        this.scopeLocals = new ScopeLocals(definition);
    }

    public boolean shouldCreateNewScopeInstance() {
        com.flarestar.drones.mvw.view.scope.ScopeDefinition definition = getScope();
        return isViewHasIsolateDirective() || !(definition.isPassthroughScope() || isInMakeDirectiveViewMethod());
    }

    public boolean hasParentScope() {
        return isInMakeDirectiveViewMethod() || getScope().getParentScope() != null;
    }

    public ScopeLocals getScopeLocals() {
        return scopeLocals;
    }

    public com.flarestar.drones.mvw.view.scope.ScopeDefinition getScope() {
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

    public String getAfterScopeCreatedCode() {
        return afterScopeCreatedCode;
    }
}
