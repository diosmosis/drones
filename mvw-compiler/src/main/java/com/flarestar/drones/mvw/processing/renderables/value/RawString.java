package com.flarestar.drones.mvw.processing.renderables.value;

import com.flarestar.drones.base.generation.Renderable;

/**
 * TODO
 */
public class RawString implements Renderable {

    private String value;

    public RawString(String value) {
        this.value = value;
    }

    @Override
    public String getTemplate() {
        return "templates/rawString.twig";
    }

    @Override
    public String getModelAttribute() {
        return "o";
    }

    public String getValue() {
        return value;
    }
}
