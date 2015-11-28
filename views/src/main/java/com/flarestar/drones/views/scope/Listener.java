package com.flarestar.drones.views.scope;

public interface Listener<E extends Event> {

    boolean invoke(E event);
}
