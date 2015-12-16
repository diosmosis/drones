package com.flarestar.drones.mvw;

import com.asual.lesscss.LessEngine;
import com.flarestar.drones.base.generation.jtwig.RenderAddon;
import com.flarestar.drones.base.generation.jtwig.SymbolsHack;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.helger.css.ECSSVersion;
import com.helger.css.writer.CSSWriterSettings;
import org.jtwig.configuration.JtwigConfiguration;
import org.jtwig.configuration.JtwigConfigurationBuilder;

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

    // TODO: code redundancy w/ base-compiler DroneModule
    @Provides
    JtwigConfiguration provideJtwigConfiguration(SymbolsHack symbolsHack) {
        return JtwigConfigurationBuilder.newConfiguration().withSymbols(symbolsHack).withAddon(RenderAddon.class).build();
    }
}
