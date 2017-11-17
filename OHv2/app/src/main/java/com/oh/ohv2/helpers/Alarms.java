package com.oh.ohv2.helpers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.oh.ohv2.home.GeofenceTriggeredReceiver;

/**
 * Created by elysi on 10/30/2017.
 */

public class Alarms {
    private AlarmManager alarmManager;
    private Context c;
    public Alarms(Context c){
        this.c = c;

    }
    public void setAlarms(String action, int pendingId, long alarmTime){
        AlarmManager alarmManager = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        Intent myIntent = new Intent(c, GeofenceTriggeredReceiver.class);
        myIntent.setAction(action);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(c, pendingId, myIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        Log.d(Constants.LOG_TAG_ALARMS, "Setting Alarm: " + action);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + alarmTime, pendingIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + alarmTime, pendingIntent);
        }
    }
}
