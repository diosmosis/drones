package com.flarestar.drones.base;

import com.google.inject.AbstractModule;

import javax.annotation.processing.ProcessingEnvironment;

public class DroneModule extends AbstractModule {

    private ProcessingEnvironment processingEnvironment;

    public DroneModule(ProcessingEnvironment processingEnvironment) {
        this.processingEnvironment = processingEnvironment;
    }

    @Override
    protected void configure() {
        bind(ProcessingEnvironment.class).toInstance(processingEnvironment);
    }
}
