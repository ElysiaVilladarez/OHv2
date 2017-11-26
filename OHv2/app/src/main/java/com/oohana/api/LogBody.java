package com.oohana.api;

import android.util.Log;

import com.oohana.helpers.Constants;

import java.util.Date;

/**
 * Created by SaperiumDev on 11/26/2017.
 */

public class LogBody {
    String deviceID;
    String geof_id;
    String status;
    String timestamp;

    public LogBody(String geof_id, String status, Date timestamp) {
        String p1 = Constants.getMACAddress("wlan0");
        String p2 = Constants.getMACAddress("eth0");
        if(p1.equals("02:00:00:00:00:00") || p1.isEmpty()) deviceID = p2;
        else deviceID = p1;

        Log.d(Constants.LOG_TAG_HOME, "MAC Address: "+ deviceID);

        this.geof_id = geof_id;
        this.status = status;
        this.timestamp = Constants.SYNC_SERVER_FORMAT.format(timestamp);
    }
}
