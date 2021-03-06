package com.flarestar.drones.mvw.processing.renderables;

import com.flarestar.drones.base.generation.Renderable;
import com.flarestar.drones.mvw.model.Directive;

/**
 * TODO
 */
public class DirectiveScopeInterface implements Renderable {

    private Directive directive;

    public DirectiveScopeInterface(Directive directive) {
        this.directive = directive;
    }

    @Override
    public String getTemplate() {
        return "templates/directiveScopeInterface.twig";
    }

    @Override
    public String getModelAttribute() {
        return "interface";
    }

    public Directive getDirective() {
        return directive;
    }
}
