package com.flarestar.drones.mvw.renderables;

import com.flarestar.drones.base.generation.Renderable;
import com.flarestar.drones.mvw.function.FunctionDefinition;

/**
 * TODO
 */
public class TemplateFunctionProxyCode implements Renderable {
    private FunctionDefinition function;

    public TemplateFunctionProxyCode(FunctionDefinition function) {
        this.function = function;
    }

    @Override
    public String getTemplate() {
        return "templates/templateFunctionProxyCode.twig";
    }

    @Override
    public String getModelAttribute() {
        return "proxy";
    }

    public FunctionDefinition getFunction() {
        return function;
    }
}
