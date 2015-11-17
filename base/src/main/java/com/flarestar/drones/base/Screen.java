package com.flarestar.drones.base;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import com.flarestar.drones.base.DroneInterface;

/**
 * TODO
 */
public abstract class Screen extends Activity
{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Drone drone = new Drone();
        drone.setUpDrone(this);
    }
}
