package com.flarestar.drones.mvw.model.scope;

public class InheritedProperty extends Property {
    public final ScopeDefinition origin;
    public final Property property;

    public InheritedProperty(ScopeDefinition scopeDefinition, Property property) {
        super(property.name, property.type, null, null, false, property.source);

        this.origin = scopeDefinition;
        this.property = property;
    }
}
