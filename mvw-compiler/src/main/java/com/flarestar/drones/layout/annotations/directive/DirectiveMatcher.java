package com.flarestar.drones.layout.annotations.directive;

import com.flarestar.drones.layout.view.directive.matchers.TagMatcher;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * TODO
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface DirectiveMatcher {

    Class<? extends com.flarestar.drones.layout.view.directive.DirectiveMatcher> value();
}
