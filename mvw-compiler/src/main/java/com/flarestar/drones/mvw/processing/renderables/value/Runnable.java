package com.flarestar.drones.mvw.processing.renderables.value;

import com.flarestar.drones.base.generation.Renderable;

/**
 * TODO
 */
public class Runnable implements Renderable {
    private String code;

    public Runnable(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    @Override
    public String getTemplate() {
        return "templates/runnable.twig";
    }

    @Override
    public String getModelAttribute() {
        return "runnable";
    }
}
