package com.flarestar.drones.mvw.view;

import com.flarestar.drones.mvw.annotations.directive.IsolateDirective;
import com.flarestar.drones.mvw.parser.exceptions.LayoutFileException;
import com.flarestar.drones.mvw.parser.exceptions.MultipleViewClassesException;
import com.flarestar.drones.mvw.parser.exceptions.NoViewClassForNode;
import com.flarestar.drones.mvw.view.scope.ScopeDefinition;

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

    public final String id;
    public final String text;
    public final Map<String, String> attributes;
    public final Map<String, String> styles;
    public final String tagName;
    public final List<Directive> directives;
    public final ViewNode parent;
    public final List<ViewNode> children = new ArrayList<>();
    public final ScopeDefinition scopeDefinition;

    private String viewClass;
    private Boolean hasDynamicDirective = null;
    private Boolean hasDynamicChild = null;
    private final Directive isolateDirective;

    public ViewNode(String tagName, String id, String text, ViewNode parent, Map<String, String> attributes,
                    Map<String, String> styles, List<Directive> directives) throws LayoutFileException {
        this.tagName = tagName;
        this.id = id;
        this.text = text;
        this.parent = parent;
        this.attributes = attributes;
        this.styles = styles;
        this.directives = directives;

        for (Directive directive : directives) {
            directive.manipulateViewNode(this);
        }

        scopeDefinition = createScopeDefinition();
        isolateDirective = findIsolateDirective();
        viewClass = findViewClass();
    }

    public String getMakeViewFunctionName() {
        if (hasIsolateDirective()) {
            return "makeDirectiveView_" + isolateDirective.getDirectiveName();
        } else {
            return "makeView_" + id;
        }
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
        return isolateDirective != null || parent == null;
    }

    public boolean hasScope() {
        return scopeDefinition.getOwner() == this;
    }

    public String getViewClassName() {
        return viewClass;
    }

    public boolean hasDynamicDirective() {
        if (hasDynamicDirective == null) {
            hasDynamicDirective = false;
            for (Directive directive : directives) {
                if (directive.isDynamic()) {
                    hasDynamicDirective = true;
                    break;
                }
            }
        }
        return hasDynamicDirective;
    }

    public boolean hasDynamicChild() {
        if (hasDynamicChild == null) {
            hasDynamicChild = false;
            for (ViewNode child : children) {
                if (child.isDynamic()) {
                    hasDynamicChild = true;
                    break;
                }
            }
        }
        return hasDynamicChild;
    }

    public boolean isDynamic() {
        return parent != null && (hasDynamicDirective() || hasScope());
    }

    public void visit(Visitor visitor) {
        visitor.visit(this);
        for (ViewNode child : children) {
            child.visit(visitor);
        }
    }

    private String findViewClass() throws MultipleViewClassesException, NoViewClassForNode {
        if (hasIsolateDirective()) {
            return null;
        }

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

        if (viewClass == null) {
            throw new NoViewClassForNode("Cannot find view class for node <" + tagName + " id = " + id + ">.");
        }

        return viewClass;
    }
}

// TODO: all node types should have an associated directive w/ tagmatcher, so if no directive found for a tag, should throw