package com.flarestar.drones.mvw.model;

import com.flarestar.drones.mvw.annotations.directive.IsolateDirective;
import com.flarestar.drones.mvw.processing.parser.exceptions.LayoutFileException;
import com.flarestar.drones.mvw.processing.parser.exceptions.MultipleViewClassesException;
import com.flarestar.drones.mvw.processing.parser.exceptions.NoViewClassForNode;
import com.flarestar.drones.mvw.model.scope.ScopeDefinition;
import com.flarestar.drones.mvw.processing.renderables.viewfactory.NullViewFactory;
import com.flarestar.drones.mvw.processing.renderables.viewfactory.SingleViewFactory;
import com.flarestar.drones.mvw.processing.renderables.viewfactory.ViewFactory;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * TODO
 */
public class ViewNode {

    public interface Visitor {
        void visit(ViewNode node);
    }

    public final Element element;
    public final String id;
    public final Map<String, String> styles;
    public final List<Directive> directives;
    public final ViewNode parent;
    public final List<ViewNode> children = new ArrayList<>();
    public final ScopeDefinition scopeDefinition;
    public final boolean isDirectiveRoot;
    public final List<ViewProperty> viewProperties;

    private String viewClass;
    private Class<? extends ViewFactory> viewFactoryRenderable;
    public final Directive isolateDirective;

    public ViewNode(Element element, ViewNode parent, Map<String, String> styles, List<Directive> directives,
                    boolean isDirectiveRoot)
            throws LayoutFileException {
        this.element = element;

        String id = element.attr("id");
        this.id = (id == null || id.isEmpty()) ? ("view" + hashCode()) : id;

        this.parent = parent;
        this.styles = styles;
        this.directives = directives;
        this.isDirectiveRoot = isDirectiveRoot;

        for (Directive directive : directives) {
            directive.manipulateViewNode(this);
        }

        viewProperties = findViewProperties();
        scopeDefinition = createScopeDefinition();
        isolateDirective = findIsolateDirective();
        viewClass = findViewClass();
        viewFactoryRenderable = findViewFactoryRenderableToUse();
    }

    private List<ViewProperty> findViewProperties() {
        List<ViewProperty> result = new ArrayList<>();
        for (Directive directive : directives) {
            result.addAll(directive.getViewProperties());
        };
        return result;
    }

    private ScopeDefinition createScopeDefinition() throws LayoutFileException {
        ScopeDefinition computed = new ScopeDefinition(this, hasIsolateDirective());
        if (parent == null || !computed.isPassthroughScope()) {
            return computed;
        } else {
            return parent.scopeDefinition;
        }
    }

    private Directive findIsolateDirective() throws LayoutFileException {
        Directive isolateDirective = null;
        for (Directive directive : directives) {
            IsolateDirective annotation = directive.getClass().getAnnotation(IsolateDirective.class);
            if (annotation == null) {
                continue;
            }

            if (isolateDirective != null) {
                throw new LayoutFileException("Element '" + id + "' has multiple isolate directives, only one is allowed per element.");
            }

            isolateDirective = directive;
        }
        return isolateDirective;
    }

    public boolean hasIsolateDirective() {
        return isolateDirective != null;
    }

    public boolean hasTranscludeDirective() {
        return isolateDirective != null && isolateDirective.hasTransclude();
    }

    public boolean hasScope() {
        return scopeDefinition.getOwner() == this;
    }

    public String getViewClassName() {
        return viewClass;
    }

    public Class<? extends ViewFactory> getViewFactoryRenderable() {
        return viewFactoryRenderable;
    }

    public void visit(Visitor visitor) {
        visitor.visit(this);
        for (ViewNode child : children) {
            child.visit(visitor);
        }
    }

    public boolean hasTransclude() {
        return element.hasAttr("ng-transclude");
    }

    public boolean isUsingIsolateDirective() {
        return hasIsolateDirective() && parent != null;
    }

    public boolean isIsolateDirectiveRoot() {
        return hasIsolateDirective() && parent == null;
    }

    private String findViewClass() throws MultipleViewClassesException, NoViewClassForNode {
        String viewClass = null;

        for (Directive directive : directives) {
            String directiveViewClass = directive.getViewClassName();
            if (directiveViewClass == null) {
                continue;
            }

            if (viewClass != null) {
                throw new MultipleViewClassesException(id, viewClass, directiveViewClass);
            }

            viewClass = directiveViewClass;
        }

        if (viewClass == null && !hasIsolateDirective()) {
            throw new NoViewClassForNode("Cannot find view class for node <" + element.tagName() + " id = " + id + ">.");
        }

        return viewClass;
    }

    private Class<? extends ViewFactory> findViewFactoryRenderableToUse() throws LayoutFileException {
        // if this is the root of a layout or directive tree, we use a NullViewFactory, since the method will return
        // a View instead of a ViewFactory
        if (parent == null) {
            return NullViewFactory.class;
        }

        Class<? extends ViewFactory> result = null;

        for (Directive viewDirective : directives) {
            Class<? extends ViewFactory> renderableFromDirective = viewDirective.getViewFactoryToUse();
            if (renderableFromDirective != null) {
                if (result != null) {
                    throw new LayoutFileException("Multiple view factory types defined by directives on <" + element.tagName()
                        + " id='" + id + "'>.");
                }

                result = renderableFromDirective;
            }
        }

        if (result == null) {
            result = SingleViewFactory.class;
        }

        return result;
    }
}


// TODO: all node types should have an associated directive w/ tagmatcher, so if no directive found for a tag, should throw