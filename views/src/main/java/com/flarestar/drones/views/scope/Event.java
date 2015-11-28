package com.flarestar.drones.views.scope;

public abstract class Event {

    private boolean isPropagationEnded = false;

    public void stopPropagation() {
        isPropagationEnded = true;
    }

    public boolean isPropagationEnded() {
        return isPropagationEnded;
    }
}
