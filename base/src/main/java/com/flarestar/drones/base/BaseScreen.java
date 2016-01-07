package com.flarestar.drones.base;

import android.os.Bundle;
import android.app.Activity;
import com.flarestar.drones.base.discovery.DroneSniffer;

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

        DroneSniffer droneSniffer = new DroneSniffer();
        DroneCollection collection = droneSniffer.findDroneCollectionFor(this);

        drones = collection.getDrones();
        for (Drone drone : drones) {
            drone.setUpDrone(this);
        }
    }
}
