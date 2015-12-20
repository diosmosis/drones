package com.flarestar.drones.base.renderables;

import com.flarestar.drones.base.ScreenDroneSniffer;
import com.flarestar.drones.base.generation.ClassRenderable;

import javax.lang.model.element.TypeElement;
import java.util.List;

/**
 * TODO
 */
public class ActivityComponent extends GeneratedClassModel implements ClassRenderable {
    private ActivityModule module;
    private List<ScreenDroneSniffer.DroneInformation> drones;
    private List<String> extraComponentMethods;

    public ActivityComponent(TypeElement element, ActivityModule module, List<ScreenDroneSniffer.DroneInformation> drones,
                             List<String> extraComponentMethods) {
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

    public List<String> getExtraComponentMethods() {
        return extraComponentMethods;
    }
}
