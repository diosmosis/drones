package com.flarestar.drones.base;

import android.os.Bundle;
import android.app.Activity;

import java.lang.reflect.Method;

/**
 * TODO
 */
public abstract class BaseScreen extends Activity {
    private Drone[] drones;

    public Drone[] drones() {
        return drones;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String activityComponentClassName = getClass().getName() + "ActivityComponent";

        DroneCollection collection;
        try {
            Class<?> componentClass = Class.forName(activityComponentClassName);
            Method buildMethod = componentClass.getMethod("build", Activity.class);
            collection = (DroneCollection)buildMethod.invoke(null, this);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }

        drones = collection.getDrones();
        for (Drone drone : drones) {
            drone.setUpDrone(this);
        }
    }
}
