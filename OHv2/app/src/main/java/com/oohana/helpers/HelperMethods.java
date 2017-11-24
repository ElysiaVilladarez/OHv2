package com.oohana.helpers;

import com.google.android.gms.location.Geofence;

/**
 * Created by SaperiumDev on 11/21/2017.
 */

public class HelperMethods {

    public double haversine(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.sin(dLon / 2) * Math.sin(dLon / 2) * Math.cos(lat1) * Math.cos(lat2);
        double c = 2 * Math.asin(Math.sqrt(a));
        return Math.abs(Constants.HARVERSIN_CONVERSION * c * 1000);
    }

    public String getStatus(int geofenceTransition) {
        String status;
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            status = "Entering ";
        } else if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL) {
            status = "Dwelling ";
        } else if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            status = "Exiting ";
        }else{
            status = "Normal";
        }
        return status;
    }

    public Geofence createGeofence(String name, double lat, double lng, float radius) {
        if (radius <= 0) radius = Constants.GEOFENCE_RADIUS_DEFAULT_VALUE;
        return new Geofence.Builder()
                .setRequestId(name)
                .setCircularRegion(lat, lng, (radius * 1000))
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Constants.GEOF_TRANSITION_TYPES)
                .setLoiteringDelay(Constants.GEOFENCE_LOITERING_DELAY)
                .build();
    }
}
