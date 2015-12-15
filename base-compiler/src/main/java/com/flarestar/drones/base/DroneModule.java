package com.flarestar.drones.base;

import com.flarestar.drones.base.generation.jtwig.RenderAddon;
import com.flarestar.drones.base.generation.jtwig.SymbolsHack;
import com.google.inject.AbstractModule;
import com.google.inject.Provider;
import com.google.inject.Provides;
import org.jtwig.configuration.JtwigConfiguration;
import org.jtwig.configuration.JtwigConfigurationBuilder;

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

    @Provides
    JtwigConfiguration provideJtwigConfiguration(SymbolsHack symbolsHack) {
        return JtwigConfigurationBuilder.newConfiguration().withSymbols(symbolsHack).withAddon(RenderAddon.class).build();
    }
}
