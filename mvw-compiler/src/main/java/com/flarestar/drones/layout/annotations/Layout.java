package com.flarestar.drones.layout.annotations;

import com.flarestar.drones.base.annotations.DroneMarker;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * TODO
 */
@Retention(RetentionPolicy.SOURCE)
@DroneMarker(generatedClass = "{fullName}LayoutBuilderDrone")
public @interface Layout {
    String value() default "";
    String stylesheet() default "";
}
