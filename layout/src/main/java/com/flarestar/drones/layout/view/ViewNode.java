package com.flarestar.drones.layout.view;

import com.flarestar.drones.layout.annotations.directive.IsolateScope;
import com.flarestar.drones.layout.parser.exceptions.LayoutFileException;
import com.flarestar.drones.layout.parser.exceptions.MultipleViewClassesException;
import com.flarestar.drones.layout.parser.exceptions.NoViewClassForNode;
import com.flarestar.drones.layout.view.scope.Property;
import com.flarestar.drones.layout.view.scope.ScopeDefinition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TODO
 */
public class ViewNode {
    public final String id;
    public final String text;
    public final Map<String, String> attributes = new HashMap<>();
    public final Map<String, String> styles = new HashMap<>();
    public final List<ViewNode> children = new ArrayList<>();
    public final String tagName;
    public final List<Directive> directives = new ArrayList<>();
    public final ViewNode parent;

    private ScopeDefinition scopeDefinition;
    private String viewClass;
    private Boolean hasDynamicDirective = null;
    private Boolean isDynamic = null;

    public ViewNode(String tagName, String id, String text, ViewNode parent) {
        this.tagName = tagName;
        this.id = id;
        this.text = text;
        this.parent = parent;
    }

    public ScopeDefinition getScopeDefinition() throws LayoutFileException {
        if (scopeDefinition == null) {
            ScopeDefinition computed = new ScopeDefinition(this, hasIsolateScope());
            if (parent == null || !computed.isPassthroughScope()) {
                scopeDefinition = computed;
            } else {
                scopeDefinition = parent.getScopeDefinition();
            }
        }

        return scopeDefinition;
    }

    public boolean hasIsolateScope() {
        if (parent == null) {
            return true;
        }

        for (Directive directive : directives) {
            IsolateScope annotation = directive.getClass().getAnnotation(IsolateScope.class);
            if (annotation != null) {
                return true;
            }
        }
        return false;
    }

    public boolean hasScope() throws LayoutFileException {
        return getScopeDefinition().getOwner() == this;
    }

    public String getViewClassName() throws LayoutFileException {
        if (viewClass == null) {
            viewClass = findViewClass();
        }

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

    public boolean isDynamic() {
        if (isDynamic == null) {
            isDynamic = false;
            for (ViewNode child : children) {
                if (child.hasDynamicDirective()) {
                    isDynamic = true;
                    break;
                }
            }
        }
        return isDynamic;
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

        if (viewClass == null) {
            throw new NoViewClassForNode("Cannot find view class for node <" + tagName + " id = " + id + ">.");
        }

        return viewClass;
    }
}

// TODO: all node types should have an associated directive w/ tagmatcher, so if no directive found for a tag, should throw