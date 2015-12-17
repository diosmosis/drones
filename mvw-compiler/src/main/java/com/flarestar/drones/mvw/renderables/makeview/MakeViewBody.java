package com.flarestar.drones.mvw.renderables.makeview;

import com.flarestar.drones.base.generation.Renderable;
import com.flarestar.drones.mvw.compilerutilities.TypeInferer;
import com.flarestar.drones.mvw.renderables.scope.ScopeCreationCode;
import com.flarestar.drones.mvw.view.Directive;
import com.flarestar.drones.mvw.view.ViewNode;
import com.flarestar.drones.views.viewgroups.BaseDroneViewGroup;

/**
 * TODO
 */
public class MakeViewBody implements Renderable {
    private ViewNode view;
    private Directive currentIsolateDirective;
    private ScopeCreationCode scopeCreationCode;
    private ViewCreationCode viewCreationCode;

    // TODO: instead of 'afterScopeCreatedCode', should create a way to specify a scope varialbe should be initialized
    //       after creating a new instance, instead of in the constructor.
    public MakeViewBody(ViewNode view, Directive currentIsolateDirective, String afterScopeCreatedCode) {
        this(view, currentIsolateDirective, makeScopeCreationCode(view, afterScopeCreatedCode, currentIsolateDirective));
    }

    public MakeViewBody(ViewNode view, Directive currentIsolateDirective, ScopeCreationCode scopeCreationCode) {
        this.view = view;
        this.currentIsolateDirective = currentIsolateDirective;
        this.scopeCreationCode = scopeCreationCode;
        this.viewCreationCode = new ViewCreationCode(view);
    }

    public MakeViewBody(ViewNode view, Directive currentIsolateDirective) {
        this(view, currentIsolateDirective, "");
    }

    @Override
    public String getTemplate() {
        return "templates/makeViewBody.twig";
    }

    @Override
    public String getModelAttribute() {
        return "body";
    }

    public boolean isViewScopeView(TypeInferer inferer) {
        ViewNode view = getView();
        return view.getViewClassName() != null && inferer.isAssignable(view.getViewClassName(), BaseDroneViewGroup.class.getName());
    }

    public ViewNode getView() {
        return view;
    }

    public boolean hasTransclude() {
        return view.hasTransclude() && currentIsolateDirective != null && currentIsolateDirective.hasTransclude();
    }

    public ScopeCreationCode getScopeCreationCode() {
        return scopeCreationCode;
    }

    public boolean hasOwnScope() {
        return view.hasScope();
    }

    protected static ScopeCreationCode makeScopeCreationCode(ViewNode view, String afterScopeCreatedCode, Directive directiveRoot) {
        boolean isInMakeDirectiveMethod = view.parent == null && directiveRoot != null;
        return new ScopeCreationCode(view.scopeDefinition, isInMakeDirectiveMethod, view.hasIsolateDirective(),
            afterScopeCreatedCode);
    }

    public ViewCreationCode getViewCreationCode() {
        return viewCreationCode;
    }
}