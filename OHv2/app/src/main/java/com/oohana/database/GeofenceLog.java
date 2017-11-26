package com.oohana.database;

import java.util.Date;

import io.realm.RealmObject;

/**
 * Created by elysi on 10/29/2017.
 */

public class GeofenceLog extends RealmObject {
    private int id;
    private String status;
    private Date timeStamp;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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
