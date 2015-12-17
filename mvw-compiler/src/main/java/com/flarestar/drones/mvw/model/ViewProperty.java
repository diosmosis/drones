package com.flarestar.drones.mvw.model;

import com.flarestar.drones.base.generation.Renderable;

/**
 * TODO
 */
public class ViewProperty implements Renderable {
    private String viewMethodName;
    private String[] viewMethodArgs;

    public ViewProperty(String viewMethodName, String... viewMethodArgs) {
        this.viewMethodName = viewMethodName;
        this.viewMethodArgs = viewMethodArgs;
    }

    public String[] getViewMethodArgs() {
        return viewMethodArgs;
    }

    public String getViewMethodName() {
        return viewMethodName;
    }

    @Override
    public String getTemplate() {
        return "templates/viewProperty.twig";
    }

    @Override
    public String getModelAttribute() {
        return "property";
    }
}
