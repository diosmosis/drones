package com.flarestar.drones.mvw.processing.renderables.scope;

import com.flarestar.drones.base.generation.Renderable;

/**
 * TODO
 */
public class ScopeFieldDeclaration implements Renderable {
    private String type;
    private String name;

    public ScopeFieldDeclaration(String type, String name) {
        this.type = type;
        this.name = name;
    }

    @Override
    public String getTemplate() {
        return "templates/scopeFieldDeclaration.twig";
    }

    @Override
    public String getModelAttribute() {
        return "declaration";
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }
}
