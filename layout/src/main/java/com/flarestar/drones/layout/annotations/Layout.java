package com.flarestar.drones.layout.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * TODO
 */
@Retention(RetentionPolicy.SOURCE)
public @interface Layout {
    String value() default "";
    String stylesheet() default "";
}
