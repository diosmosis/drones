package com.flarestar.drones.mvw.renderables.viewfactory;

import com.flarestar.drones.mvw.renderables.makeview.MakeViewBody;

/**
 * TODO
 */
public class SingleViewFactory extends ViewFactory {
    public SingleViewFactory(MakeViewBody makeViewBody) {
        super(makeViewBody);
    }

    @Override
    public String getTemplate() {
        return "templates/singleViewFactory.twig";
    }
}
