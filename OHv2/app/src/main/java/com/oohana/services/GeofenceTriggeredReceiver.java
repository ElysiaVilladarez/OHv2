package com.oohana.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;
import com.oohana.R;
import com.oohana.api.RetrofitService;
import com.oohana.helpers.Constants;
import com.oohana.helpers.DialogConfirmCreator;
import com.oohana.helpers.PermissionIntent;
import com.oohana.helpers.SyncLogsToServer;
import com.oohana.home.HomeContract;
import com.oohana.home.HomeModel;
import com.oohana.home.TransparentActDialog;

import java.util.List;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by elysi on 10/30/2017.
 */

public class GeofenceTriggeredReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(final Context context, Intent intent) {

        final HomeContract.HomeModelToPresenter homeModel = new HomeModel();
        homeModel.instantiateRealm(context);



        //Geofences have been triggered
        if(Constants.ACTION_GEOFENCE_TRIGGERED.equals(intent.getAction())) {
            Log.d(Constants.LOG_TAG_RECEIVER, "Geofence triggered!");
            GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

            if (geofencingEvent.hasError()) {
                Log.d(Constants.LOG_TAG_RECEIVER, "ERROR: " + getErrorString(geofencingEvent.getErrorCode()));
                return;
            }

            int geoFenceTransition = geofencingEvent.getGeofenceTransition();

            if (geoFenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER || geoFenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL
                    || geoFenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
                List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();
                homeModel.addLogs(geoFenceTransition, triggeringGeofences);
                Log.d(Constants.LOG_TAG_RECEIVER, "Status: " + geoFenceTransition + " ~ " + TextUtils.join(", ", triggeringGeofences));
                Intent trigIntent = new Intent();
                trigIntent.setAction(Constants.ACTION_UPDATE_LOG_COUNT);
                trigIntent.putExtra(Constants.LOG_COUNT_KEY, homeModel.getLogCount());
                context.sendBroadcast(trigIntent);
            }
        }
        if(Constants.ACTION_PROVIDERS_CHANGED.equals(intent.getAction())){
            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            Intent geoIntent = new Intent();
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                // show dialog asking to turn on gps for oohana
                Log.d(Constants.LOG_TAG_RECEIVER, "GPS is off");
                geoIntent.setAction(Constants.ACTION_GPS_OFF);
                context.sendBroadcast(geoIntent);
            }else{
                //set up geofences again
                geoIntent.setAction(Constants.ACTION_GPS_ON);
                context.sendBroadcast(geoIntent);
                Log.d(Constants.LOG_TAG_HOME, "GPS turned on.");
            }
        }
        if(Constants.ACTION_STATE_CHANGED.equals(intent.getAction()) || Constants.ACTION_WIFI_STATE_CHANGED.equals(intent.getAction())
                || ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())){
            NetworkInfo ni = (NetworkInfo) intent.getExtras().get(
                    ConnectivityManager.EXTRA_NETWORK_INFO);
            if (ni != null && ni.getState() == NetworkInfo.State.CONNECTED && homeModel.getLogCount() > 0) {
                Log.d(Constants.LOG_TAG_RECEIVER, "There is internet connection!");
                SharedPreferences pref = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
                boolean hasSyncedBefore = pref.getBoolean(Constants.SHARED_PREF_HAS_SYNCED_BEFORE_KEY, false);
                if(hasSyncedBefore) {
                    Log.d(Constants.LOG_TAG_RECEIVER, "Syncing has been attempted before! Sync now");
                    Retrofit retrofitApi = new Retrofit.Builder()
                            .baseUrl(Constants.BASE_URL)
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();

                    RetrofitService getService = retrofitApi.create(RetrofitService.class);
                    SyncLogsToServer.syncLogs(context, getService, homeModel, false);
                }

                boolean hasFetchedBefore = pref.getBoolean(Constants.SHARED_PREF_HAS_FETCHED_BEFORE_KEY, false);
                if(hasFetchedBefore) {
                    Log.d(Constants.LOG_TAG_RECEIVER, "Fetch has been attempted before! Fetch now");
                    Retrofit retrofitApi = new Retrofit.Builder()
                            .baseUrl(Constants.BASE_URL)
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();

                    RetrofitService getService = retrofitApi.create(RetrofitService.class);
                    SyncLogsToServer.fetchGeofencesFromServer(context, getService, homeModel, false);
                }
            } else{
                Log.d(Constants.LOG_TAG_RECEIVER, "There is no internet connection!");
            }
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
