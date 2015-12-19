package com.flarestar.drones.mvw.processing.writer;

import com.flarestar.drones.base.generation.Renderable;
import com.flarestar.drones.mvw.model.ViewNode;
import com.flarestar.drones.mvw.model.scope.Property;
import com.flarestar.drones.mvw.processing.renderables.value.Callable;
import com.flarestar.drones.mvw.processing.renderables.value.RawString;
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


    public Renderable getInitialValueRenderable(Property property, ViewNode node) {
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
            case LOCAL_VAR:
                return new RawString(property.initialValue);
            case RAW_ATTR_VALUE:
                String value = node.element.attr(property.initialValue);
                value = interpolator.interpolate(value);
                return new RawString(JSONObject.quote(value));
            case EXPRESSION_VALUE:
                return new RawString(node.element.attr(property.initialValue));
            case EXPRESSION_EVAL:
                String code = node.element.attr(property.initialValue);
                if (code == null) {
                    return null;
                }

                // TODO: if return is in a string literal in the code, this will fail
                if (code.contains("return")) {
                    return new Callable(code);
                } else {
                    return new com.flarestar.drones.mvw.processing.renderables.value.Runnable(code);
                }
        }

        return null;
    }
}
