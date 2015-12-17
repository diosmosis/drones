package com.flarestar.drones.mvw.renderables.makeview;

import com.flarestar.drones.base.generation.Renderable;
import com.flarestar.drones.mvw.view.Directive;
import com.flarestar.drones.mvw.view.ViewNode;
import com.flarestar.drones.mvw.view.ViewProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO
 */
public class ViewCreationCode implements Renderable {
    private String viewClassName;
    private List<ViewProperty> viewProperties;

    public ViewCreationCode(ViewNode node) {
        this.viewClassName = node.getViewClassName();

        this.viewProperties = new ArrayList<>();
        for (Directive directive : node.directives) {
            viewProperties.addAll(directive.getViewProperties());
        }
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
