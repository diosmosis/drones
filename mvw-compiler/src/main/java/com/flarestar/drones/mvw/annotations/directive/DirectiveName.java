package com.flarestar.drones.mvw.annotations.directive;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface DirectiveName {
    String value();
}
