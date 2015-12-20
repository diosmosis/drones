package com.flarestar.drones.mvw.model.scope;

import com.flarestar.drones.mvw.processing.parser.exceptions.LayoutFileException;
import com.flarestar.drones.mvw.processing.parser.exceptions.ScopePropertyAlreadyDefined;
import com.flarestar.drones.mvw.processing.renderables.scope.ScopeEventListener;
import com.flarestar.drones.mvw.model.Directive;
import com.flarestar.drones.mvw.model.ViewNode;
import com.flarestar.drones.mvw.processing.renderables.scope.WatcherDefinition;
import com.flarestar.drones.routing.ActivityRouter;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
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
    public final List<WatcherDefinition> watchers = new ArrayList<>();

    public ScopeDefinition(ViewNode node, boolean isIsolateScope) throws LayoutFileException {
        this.properties = new HashMap<>();
        this.owner = node;
        this.isIsolateScope = isIsolateScope;

        if (node.parent == null) {
            parentScope = null;

            addRootProperties();
        } else {
            parentScope = node.parent.scopeDefinition;
        }

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

    private void addRootProperties() {
        properties.put("$location",
            new Property("$location", ActivityRouter.class.getName(), Property.BindType.NONE, "", true, null));
    }

    public String getScopeClassName() {
        return scopeClassName;
    }

    private void setScopeClassName(ViewNode node) {
        this.scopeClassName = node.id + "_Scope";
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

    public boolean isPassthroughScope() {
        if (isPassthroughScope == null) {
            if (isIsolateScope) {
                isPassthroughScope = false;
            } else {
                isPassthroughScope = ownProperties().isEmpty() && events.isEmpty() && watchers.isEmpty();
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
        if (properties.containsKey(property.name)) {
            Property originalProperty = properties.get(property.name);
            if (!(originalProperty instanceof InheritedProperty)) {
                throw new ScopePropertyAlreadyDefined(property.name, originalProperty.source.getDirectiveName());
            }
        }

        properties.put(property.name, property);
    }

    public Property getProperty(String name) {
        Property result = properties.get(name);
        if (result == null) {
            throw new IllegalArgumentException("Invalid property name '" + name + "'. (This should not happen).");
        }
        return result;
    }

    public Collection<Property> boundProperties() {
        return Collections2.filter(properties.values(), new Predicate<Property>() {
            @Override
            public boolean apply(@Nullable Property property) {
                return property.hasBinding();
            }
        });
    }

    public Collection<WatcherDefinition> getParentScopeDirectiveWatchers() {
        return Collections2.filter(watchers, new Predicate<WatcherDefinition>() {
            @Override
            public boolean apply(@Nullable WatcherDefinition watcherDefinition) {
                return watcherDefinition.isOnParentScope();
            }
        });
    }

    public Collection<WatcherDefinition> getThisScopeDirectiveWatchers() {
        return Collections2.filter(watchers, new Predicate<WatcherDefinition>() {
            @Override
            public boolean apply(@Nullable WatcherDefinition watcherDefinition) {
                return !watcherDefinition.isOnParentScope();
            }
        });
    }
}
