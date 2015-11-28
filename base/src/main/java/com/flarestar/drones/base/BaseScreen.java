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

        // TODO: lots of reflection here. if dagger DI's generated classes implemented some interfaces,
        //       I think I could replace most of it. could try and add this upstream.
        String activityComponentClassName = getClass().getName() + "ActivityComponent";
        Class<?> componentClass = null;
        try {
            componentClass = Class.forName(activityComponentClassName);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        Method buildMethod = null;
        try {
            buildMethod = componentClass.getMethod("build", Activity.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        DroneCollection collection = null;
        try {
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
