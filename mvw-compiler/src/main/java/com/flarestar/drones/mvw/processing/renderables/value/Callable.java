package com.flarestar.drones.mvw.processing.renderables.value;

import com.flarestar.drones.base.generation.Renderable;

/**
 * TODO
 */
public class Callable implements Renderable {
    private String code;

    public Callable(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    @Override
    public String getTemplate() {
        return "templates/callable.twig";
    }

    @Override
    public String getModelAttribute() {
        return "callable";
    }
}
