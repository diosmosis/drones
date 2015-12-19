package com.flarestar.drones.base.inject;

/**
 * TODO
 *
 * TODO: at the moment, can't use variadic args since guice doesn't create factories that way.
 */
public interface GenericInstanceFactory<T, A> {
    T make(A arg);
}
