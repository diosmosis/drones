package com.flarestar.drones.base;

import com.flarestar.drones.base.generation.jtwig.RenderAddon;
import com.flarestar.drones.base.generation.jtwig.SymbolsHack;
import com.flarestar.drones.base.inject.GenericInstanceFactory;
import com.flarestar.drones.base.inject.InstanceFactory;
import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.assistedinject.FactoryProvider;
import com.google.inject.name.Names;
import org.jtwig.configuration.JtwigConfiguration;
import org.jtwig.configuration.JtwigConfigurationBuilder;

import javax.annotation.processing.ProcessingEnvironment;

public class DroneModule extends AbstractModule {

    protected ProcessingEnvironment processingEnvironment;

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

    protected <F extends GenericInstanceFactory, V> void bindInstanceFactory(
            Class<F> instanceFactoryClass, Class<V> baseClass, Class<? extends V> implementationClass) {
        Key<F> key = Key.get(instanceFactoryClass, Names.named(implementationClass.getName()));
        install(new FactoryModuleBuilder()
            .implement(baseClass, implementationClass)
            .build(key));
    }
}
