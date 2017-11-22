package com.oh.ohv2.services;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.LocationManager;
import android.os.Build;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;
import com.oh.ohv2.R;
import com.oh.ohv2.api.GeofenceList;
import com.oh.ohv2.api.RetrofitService;
import com.oh.ohv2.database.GeofenceLog;
import com.oh.ohv2.database.ServerGeofence;
import com.oh.ohv2.database.TriggeredLogs;
import com.oh.ohv2.helpers.Alarms;
import com.oh.ohv2.helpers.Constants;
import com.oh.ohv2.helpers.DialogConfirmCreator;
import com.oh.ohv2.helpers.PermissionIntent;
import com.oh.ohv2.home.HomeContract;
import com.oh.ohv2.home.HomeModel;
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

public class GeofenceTriggeredReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(final Context context, Intent intent) {

        final HomeContract.HomeModelToPresenter homeModel = new HomeModel();
        homeModel.instantiateRealm(context);

        //Geofences have been triggered
        if(Constants.ACTION_GEOFENCE_TRIGGERED.matches(intent.getAction())) {
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
        } else if(Constants.ACTION_PROVIDERS_CHANGED.matches(intent.getAction())){
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
            }
        } else if(Constants.ACTION_FETCH_GEOFENCES.matches(intent.getAction())){
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
