package com.flarestar.drones.mvw.processing.renderables.viewfactory;

import com.flarestar.drones.mvw.model.ViewNode;
import com.flarestar.drones.mvw.processing.renderables.makeview.MakeViewBody;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

/**
 * TODO
 */
public class NullViewFactory extends ViewFactory {
    public NullViewFactory(MakeViewBody makeViewBody, com.flarestar.drones.mvw.model.ViewFactory viewFactoryModel) {
        super(makeViewBody, viewFactoryModel);
    }

    @AssistedInject
    public NullViewFactory(@Assisted Object[] args) {
        this((MakeViewBody)args[0], (com.flarestar.drones.mvw.model.ViewFactory) args[1]);
    }

    @Override
    public String getTemplate() {
        return "templates/nullViewFactory.twig";
    }
}
