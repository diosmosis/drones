package com.flarestar.drones.mvw;

import com.asual.lesscss.LessEngine;
import com.google.inject.AbstractModule;
import com.helger.css.ECSSVersion;
import com.helger.css.writer.CSSWriterSettings;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;

public class DroneModule extends AbstractModule {

    private ProcessingEnvironment processingEnvironment;
    private RoundEnvironment roundEnvironment;

    public DroneModule(ProcessingEnvironment processingEnvironment, RoundEnvironment roundEnvironment) {
        this.processingEnvironment = processingEnvironment;
        this.roundEnvironment = roundEnvironment;
    }

    @Override
    protected void configure() {
        bind(ProcessingEnvironment.class).toInstance(processingEnvironment);
        bind(RoundEnvironment.class).toInstance(roundEnvironment);
        bind(CSSWriterSettings.class).toInstance(new CSSWriterSettings(ECSSVersion.CSS30));
        bind(LessEngine.class).toInstance(new LessEngine());
    }
}
