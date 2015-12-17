package com.flarestar.drones.mvw.processing.renderables.makeview;

import com.flarestar.drones.mvw.model.Directive;
import com.flarestar.drones.mvw.model.ViewNode;

/**
 * TODO
 */
public class DirectiveMakeViewBody extends MakeViewBody {
    public DirectiveMakeViewBody(ViewNode view, Directive currentIsolateDirective) {
        super(view, currentIsolateDirective);
    }

    @Override
    public String getTemplate() {
        return "templates/directiveMakeViewBody.twig";
    }
}
