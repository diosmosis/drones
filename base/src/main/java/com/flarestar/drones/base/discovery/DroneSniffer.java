package com.flarestar.drones.base.discovery;

import android.app.Activity;
import com.flarestar.drones.base.BaseScreen;
import com.flarestar.drones.base.DroneCollection;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * TODO
 */
public class DroneSniffer {

    public DroneCollection findDroneCollectionFor(BaseScreen screen, String activityComponentClassName) {
        DroneCollection collection;
        try {
            Class<?> componentClass = Class.forName(activityComponentClassName);
            Method buildMethod = componentClass.getMethod("build", Activity.class);

            if (!Modifier.isStatic(buildMethod.getModifiers())) {
                throw new RuntimeException("Invalid ActivityComponent.build() method: build method must be static.");
            }

            collection = (DroneCollection) buildMethod.invoke(null, screen);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }

        if (collection == null) {
            throw new RuntimeException("Invalid ActivityComponent.build() method: returns null instead of valid DroneCollection.");
        }

        return collection;
    }

    public DroneCollection findDroneCollectionFor(BaseScreen screen) {
        return findDroneCollectionFor(screen, screen.getClass().getName() + "ActivityComponent");
    }
}
