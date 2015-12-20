package com.flarestar.drones.mvw.annotations;

import com.flarestar.drones.base.annotations.DroneMarker;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * TODO
 */
@Retention(RetentionPolicy.SOURCE)
@DroneMarker(
    generatedClass = "{fullName}LayoutBuilderDrone",
    extraComponentMethods = {"{fullName}LayoutBuilderDrone.ScopesComponent makeScopesComponent()"}
)
public @interface Layout {
    String value() default "";
    String stylesheet() default "";
}
