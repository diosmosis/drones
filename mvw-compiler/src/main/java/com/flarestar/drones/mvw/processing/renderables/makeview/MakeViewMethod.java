package com.flarestar.drones.mvw.processing.renderables.makeview;

import com.flarestar.drones.base.generation.Renderable;
import com.flarestar.drones.mvw.model.scope.Property;
import com.flarestar.drones.mvw.processing.renderables.scope.ScopeLocals;
import com.flarestar.drones.mvw.processing.renderables.scope.WatcherDefinition;
import com.flarestar.drones.mvw.processing.renderables.viewfactory.NullViewFactory;
import com.flarestar.drones.mvw.processing.renderables.viewfactory.ViewFactory;
import com.flarestar.drones.mvw.model.Directive;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * TODO
 */
public class MakeViewMethod implements Renderable {
    private String viewId;
    private String parentScopeClassName;
    private String resultType;
    private Directive directive;
    private boolean hasParent = false;
    private boolean isRootDirectiveMethod;

    private ViewFactory viewFactory;
    private ScopeLocals parentScopeLocals = null;
    private List<MakeViewMethod> childRenderables;
    private Collection<WatcherDefinition> parentScopeWatchers;
    private Collection<Property> boundProperties;

    public MakeViewMethod(String viewId, Directive directiveRoot, ViewFactory viewFactory,
                          List<MakeViewMethod> childRenderables, boolean isRootDirectiveMethod,
                          boolean hasParent, ScopeLocals parentScopeLocals, String parentScopeClassName,
                          Collection<WatcherDefinition> parentScopeWatchers, Collection<Property> boundProperties) {
        this.viewId = viewId;
        this.directive = directiveRoot;
        this.viewFactory = viewFactory;
        this.childRenderables = childRenderables;

        this.isRootDirectiveMethod = isRootDirectiveMethod;

        this.hasParent = hasParent;
        this.boundProperties = boundProperties;
        if (hasParent) {
            this.parentScopeLocals = parentScopeLocals;
            this.parentScopeClassName = parentScopeClassName;
            this.resultType = "ViewFactory";
        } else {
            this.resultType = "View";
        }

        this.parentScopeWatchers = parentScopeWatchers == null ? new ArrayList<WatcherDefinition>() : parentScopeWatchers;
    }

    @Override
    public String getTemplate() {
        return "templates/makeViewMethod.twig";
    }

    @Override
    public String getModelAttribute() {
        return "method";
    }

    public ScopeLocals getParentScopeLocals() {
        return parentScopeLocals;
    }

    public ViewFactory getViewFactory() {
        return viewFactory;
    }

    public Directive getDirectiveRoot() {
        return directive;
    }

    public List<MakeViewMethod> getChildrenMakeViewMethods() {
        return childRenderables;
    }

    public String getResultType() {
        return resultType;
    }

    public boolean hasViewFactory() {
        return !(viewFactory instanceof NullViewFactory);
    }

    public boolean isRootDirectiveMethod() {
        return isRootDirectiveMethod;
    }

    public String getViewId() {
        return viewId;
    }

    public boolean hasParent() {
        return hasParent;
    }

    public Collection<WatcherDefinition> getParentScopeWatchers() {
        return parentScopeWatchers;
    }

    public String getParentScopeClassName() {
        return parentScopeClassName;
    }

    public Collection<Property> getBoundProperties() {
        return boundProperties;
    }
}
