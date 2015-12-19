package com.flarestar.drones.mvw.processing.renderables.viewfactory;

import com.flarestar.drones.mvw.processing.renderables.makeview.MakeViewBody;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

/**
 * TODO
 */
public class NullViewFactory extends ViewFactory {
    @AssistedInject
    public NullViewFactory(@Assisted MakeViewBody makeViewBody) {
        super(makeViewBody);
    }

    @Override
    public String getTemplate() {
        return "templates/nullViewFactory.twig"; // TODO
    }
}
