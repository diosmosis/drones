package com.flarestar.drones.mvw.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * TODO
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Function {
    String value();
}
/*
@Function("uppercase")
class Uppercase {
    @Inject
    public Uppercase() {
        // empty
    }

    public String invoke(String value) {
        return value.toUpperCase();
    }
}

1. during compiling, we need to find all type mirrors w/ Function annotation.
2. make sure each Function is default constructible or has @Inject constructor & each Function has invoke method.
3. add as injected properties
4. in LayoutBuilder.twig, generate private methods w/ name, eg, String uppercase(...) which forwards arguments to .invoke.
 */