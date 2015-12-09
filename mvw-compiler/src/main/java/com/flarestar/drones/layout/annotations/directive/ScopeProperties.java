package com.flarestar.drones.layout.annotations.directive;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by runic on 11/4/15.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface ScopeProperties {
    String[] value();
}
