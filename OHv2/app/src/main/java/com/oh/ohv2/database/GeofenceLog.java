package com.oh.ohv2.database;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by elysi on 10/29/2017.
 */

public class GeofenceLog extends RealmObject {
    private ServerGeofence triggeredGeofence;
    private String status;
    private Date timeStamp;

    public ServerGeofence getTriggeredGeofence() {
        return triggeredGeofence;
    }

    public void setTriggeredGeofence(ServerGeofence triggeredGeofence) {
        this.triggeredGeofence = triggeredGeofence;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }
}
