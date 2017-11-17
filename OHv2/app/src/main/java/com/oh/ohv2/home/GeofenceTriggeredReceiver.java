package com.oh.ohv2.home;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;
import com.oh.ohv2.api.GeofenceList;
import com.oh.ohv2.api.RetrofitService;
import com.oh.ohv2.database.GeofenceLog;
import com.oh.ohv2.database.ServerGeofence;
import com.oh.ohv2.helpers.Alarms;
import com.oh.ohv2.helpers.Constants;
import com.oh.ohv2.splashscreen.SplashScreenPresenter;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by elysi on 10/30/2017.
 */

public class GeofenceTriggeredReceiver extends WakefulBroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        //Geofences have been triggered
        final HomeContract.HomeModelToPresenter homeModel = new HomeModel();
        homeModel.instantiateRealm(context);
        HomeContract.HomePresenterToView homePresenter = new HomePresenter(null);
        Alarms alarms = new Alarms(context);

        if(Constants.ACTION_GEOFENCE_TRIGGERED.equals(intent.getAction())) {
            GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

            if (geofencingEvent.hasError()) {
                Log.d(Constants.LOG_TAG_RECEIVER, "ERROR: " + getErrorString(geofencingEvent.getErrorCode()));
                return;
            }

            int geoFenceTransition = geofencingEvent.getGeofenceTransition();

            if (geoFenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER || geoFenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL || geoFenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
                List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();
                homeModel.addLogs(geoFenceTransition, triggeringGeofences);
            }
        }

        if(Constants.ACTION_SYNC_LOGS.equals(intent.getAction())) {
            Retrofit retrofitApi = new Retrofit.Builder()
                    .baseUrl(Constants.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            RetrofitService getService = retrofitApi.create(RetrofitService.class);
            Call<Void> postLogs;
            ArrayList<GeofenceLog> logs = homeModel.getLogs();
            for(GeofenceLog l: logs) {
                postLogs = getService.sendLog(l.getTriggeredGeofence().getGeofId(), l.getStatus(), l.getTimeStamp());
                postLogs.enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        Log.d(Constants.LOG_TAG_RECEIVER, "Posting logs successful: " + response.isSuccessful());
                        homeModel.deleteLogs();
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Log.d(Constants.LOG_TAG_HOME, "Posting logs failed: " + t.toString());

                    }
                });
            }
            alarms.setAlarms(Constants.ACTION_SYNC_LOGS, Constants.SYNC_PENDING_INTENT_ID, Constants.SYNC_LOGS_TIME);
        }

        if(Constants.ACTION_FETCH_GEOFENCES.equals(intent.getAction())){
            homePresenter.fetchGeofencesFromServer();
            homePresenter.connectToGoogleApi();
            alarms.setAlarms(Constants.ACTION_FETCH_GEOFENCES, Constants.FETCH_PENDING_INTENT_ID, Constants.FETCH_LOGS_TIME);
        }

    }
    private String getErrorString(int errorCode) {
        switch (errorCode) {
            case GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE:
                return "GeoFence not available";
            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES:
                return "Too many GeoFences";
            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS:
                return "Too many pending intents";
            default:
                return "Unknown error.";
        }
    }

}
