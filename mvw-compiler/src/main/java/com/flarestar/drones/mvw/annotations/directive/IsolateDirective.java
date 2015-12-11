package com.flarestar.drones.mvw.annotations.directive;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface IsolateDirective {
    String template() default "";
    String less() default "";
}