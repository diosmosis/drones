package com.flarestar.drones.layout.view.scope;

public class InheritedProperty extends Property {
    public final ScopeDefinition origin;
    public final Property property;

    public InheritedProperty(ScopeDefinition scopeDefinition, Property property) {
        super(property.name, property.type, property.initialValueExpression, property.source);

        this.origin = scopeDefinition;
        this.property = property;
    }

    @Override
    public String accessCode() {
        StringBuilder builder = new StringBuilder();

        builder.append("scope.");

        Property current = this;
        while (current instanceof InheritedProperty) {
            builder.append("_parent.");
            current = ((InheritedProperty)current).property;
        }

        builder.append(name);
        return builder.toString();
    }
}
