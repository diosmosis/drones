package com.flarestar.drones.mvw.renderables.makeview;

import com.flarestar.drones.base.generation.Renderable;
import com.flarestar.drones.mvw.parser.exceptions.LayoutFileException;
import com.flarestar.drones.mvw.renderables.scope.ScopeLocals;
import com.flarestar.drones.mvw.renderables.viewfactory.NullViewFactory;
import com.flarestar.drones.mvw.renderables.viewfactory.ViewFactory;
import com.flarestar.drones.mvw.view.Directive;
import com.flarestar.drones.mvw.view.ViewNode;
import com.flarestar.drones.mvw.view.scope.ScopeDefinition;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * TODO
 */
public class MakeViewMethod implements Renderable {
    private ViewNode view;
    private ScopeLocals parentScopeLocals = null;
    private ViewFactory viewFactory;
    private Directive directive;

    public MakeViewMethod(ViewNode view, @Nullable Directive directiveRoot) throws LayoutFileException {
        this.view = view;
        this.directive = directiveRoot;

        // if this is the root of a layout or directive tree, we use a NullViewFactory, since the method will return
        // a View instead of a ViewFactory
        if (view.parent == null) {
            this.viewFactory = new NullViewFactory(new MakeViewBody(view, directiveRoot));
        } else {
            this.viewFactory = view.getViewFactoryRenderable(directiveRoot);
        }

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

    public List<MakeViewMethod> getChildrenMakeViewMethods() throws LayoutFileException {
        return Lists.newArrayList(Iterables.transform(view.children, new Function<ViewNode, MakeViewMethod>() {
            @Nonnull
            @Override
            public MakeViewMethod apply(@Nonnull ViewNode child) {
                try {
                    return new MakeViewMethod(child, directive);
                } catch (LayoutFileException e) {
                    throw new RuntimeException(e); // TODO: very annoying we have to do this...
                }
            }
        }));
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
