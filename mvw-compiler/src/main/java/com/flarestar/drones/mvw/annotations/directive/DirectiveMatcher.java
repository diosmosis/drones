package com.flarestar.drones.mvw.annotations.directive;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * TODO
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface DirectiveMatcher {

    Class<? extends com.flarestar.drones.mvw.processing.parser.directive.DirectiveMatcher> value();
}
