package com.flarestar.drones.base.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface DroneMarker {
    String generatedClass();
}
