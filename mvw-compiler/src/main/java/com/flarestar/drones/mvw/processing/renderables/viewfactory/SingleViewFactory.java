package com.flarestar.drones.mvw.processing.renderables.viewfactory;

import com.flarestar.drones.mvw.processing.renderables.makeview.MakeViewBody;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

/**
 * TODO
 */
public class SingleViewFactory extends ViewFactory {
    @AssistedInject
    public SingleViewFactory(@Assisted MakeViewBody makeViewBody) {
        super(makeViewBody);
    }

    @Override
    public String getTemplate() {
        return "templates/singleViewFactory.twig";
    }
}
