package com.flarestar.drones.base.inject;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * TODO
 */
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface InstanceFactory {
    Class<? extends GenericInstanceFactory<?>> value();
}
