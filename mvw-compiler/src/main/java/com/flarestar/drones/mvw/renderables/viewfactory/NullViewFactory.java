package com.flarestar.drones.mvw.renderables.viewfactory;

import com.flarestar.drones.mvw.renderables.makeview.MakeViewBody;

/**
 * TODO
 */
public class NullViewFactory extends ViewFactory {
    public NullViewFactory(MakeViewBody makeViewBody) {
        super(makeViewBody);
    }

    @Override
    public String getTemplate() {
        return "templates/nullViewFactory.twig"; // TODO
    }
}
