package com.flarestar.drones.mvw.renderables.makeview;

import com.flarestar.drones.mvw.view.Directive;
import com.flarestar.drones.mvw.view.ViewNode;

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
