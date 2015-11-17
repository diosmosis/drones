package com.flarestar.drones.layout.view;

import com.flarestar.drones.layout.annotations.directive.DirectiveView;
import com.flarestar.drones.layout.annotations.directive.IsolateScope;
import com.flarestar.drones.layout.annotations.directive.ScopeProperties;
import com.flarestar.drones.layout.parser.exceptions.InvalidPropertyDescriptor;
import com.flarestar.drones.layout.parser.exceptions.LayoutFileException;
import com.flarestar.drones.layout.view.scope.ScopeDefinition;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO
 */
public abstract class Directive {

    public void onViewCreated(ViewNode node, StringBuilder result) throws LayoutFileException {
        // empty
    }

    public abstract String getDirectiveName();

    @Override
    public int hashCode() {
        return getDirectiveName().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Directive && getDirectiveName() == ((Directive)obj).getDirectiveName();
    }

    public String getViewClassName() {
        DirectiveView annotation = getClass().getAnnotation(DirectiveView.class);
        return annotation == null ? null : annotation.view().getName();
    }

    public String getOnViewCreatedCode(ViewNode node) throws LayoutFileException {
        StringBuilder builder = new StringBuilder();
        onViewCreated(node, builder);
        return builder.toString();
    }

    public List<ScopeDefinition.Property> getScopeProperties(ViewNode node) throws LayoutFileException {
        List<ScopeDefinition.Property> result = new ArrayList<>();

        ScopeProperties annotation = getClass().getAnnotation(ScopeProperties.class);
        if (annotation == null) {
            return result;
        }

        for (String propertyDescriptor : annotation.value()) {
            ScopeDefinition.Property property = ScopeDefinition.Property.makeFromDescriptor(propertyDescriptor, this);
            result.add(property);
        }

        return result;
    }
}
