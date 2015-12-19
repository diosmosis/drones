package com.flarestar.drones.mvw.processing.renderables.makeview;

import com.flarestar.drones.base.generation.Renderable;
import com.flarestar.drones.mvw.processing.parser.exceptions.LayoutFileException;
import com.flarestar.drones.mvw.processing.renderables.scope.ScopeLocals;
import com.flarestar.drones.mvw.processing.renderables.scope.WatcherDefinition;
import com.flarestar.drones.mvw.processing.renderables.viewfactory.NullViewFactory;
import com.flarestar.drones.mvw.processing.renderables.viewfactory.SingleViewFactory;
import com.flarestar.drones.mvw.processing.renderables.viewfactory.ViewFactory;
import com.flarestar.drones.mvw.model.Directive;
import com.flarestar.drones.mvw.model.ViewNode;
import com.flarestar.drones.mvw.model.scope.ScopeDefinition;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
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
    private List<WatcherDefinition> parentScopeWatchers;

    public MakeViewMethod(String viewId, Directive directiveRoot, ViewFactory viewFactory,
                          List<MakeViewMethod> childRenderables, boolean isRootDirectiveMethod,
                          boolean hasParent, ScopeLocals parentScopeLocals, String parentScopeClassName,
                          List<WatcherDefinition> parentScopeWatchers) {
        this.viewId = viewId;
        this.directive = directiveRoot;
        this.viewFactory = viewFactory;
        this.childRenderables = childRenderables;

        this.isRootDirectiveMethod = isRootDirectiveMethod;

        this.hasParent = hasParent;
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

    public List<WatcherDefinition> getParentScopeWatchers() {
        return parentScopeWatchers;
    }

    public String getParentScopeClassName() {
        return parentScopeClassName;
    }
}
