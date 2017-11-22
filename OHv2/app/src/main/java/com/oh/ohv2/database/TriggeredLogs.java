package com.oh.ohv2.database;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Date;

/**
 * Created by SaperiumDev on 11/19/2017.
 */

public class TriggeredLogs {
    @SerializedName("geof_id")
    @Expose
    private int triggeredGeofenceId;
    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("timestamp")
    @Expose
    private Date timeStamp;

    public TriggeredLogs(int triggeredGeofenceId, String status, Date timeStamp) {
        this.triggeredGeofenceId = triggeredGeofenceId;
        this.status = status;
        this.timeStamp = timeStamp;
    }

    public int getTriggeredGeofenceId() {
        return triggeredGeofenceId;
    }

    public void setTriggeredGeofenceId(int triggeredGeofenceId) {
        this.triggeredGeofenceId = triggeredGeofenceId;
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
