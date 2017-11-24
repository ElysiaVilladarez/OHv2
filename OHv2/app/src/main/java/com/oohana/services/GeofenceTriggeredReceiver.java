package com.oohana.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;
import com.oohana.R;
import com.oohana.helpers.Constants;
import com.oohana.helpers.DialogConfirmCreator;
import com.oohana.helpers.PermissionIntent;
import com.oohana.home.HomeContract;
import com.oohana.home.HomeModel;

import java.util.List;

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
            }
        } else if(Constants.ACTION_PROVIDERS_CHANGED.equals(intent.getAction())){
            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                // show dialog asking to turn on gps for oohana
                final DialogConfirmCreator dialog = new DialogConfirmCreator(context);
                dialog.setAlertDialogMes(context.getString(R.string.turn_on_loc_mes));
                dialog.setAlertOkButtonClick(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        PermissionIntent pi = new PermissionIntent(context);
                        dialog.dismissDialog();
                        pi.changeLocationSettings();
                    }
                });
                dialog.setAlertCancelButtonClick(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismissDialog();
                    }
                });
            }else{
                //set up geofences again
                Intent geoIntent = new Intent();
                LocalBroadcastManager.getInstance(context).sendBroadcast(geoIntent.setAction(Constants.ACTION_PROVIDERS_CHANGED));
                Log.d(Constants.LOG_TAG_HOME, "GPS turned on.");
            }
        } else if(Constants.ACTION_FETCH_GEOFENCES.equals(intent.getAction())){
            Log.d(Constants.LOG_TAG_RECEIVER, "Fetching newly added geofences from server . . .");
//            homePresenter.fetchGeofencesFromServer();
//            homePresenter.connectToGoogleApi();
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
