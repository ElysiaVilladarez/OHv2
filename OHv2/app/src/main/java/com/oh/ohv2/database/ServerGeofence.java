package com.oh.ohv2.database;

import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by elysi on 10/29/2017.
 */

public class ServerGeofence extends RealmObject {

    @SerializedName("geof_id")
    @PrimaryKey
    private int geofId;

    @SerializedName("geof_name")
    private String geofName;

    @SerializedName("geof_lat")
    private double geofLat;

    @SerializedName("geof_long")
    private double geofLng;

    @SerializedName("geof_rad")
    private float geofRad; //must be in meters

    private double nearness;

    public int getGeofId() {
        return geofId;
    }

    public void setGeofId(int geofId) {
        this.geofId = geofId;
    }

    public String getGeofName() {
        return geofName;
    }

    public void setGeofName(String geofName) {
        this.geofName = geofName;
    }

    public double getGeofLat() {
        return geofLat;
    }

    public void setGeofLat(double geofLat) {
        this.geofLat = geofLat;
    }

    public double getGeofLng() {
        return geofLng;
    }

    public void setGeofLnng(double geofLong) {
        this.geofLng = geofLong;
    }

    public float getGeofRad() {
        return geofRad;
    }

    public void setGeofRad(float geofRad) {
        this.geofRad = geofRad;
    }

    public double getNearness() {
        return nearness;
    }

    public void setNearness(double nearness) {
        this.nearness = nearness;
    }
}
