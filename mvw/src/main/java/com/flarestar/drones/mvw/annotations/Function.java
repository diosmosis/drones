package com.flarestar.drones.mvw.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * TODO
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Function {
    String value();
}
