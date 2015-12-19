package com.flarestar.drones.mvw.processing.renderables.makeview;

import com.flarestar.drones.base.generation.Renderable;
import com.flarestar.drones.mvw.processing.parser.exceptions.LayoutFileException;
import com.flarestar.drones.mvw.processing.renderables.scope.ScopeLocals;
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
import java.util.List;

/**
 * TODO
 *
 * LayoutBuilder builder = renderableFactory.makeLayoutBuilder(LayoutBuilder.class);
 *   - renderable factory
 *
 * xml/less => LayoutProcessor => ViewNode tree
 * ViewNode tree => RenderableFactory => renderable tree (LayoutBuilder)
 * renderable tree => generator => outputted files
 *
 * TODO:
 * - remove ViewNode dependency from Renderable object model
 * - implement process above in LayoutAnnotationProcessor
 *
 * TODO: renderables should not be @Singletons and must be @Inject-ed. need to put this somewhere.
 */
public class MakeViewMethod implements Renderable {
    private ViewNode view;
    private ScopeLocals parentScopeLocals = null;
    private ViewFactory viewFactory;
    private Directive directive;
    private List<MakeViewMethod> childRenderables;

    public MakeViewMethod(ViewNode view, @Nullable Directive directiveRoot, ViewFactory viewFactory,
                          List<MakeViewMethod> childRenderables) {
        this.view = view;
        this.directive = directiveRoot;

        this.viewFactory = viewFactory;
        this.childRenderables = childRenderables;

        if (view.parent != null) {
            this.parentScopeLocals = new ScopeLocals(view.parent.scopeDefinition);
        }
    }

    @Override
    public String getTemplate() {
        return "templates/makeViewMethod.twig";
    }

    @Override
    public String getModelAttribute() {
        return "method";
    }

    public ViewNode getView() {
        return view;
    }

    public ScopeLocals getParentScopeLocals() {
        return parentScopeLocals;
    }

    public ViewFactory getViewFactory() {
        return viewFactory;
    }

    public Directive getDirective() {
        return directive;
    }

    public List<MakeViewMethod> getChildrenMakeViewMethods() {
        return childRenderables;
    }

    public String getResultType() {
        return view.parent == null ? "View" : "ViewFactory";
    }

    public boolean hasViewFactory() {
        return !(viewFactory instanceof NullViewFactory);
    }

    public ScopeDefinition getParentViewScope() {
        return getView().parent.scopeDefinition;
    }

    public boolean isRootDirectiveMethod() {
        return directive != null && view.parent == null;
    }
}
