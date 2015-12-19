package com.flarestar.drones.mvw.model.scope;

import com.flarestar.drones.mvw.processing.parser.exceptions.LayoutFileException;
import com.flarestar.drones.mvw.processing.parser.exceptions.ScopePropertyAlreadyDefined;
import com.flarestar.drones.mvw.processing.renderables.scope.ScopeEventListener;
import com.flarestar.drones.mvw.model.Directive;
import com.flarestar.drones.mvw.model.ViewNode;
import com.google.common.base.Predicate;
import com.google.common.collect.Maps;

import javax.annotation.Nullable;
import java.util.*;

// TODO: refactor whole code base for cleaner code

public class ScopeDefinition {

    private String scopeClassName;
    private final ViewNode owner;
    private final ScopeDefinition parentScope;
    public final boolean isIsolateScope;
    private final Map<String, Property> properties;
    private Boolean isPassthroughScope;
    private final Map<String, Property> ownProperties;
    private final Map<String, Property> inheritedProperties;

    // TODO: when calling apply() after an event, need to make sure only one apply ends up being scheduled
    public final List<ScopeEventListener> events = new ArrayList<>();

    public ScopeDefinition(ViewNode node, boolean isIsolateScope) throws LayoutFileException {
        this.properties = new HashMap<>();
        this.owner = node;
        this.isIsolateScope = isIsolateScope;

        if (node.parent == null) {
            parentScope = null;
        } else {
            parentScope = node.parent.scopeDefinition;
        }

        setScopeProperties(node);
        setScopeEvents(node);
        setInheritedScopeProperties(node.parent);
        setScopeClassName(node);

        ownProperties = Maps.filterValues(properties, new Predicate<Property>() {
            @Override
            public boolean apply(@Nullable Property property) {
                return !(property instanceof InheritedProperty);
            }
        });

        inheritedProperties = Maps.filterValues(properties, new Predicate<Property>() {
            @Override
            public boolean apply(@Nullable Property property) {
                return property instanceof InheritedProperty;
            }
        });
    }

    public String getScopeClassName() {
        return scopeClassName;
    }

    private void setScopeClassName(ViewNode node) {
        this.scopeClassName = node.id + "_Scope";
    }

    private void setScopeProperties(ViewNode node) throws LayoutFileException {
        for (Directive directive : node.directives) {
            processDirectiveProperties(directive);
        }
    }

    private void setScopeEvents(ViewNode node) {
        for (Directive directive : node.directives) {
            for (ScopeEventListener event : directive.getEvents()) {
                events.add(event);
            }
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

    private void processDirectiveProperties(Directive directive) throws LayoutFileException {
        List<Property> directiveProperties = directive.getScopeProperties();
        for (Property property : directiveProperties) {
            if (properties.containsKey(property.name)) {
                Property originalProperty = properties.get(property.name);
                if (!(originalProperty instanceof InheritedProperty)) {
                    throw new ScopePropertyAlreadyDefined(property.name, originalProperty.source.getDirectiveName(),
                        directive.getDirectiveName());
                }
            }

            properties.put(property.name, property);
        }
    }

    public boolean isPassthroughScope() {
        if (isPassthroughScope == null) {
            if (isIsolateScope) {
                isPassthroughScope = false;
            } else {
                isPassthroughScope = ownProperties().size() == 0 && events.size() == 0;
            }
        }

        return isPassthroughScope;
    }

    public Map<String, Property> allProperties() {
        return properties;
    }

    public Map<String, Property> ownProperties() {
        return ownProperties;
    }

    public Map<String, Property> inheritedProperties() {
        return inheritedProperties;
    }

    private void setInheritedScopeProperties(ViewNode node) {
        if (node == null) {
            return;
        }

        for (Property property : node.scopeDefinition.properties.values()) {
            if (properties.containsKey(property.name)) {
                continue;
            }

            properties.put(property.name, new InheritedProperty(node.scopeDefinition, property));
        }

        setInheritedScopeProperties(node.parent);
    }

    public void addProperty(Property property) {
        properties.put(property.name, property);
    }
}