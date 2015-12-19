package com.flarestar.drones.mvw.processing.renderables.viewfactory;

import com.flarestar.drones.base.generation.Renderable;
import com.flarestar.drones.base.inject.GenericInstanceFactory;
import com.flarestar.drones.base.inject.InstanceFactory;
import com.flarestar.drones.mvw.model.ViewNode;
import com.flarestar.drones.mvw.processing.renderables.makeview.MakeViewBody;
import com.google.inject.Inject;

/**
 * TODO
 */
@InstanceFactory(ViewFactory.InstanceFactory.class)
public abstract class ViewFactory implements Renderable {

    public interface InstanceFactory extends GenericInstanceFactory<ViewFactory> {
    }

    private MakeViewBody makeViewBody;

    protected ViewFactory(MakeViewBody makeViewBody, com.flarestar.drones.mvw.model.ViewFactory viewFactoryModel) {
        this.makeViewBody = makeViewBody;
    }

    public MakeViewBody getMakeViewBody() {
        return makeViewBody;
    }

    @Override
    public String getModelAttribute() {
        return "factory";
    }
}
