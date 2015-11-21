package com.flarestar.drones.layout.view.scope;

import com.flarestar.drones.layout.compilerutilities.TypeInferer;
import com.flarestar.drones.layout.compilerutilities.exceptions.*;
import com.flarestar.drones.layout.parser.exceptions.InvalidPropertyDescriptor;
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
    private static final Pattern EXPRESSION_START_REGEX = Pattern.compile("([a-zA-Z0-9_$]+)(.*)");

    public static class Property {
        private static final Pattern PROPERTY_DESCRIPTOR_REGEX = Pattern.compile("(\\w+)\\s+(\\w+)\\s*(?:=\\s*(.+))?");

        public final String name;
        public final String type;
        public final String initialValueExpression;
        public final Directive source;

        public Property(String name, String type, String initialValueExpression, Directive source) {
            this.name = name;
            this.type = type;
            this.initialValueExpression = initialValueExpression;
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
            Matcher m = PROPERTY_DESCRIPTOR_REGEX.matcher(propertyDescriptor);
            if (!m.matches()) {
                throw new InvalidPropertyDescriptor(propertyDescriptor, directive.getDirectiveName());
            }

            return new Property(m.group(2), m.group(1), m.group(3), directive);
        }
    }

    private String scopeClassName;
    private ViewNode owner;
    private ScopeDefinition parentScope;
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

    public ScopeDefinition getParentScope() throws LayoutFileException {
        if (parentScope == null) {
            // returns the scope of the first view node parent that isn't this scope
            ViewNode node = owner.parent;
            while (node != null) {
                ScopeDefinition definition = node.getScopeDefinition();
                if (definition != this && definition != null) {
                    parentScope = definition;
                    return definition;
                }

                node = node.parent;
            }

            throw new IllegalStateException("Unexpected state: missing root scope.");
        }

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

    public TypeMirror getTypeOfExpression(String expression)
            throws InvalidTypeExpression, InvalidExpression, InvalidTypeException, CannotFindProperty, CannotFindMethod {
        TypeInferer inferer = TypeInferer.getInstance(); // TODO: shouldn't be singleton.

        if (expression.startsWith("scope.")) {
            expression = expression.substring(6);
        }

        TypeMirror type = null;

        ScopeDefinition scope = this;
        while (scope != null) {
            Matcher m = EXPRESSION_START_REGEX.matcher(expression);
            if (!m.matches()) {
                throw new RuntimeException("Unexpected error, cannot parse expression start: " + expression);
            }

            String propertyName = m.group(1);
            Property property = scope.properties.get(propertyName);
            if (property == null) {
                throw new InvalidTypeExpression(expression, "no property named '" + propertyName + "'");
            }
            expression = m.group(2);

            // TODO: if someone stores a scope as a property in a scope, this won't work out.
            if (property.type.equals("_parent")) {
                scope = parentScope;
            } else {
                type = inferer.getTypeMirrorFor(property.type);
                scope = null;
            }
        }

        if (type == null) {
            // TODO: We can't get a TypeMirror from a generic string, eg List<String> and we can't get
            //       TypeMirrors for Scope types, so for now disabling using scope types as results in expressions.
            throw new InvalidTypeExpression(expression, "scope types not allowed in this context");
        }

        return inferer.getTypeOfExpression(type, expression, this);
    }
}
