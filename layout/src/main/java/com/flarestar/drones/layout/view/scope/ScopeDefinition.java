package com.flarestar.drones.layout.view.scope;

import com.flarestar.drones.layout.compilerutilities.TypeInferer;
import com.flarestar.drones.layout.compilerutilities.exceptions.*;
import com.flarestar.drones.layout.parser.exceptions.LayoutFileException;
import com.flarestar.drones.layout.parser.exceptions.ScopePropertyAlreadyDefined;
import com.flarestar.drones.layout.view.Directive;
import com.flarestar.drones.layout.view.ViewNode;

import javax.lang.model.type.TypeMirror;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// TODO: refactor whole code base for cleaner code

public class ScopeDefinition {
    private String scopeClassName;
    private final ViewNode owner;
    private final ScopeDefinition parentScope;
    public final boolean isIsolateScope;
    public final Map<String, Property> properties;
    private Boolean isPassthroughScope;

    public ScopeDefinition(ViewNode node, boolean isIsolateScope) throws LayoutFileException {
        this.properties = new HashMap<>();
        this.owner = node;
        this.isIsolateScope = isIsolateScope;

        if (node.parent == null) {
            parentScope = null;
        } else {
            parentScope = node.parent.getScopeDefinition();
        }

        setScopeProperties(node);
        setScopeClassName(node);
    }

    public String getScopeClassName() {
        return scopeClassName;
    }

    private void setScopeClassName(ViewNode node) {
        if (properties.isEmpty()) {
            this.scopeClassName = "Scope";
        } else {
            this.scopeClassName = node.id + "_Scope";
        }
    }

    public void setScopeProperties(ViewNode node) throws LayoutFileException {
        processDirectives(node); // TODO: not necessary to have two methods here
    }

    public void processDirectives(ViewNode node) throws LayoutFileException {
        for (Directive directive : node.directives) {
            processDirectiveProperties(properties, directive);
        }
    }

    public ViewNode getParentScopeOwner() {
        return parentScope.getOwner();
    }

    public ViewNode getOwner() {
        return owner;
    }

    public ScopeDefinition getParentScope() {
        return parentScope;
    }

    private void processDirectiveProperties(Map<String, Property> properties, Directive directive)
            throws LayoutFileException {
        List<Property> directiveProperties = directive.getScopeProperties();
        for (Property property : directiveProperties) {
            if (properties.containsKey(property.name)) {
                Property originalProperty = properties.get(property.name);
                throw new ScopePropertyAlreadyDefined(property.name, originalProperty.source.getDirectiveName(),
                    directive.getDirectiveName());
            }

            properties.put(property.name, property);
        }
    }

    public boolean isPassthroughScope() {
        if (isPassthroughScope == null) {
            if (isIsolateScope) {
                isPassthroughScope = false;
            } else {
                isPassthroughScope = properties.size() == 0;
            }
        }

        return isPassthroughScope;
    }
}
