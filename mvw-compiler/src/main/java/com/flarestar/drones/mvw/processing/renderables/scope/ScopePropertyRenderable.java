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
    private boolean initializeToLocalValue;
    private String codeToAccess;
    private Renderable initialValue;
    private boolean hasBidirectionalBinding;
    private boolean canInitializeInScopeConstructor;
    private boolean isInjected;

    public ScopePropertyRenderable(String type, String name, boolean hasBidirectionalBinding,
                                   boolean canInitializeInScopeConstructor, boolean initializeToLocalValue,
                                   String codeToAccess, Renderable initialValue, boolean isInjected) {
        this.type = type;
        this.name = name;
        this.hasBidirectionalBinding = hasBidirectionalBinding;
        this.canInitializeInScopeConstructor = canInitializeInScopeConstructor;
        this.initializeToLocalValue = initializeToLocalValue;
        this.codeToAccess = codeToAccess;
        this.initialValue = initialValue;
        this.isInjected = isInjected;
    }

    public Renderable getInitialValue() {
        return initialValue;
    }

    public boolean hasBidirectionalBinding() {
        return hasBidirectionalBinding;
    }

    public boolean initializeToLocalValue() {
        return initializeToLocalValue;
    }

    public boolean canInitializeInScopeConstructor() {
        return canInitializeInScopeConstructor && initialValue != null;
    }

    public String getCodeToAccess() {
        return codeToAccess;
    }

    public boolean isInjected() {
        return isInjected;
    }
}
