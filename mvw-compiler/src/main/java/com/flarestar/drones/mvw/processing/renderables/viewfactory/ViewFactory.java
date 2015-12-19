package com.flarestar.drones.mvw.processing.renderables.viewfactory;

import com.flarestar.drones.base.generation.Renderable;
import com.flarestar.drones.base.inject.GenericInstanceFactory;
import com.flarestar.drones.base.inject.InstanceFactory;
import com.flarestar.drones.mvw.processing.renderables.makeview.MakeViewBody;
import com.google.inject.Inject;

/**
 * TODO
 */
@InstanceFactory(ViewFactory.InstanceFactory.class)
public abstract class ViewFactory implements Renderable {

    public interface InstanceFactory extends GenericInstanceFactory<ViewFactory, MakeViewBody> {
    }

    private MakeViewBody makeViewBody;

    protected ViewFactory(MakeViewBody makeViewBody) {
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
