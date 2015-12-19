package com.flarestar.drones.mvw.processing.renderables.viewfactory;

import com.flarestar.drones.mvw.model.ViewNode;
import com.flarestar.drones.mvw.processing.renderables.makeview.MakeViewBody;

import javax.lang.model.type.TypeMirror;

/**
 * TODO
 */
public abstract class RangeViewFactory extends ViewFactory {
    public RangeViewFactory(MakeViewBody makeViewBody, com.flarestar.drones.mvw.model.ViewFactory viewFactoryModel) {
        super(makeViewBody, viewFactoryModel);
    }

    @Override
    public String getTemplate() {
        return "templates/rangeViewFactory.twig";
    }
}
