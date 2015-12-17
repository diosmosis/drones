package com.flarestar.drones.mvw.view;

import com.flarestar.drones.mvw.annotations.directive.IsolateDirective;
import com.flarestar.drones.mvw.processing.parser.exceptions.LayoutFileException;
import com.flarestar.drones.mvw.processing.parser.exceptions.MultipleViewClassesException;
import com.flarestar.drones.mvw.processing.parser.exceptions.NoViewClassForNode;
import com.flarestar.drones.mvw.view.scope.ScopeDefinition;
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

    public void visit(Visitor visitor) {
        visitor.visit(this);
        for (ViewNode child : children) {
            child.visit(visitor);
        }
    }

    public boolean hasTransclude() {
        return element.hasAttr("ng-transclude");
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
}


// TODO: all node types should have an associated directive w/ tagmatcher, so if no directive found for a tag, should throw