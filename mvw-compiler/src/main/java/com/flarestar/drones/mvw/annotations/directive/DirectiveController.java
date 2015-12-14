package com.flarestar.drones.mvw.annotations.directive;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * TODO
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface DirectiveController {
    String value();
}
