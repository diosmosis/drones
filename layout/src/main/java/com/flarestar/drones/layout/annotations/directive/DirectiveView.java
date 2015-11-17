package com.flarestar.drones.layout.annotations.directive;

import android.view.View;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface DirectiveView {
    Class<? extends View> view();
}
