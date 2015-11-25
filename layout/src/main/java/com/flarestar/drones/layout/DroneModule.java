package com.flarestar.drones.layout;

import com.asual.lesscss.LessEngine;
import com.google.inject.AbstractModule;
import com.helger.css.ECSSVersion;
import com.helger.css.writer.CSSWriterSettings;

import javax.annotation.processing.ProcessingEnvironment;

public class DroneModule extends AbstractModule {

    private ProcessingEnvironment processingEnvironment;

    public DroneModule(ProcessingEnvironment processingEnvironment) {
        this.processingEnvironment = processingEnvironment;
    }

    @Override
    protected void configure() {
        bind(ProcessingEnvironment.class).toInstance(processingEnvironment);
        bind(CSSWriterSettings.class).toInstance(new CSSWriterSettings(ECSSVersion.CSS30));
        bind(LessEngine.class).toInstance(new LessEngine());
    }
}
