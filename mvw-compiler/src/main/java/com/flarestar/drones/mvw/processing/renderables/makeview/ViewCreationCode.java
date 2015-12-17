package com.flarestar.drones.mvw.processing.renderables.makeview;

import com.flarestar.drones.base.generation.Renderable;
import com.flarestar.drones.mvw.model.ViewNode;
import com.flarestar.drones.mvw.model.ViewProperty;

import java.util.List;

/**
 * TODO
 */
public class ViewCreationCode implements Renderable {
    private String viewClassName;
    private List<ViewProperty> viewProperties;

    public ViewCreationCode(ViewNode node) {
        this.viewClassName = node.getViewClassName();
        this.viewProperties = node.viewProperties;
    }

    @Override
    public String getTemplate() {
        return "templates/viewCreationCode.twig";
    }

    @Override
    public String getModelAttribute() {
        return "code";
    }

    public List<ViewProperty> getViewProperties() {
        return viewProperties;
    }

    public String getViewClassName() {
        return viewClassName;
    }
}
