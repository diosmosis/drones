package com.flarestar.drones.base.inject;

/**
 * TODO
 */
public interface GenericInstanceFactory<T> {
    T make(Object[] args);
}
