package com.flarestar.drones.mvw.processing.renderables.scope;

import com.flarestar.drones.base.generation.Renderable;
import com.flarestar.drones.mvw.model.scope.Property;
import com.flarestar.drones.mvw.processing.writer.Interpolator;
import com.flarestar.drones.mvw.processing.writer.ScopePropertyValueDeducer;

/**
 * TODO
 */
public class ScopePropertyRenderable {
    public String type;
    public String name;
    private Renderable initialValue;
    private boolean hasBidirectionalBinding;
    private boolean canInitializeInScopeConstructor;

    public ScopePropertyRenderable(String type, String name, boolean hasBidirectionalBinding,
                                   boolean canInitializeInScopeConstructor, Renderable initialValue) {
        this.type = type;
        this.name = name;
        this.hasBidirectionalBinding = hasBidirectionalBinding;
        this.canInitializeInScopeConstructor = canInitializeInScopeConstructor;
        this.initialValue = initialValue;
    }

    public Renderable getInitialValue() {
        return initialValue;
    }

    public boolean hasBidirectionalBinding() {
        return hasBidirectionalBinding;
    }

    public boolean canInitializeInScopeConstructor() {
        return canInitializeInScopeConstructor && initialValue != null;
    }
}
