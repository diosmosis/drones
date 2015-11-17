package com.flarestar.drones.base;

import android.content.Context;
import android.util.Log;
import dalvik.system.DexFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * TODO
 */
public class Drone {
    public final static String LOG_TAG = "com.flarestar";

    public void setUpDrone(Screen screen) {
        Log.d(LOG_TAG, "Drone: Setting up all drones...");

        for (DroneInterface drone : getAllDrones(screen)) {
            Log.d(LOG_TAG, "Drone: Setting up drone '" + drone.getClass().toString() + "'.");

            drone.setUpDrone(screen);
        }
    }

    private Iterable<? extends DroneInterface> getAllDrones(Context context) {
        DexFile df = null;
        try {
            df = new DexFile(context.getPackageCodePath());
        } catch (IOException e) {
            throw new RuntimeException("Could not get DexFile for '" + context.getPackageCodePath() + "'.", e);
        }

        ArrayList<DroneInterface> result = new ArrayList<>();
        for (Enumeration<String> iter = df.entries(); iter.hasMoreElements(); ) {
            String className = iter.nextElement();
            if (className.endsWith("Drone")) {
                Class<?> klass = null;
                try {
                    klass = Class.forName(className);
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException("Could not load class '" + className + "' found in DexFile.", e);
                }

                if (!DroneInterface.class.isAssignableFrom(klass)) {
                    continue;
                }

                DroneInterface instance = null;
                try {
                    instance = (DroneInterface)klass.newInstance();
                } catch (InstantiationException e) {
                    throw new RuntimeException("Unexpected error: cannot create drone " + className
                            + " by calling public default constructor.", e);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Unexpected error: cannot create drone " + className
                            + ", there is no public default constructor.", e);
                }

                result.add(instance);
            }
        }
        return result;
    }
}
