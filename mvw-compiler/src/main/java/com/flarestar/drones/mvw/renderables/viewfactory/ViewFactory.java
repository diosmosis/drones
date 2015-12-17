package com.flarestar.drones.mvw.renderables.viewfactory;

import com.flarestar.drones.base.generation.Renderable;
import com.flarestar.drones.mvw.renderables.makeview.MakeViewBody;

/**
 * TODO
 */
public abstract class ViewFactory implements Renderable {
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
