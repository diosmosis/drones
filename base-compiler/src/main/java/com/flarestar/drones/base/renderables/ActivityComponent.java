package com.flarestar.drones.base.renderables;

import com.flarestar.drones.base.ScreenDroneSniffer;
import com.flarestar.drones.base.generation.ClassRenderable;

import javax.lang.model.element.TypeElement;
import java.util.Collection;
import java.util.List;

/**
 * TODO
 */
public class ActivityComponent extends GeneratedClassModel implements ClassRenderable {
    private ActivityModule module;
    private List<ScreenDroneSniffer.DroneInformation> drones;
    private Collection<String> extraComponentMethods;

    public ActivityComponent(TypeElement element, ActivityModule module, List<ScreenDroneSniffer.DroneInformation> drones,
                             Collection<String> extraComponentMethods) {
        super(element);

        this.module = module;
        this.drones = drones;
        this.extraComponentMethods = extraComponentMethods;
    }

    @Override
    protected String computeGeneratedClassName() {
        return getElement().getQualifiedName().toString() + "ActivityComponent";
    }

    @Override
    public String getTemplate() {
        return "templates/ActivityComponent.twig";
    }

    @Override
    public String getModelAttribute() {
        return "component";
    }

    public ActivityModule getModule() {
        return module;
    }

    public List<ScreenDroneSniffer.DroneInformation> getDrones() {
        return drones;
    }

    public Collection<String> getExtraComponentMethods() {
        return extraComponentMethods;
    }
}
