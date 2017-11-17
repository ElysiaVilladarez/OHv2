package com.oh.ohv2.api;

import com.google.gson.annotations.SerializedName;
import com.oh.ohv2.database.ServerGeofence;

import java.util.List;

/**
 * Created by elysi on 10/29/2017.
 */

public class GeofenceList {
    @SerializedName("result")
    public String result;
    @SerializedName("geofence")
    public List<ServerGeofence> getGeofenceList;
}
