package com.flarestar.drones.mvw.model;

/**
 * TODO
 */
public class ViewFactory {
    private Class<? extends com.flarestar.drones.mvw.processing.renderables.viewfactory.ViewFactory> renderableClass;

    public ViewFactory(Class<? extends com.flarestar.drones.mvw.processing.renderables.viewfactory.ViewFactory> renderableClass) {
        this.renderableClass = renderableClass;
    }

    public Class<? extends com.flarestar.drones.mvw.processing.renderables.viewfactory.ViewFactory> getRenderableClass() {
        return renderableClass;
    }
}
