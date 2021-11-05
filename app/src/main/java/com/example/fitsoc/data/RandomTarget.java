package com.example.fitsoc.data;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

import android.location.Location;
import android.util.Log;

import java.util.Random;

public class RandomTarget {
    String taskName;
    boolean completedStatus;
    Location targetLocation;

    public RandomTarget() {
        taskName = "go to the target!";
        completedStatus = false;
        targetLocation = null;
    }

    // generate a near marker
    public RandomTarget(Location currentLocation, double radius) {
        this();

        double x0 = currentLocation.getLongitude();
        double y0 = currentLocation.getLatitude();
        String message2 = "Longitude: " + x0 + "  Latitude: " + y0;
        Log.d("Current Location ----> ", message2);
        Random random = new Random();
        double radiusInDegrees = radius / 111300f;
        double u = random.nextDouble();
        double v = random.nextDouble();
        double w = radiusInDegrees * Math.sqrt(u);
        double t = 2 * Math.PI * v;
        double x = w * cos(t);
        double y = w * sin(t);
        x = x / Math.cos(Math.cos(y0));

        double targetLongitude = x + x0;
        double targetLatitude = y + y0;
        targetLocation = new Location("Target Location");
        targetLocation.setLongitude(targetLongitude);
        targetLocation.setLatitude(targetLatitude);
        String message = "Longitude: " + targetLongitude + "  Latitude: " + targetLatitude;
        Log.d("Target Location ----> ", message);
    }


    public Location getTargetLocation() {
        return targetLocation;
    }

    public void taskAccomplished() {
        completedStatus = true;
    }

    public float calculateDistance(Location location) {
        Log.d("Distance to Target: -----> ", String.valueOf(targetLocation.distanceTo(location)));
        return targetLocation.distanceTo(location);
    }

    public boolean isAtTargetLocation(Location location) {
        if (calculateDistance(location) < 15) {
            taskAccomplished();
            return true;
        } else return false;
    }
}
