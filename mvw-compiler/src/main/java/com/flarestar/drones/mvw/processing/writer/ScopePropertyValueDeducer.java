package com.flarestar.drones.mvw.processing.writer;

import com.flarestar.drones.mvw.processing.parser.exceptions.MissingRequiredAttribute;
import com.flarestar.drones.mvw.view.ViewNode;
import com.flarestar.drones.mvw.view.scope.Property;
import com.google.inject.Inject;
import org.json.JSONObject;

/**
 * TODO
 */
public class ScopePropertyValueDeducer {

    private Interpolator interpolator;

    @Inject
    public ScopePropertyValueDeducer(Interpolator interpolator) {
        this.interpolator = interpolator;
    }


    public String getInitialValueExpression(Property property, ViewNode node) throws MissingRequiredAttribute {
        if (node == null
            && property.bindType != Property.BindType.NONE
            && property.bindType != Property.BindType.PARENT_CHILD
        ) {
            throw new IllegalArgumentException("getInitialValueExpression cannot be called with null ViewNode for property '"
                + property.type + " " + property.name + "' since it has a bind type of " + property.bindType.toString());
        }

        switch (property.bindType) {
            case NONE:
            case PARENT_CHILD:
                return property.initialValue;
            case RAW_ATTR_VALUE:
                String value = node.element.attr(property.initialValue);
                value = interpolator.interpolate(value);
                return JSONObject.quote(value);
            case EXPRESSION_VALUE:
            case LOCAL_VAR:
                return node.element.attr(property.initialValue);
            case EXPRESSION_EVAL:
                String code = node.element.attr(property.initialValue);
                if (code ==  null) {
                    return null;
                }

                // TODO: if return is in a string literal in the code, this will fail
                if (code.contains("return")) {
                    return generateCallable(code);
                } else {
                    return generateRunnable(code);
                }
        }

        return null;
    }

    private String generateRunnable(String code) {
        StringBuilder builder = new StringBuilder();
        builder.append("new Runnable() {\n");
        builder.append("    void run() {\n");

        {
            builder.append("        ");
            builder.append(code);
            builder.append('\n');
        }

        builder.append("    }\n");
        builder.append("}\n");
        return builder.toString();
    }

    private String generateCallable(String code) {
        StringBuilder builder = new StringBuilder();
        builder.append("new Callable() {\n");
        builder.append("    Object call() {\n");

        {
            builder.append("        return");
            builder.append(code);
            builder.append('\n');
        }

        builder.append("    }\n");
        builder.append("}\n");
        return builder.toString();
    }
}
