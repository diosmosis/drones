package com.flarestar.drones.base.inject;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;

/**
 * TODO
 */
public class GuiceFactoryDelegate {
    private Injector injector;

    @Inject
    public GuiceFactoryDelegate(Injector injector) {
        this.injector = injector;
    }

    public <T> T make(Class<T> target, Object... args) {
        InstanceFactory annotation = target.getAnnotation(InstanceFactory.class);
        if (annotation == null) {
            throw new IllegalArgumentException("Class '" + target.getName() +
                "' is not annotated w/ @InstanceFactory, so we can't create an instance of it by class.");
        }

        Class<? extends GenericInstanceFactory<?>> factoryClass = annotation.value();

        Key<? extends GenericInstanceFactory> key = Key.get(factoryClass, Names.named(target.getName()));
        GenericInstanceFactory factory = injector.getInstance(key);

        return (T)factory.make(args);
    }
}
