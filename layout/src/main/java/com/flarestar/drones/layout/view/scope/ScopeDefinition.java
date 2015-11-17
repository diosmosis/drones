package com.flarestar.drones.layout.view.scope;

import com.flarestar.drones.layout.parser.exceptions.InvalidPropertyDescriptor;
import com.flarestar.drones.layout.parser.exceptions.LayoutFileException;
import com.flarestar.drones.layout.parser.exceptions.ScopePropertyAlreadyDefined;
import com.flarestar.drones.layout.view.Directive;
import com.flarestar.drones.layout.view.ViewNode;

import java.util.*;

// TODO: refactor whole code base for cleaner code

public class ScopeDefinition {

    public static class Property {
        public final String name;
        public final String type;
        public final Directive source;

        public Property(String name, String type, Directive source) {
            this.name = name;
            this.type = type;
            this.source = source;
        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Property property = (Property) o;
            return name.equals(property.name);
        }

        public static Property makeFromDescriptor(String propertyDescriptor, Directive directive)
                throws InvalidPropertyDescriptor {
            String[] parts = propertyDescriptor.trim().split("\\s*", 2);
            if (parts.length != 2) {
                throw new InvalidPropertyDescriptor(propertyDescriptor, directive.getDirectiveName());
            }

            return new Property(parts[1], parts[0], directive);
        }
    }

    private String scopeClassName;
    private ViewNode parentScopeOwner;
    private ViewNode owner;
    public final Map<String, Property> properties;

    public ScopeDefinition(ViewNode node) throws LayoutFileException {
        this.properties = new HashMap<>();
        this.owner = node;

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
        if (!properties.containsKey("_parent") && node.parent != null) {
            String scopeType = getParentScope(node).getScopeClassName();
            properties.put("_parent", new Property("_parent", scopeType, null));
        }

        processDirectives(node);
    }

    public void processDirectives(ViewNode node) throws LayoutFileException {
        for (Directive directive : node.directives) {
            processDirectiveProperties(properties, directive, node);
        }
    }

    public ViewNode getParentScopeOwner() {
        return parentScopeOwner;
    }

    public ViewNode getOwner() {
        return owner;
    }

    private ScopeDefinition getParentScope(ViewNode node) throws LayoutFileException {
        // returns the scope of the first view node parent that isn't this scope
        node = node.parent;
        while (node != null) {
            ScopeDefinition definition = node.getScopeDefinition();
            if (definition != this && definition != null) {
                parentScopeOwner = node;
                return definition;
            }

            node = node.parent;
        }

        throw new IllegalStateException("Unexpected state: missing root scope.");
    }

    private void processDirectiveProperties(Map<String, Property> properties, Directive directive, ViewNode node)
            throws LayoutFileException {
        List<Property> directiveProperties = directive.getScopeProperties(node);
        for (Property property : directiveProperties) {
            if (properties.containsKey(property.name)) {
                Property originalProperty = properties.get(property.name);
                throw new ScopePropertyAlreadyDefined(property.name, originalProperty.source.getDirectiveName(),
                    directive.getDirectiveName());
            }

            properties.put(property.name, property);
        }
    }
}
