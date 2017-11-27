package com.oohana.home;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.oohana.R;
import com.oohana.helpers.Constants;
import com.oohana.helpers.DialogConfirmCreator;
import com.oohana.home.fragments.HomeFragment;

import java.util.Calendar;

public class HomeActivity extends AppCompatActivity implements HomeContract.HomeViewToPresenter {
    private static HomeContract.HomePresenterToView homePresenter;
    private static TextView latText, lngText, lastUpdated, geofenceNumText, logCount, geofencesCount;
    private Dialog alertDialogs;
    private TextView alertDialogMes;
    private Button alertOkButton;
    private LocationUpdateUIReceiver receiver;
    private static SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        homePresenter = new HomePresenter(this);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        Fragment homeFragment = HomeFragment.newInstance();
        fragmentTransaction.add(R.id.fragment_container, homeFragment);
        fragmentTransaction.commit();

        prefs = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE);

        alertDialogs = new Dialog(this);
        alertDialogs.requestWindowFeature(Window.FEATURE_NO_TITLE);
        alertDialogs.setContentView(R.layout.dialog_general_template);
        alertDialogs.setCancelable(false);
        alertDialogs.setCanceledOnTouchOutside(false);
        alertDialogs.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialogMes = (TextView)alertDialogs.findViewById(R.id.message);
        alertOkButton = (Button)alertDialogs.findViewById(R.id.ok_button);

        receiver = new LocationUpdateUIReceiver();
        homePresenter.registerReceiverToBroadcast(receiver);


    }
    @Override
    public void onStart(){
        super.onStart();

        //Home Fragment
        latText = (TextView)findViewById(R.id.locationLat);
        lngText = (TextView)findViewById(R.id.locationLng);
        lastUpdated = (TextView)findViewById(R.id.updatedTime);
        geofenceNumText = (TextView)findViewById(R.id.geofencesActive);
        logCount = (TextView)findViewById(R.id.log_count);
        geofencesCount = (TextView)findViewById(R.id.geo_count);

        Intent intent = getIntent();
        if(intent != null) {
            if (Constants.ACTION_STOP_SERVICE.equals(intent.getAction())) {
                final DialogConfirmCreator dialog = new DialogConfirmCreator(HomeActivity.this);
                dialog.setAlertDialogMes(getString(R.string.confirm_stop_logs_mes));
                dialog.setAlertOkButtonClick(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d(Constants.LOG_TAG_HOME, "Stopping logs . . . ");
                        homePresenter.stopService();
                        homePresenter.unregisterReceiver(receiver);
                        homePresenter.stopAllJobs();
                        homePresenter.stopLocationUpdates();
                        homePresenter.removeAllGeofences();
                        //exit the whole app
                        finishAffinity();
                    }
                });
                dialog.setAlertCancelButtonClick(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismissDialog();
                    }
                });
                try {
                    dialog.showDialog();
                } catch (Exception e){
                    dialog.dismissDialog();
                }
            } else{
                Log.d(Constants.LOG_TAG_HOME, "Setting up . . . ");
                homePresenter.setUpResourcesAndServices(this.getApplicationContext());
            }
        } else{
            Log.d(Constants.LOG_TAG_HOME, "Setting up . . . ");
            homePresenter.setUpResourcesAndServices(this.getApplicationContext());
        }
    }

    @Override
    public Activity getActivity() {
        return this;
    }

    //Set texts
    @Override
    public void setLatLng(String lat, String lng) {
        latText.setText(lat);
        lngText.setText(lng);
        lastUpdated.setText(Constants.LAST_UPDATED_FORMAT.format(Calendar.getInstance().getTime()));
    }

    public static void setLatLngStatic(String lat, String lng) {
        latText.setText(lat);
        lngText.setText(lng);
        lastUpdated.setText(Constants.LAST_UPDATED_FORMAT.format(Calendar.getInstance().getTime()));
    }

    public static void setGeofencesCountStatic(String geofCount) {
        geofencesCount.setText(geofCount);
    }

    @Override
    public void setGeofencesCount(String geofCount){
        geofencesCount.setText(geofCount);
    }

    public static void setLogCountStatic(String logsCount) {
        logCount.setText(logsCount);
    }
    @Override
    public void setLogCount(String logsCount) {
        logCount.setText(logsCount);
    }

    @Override
    public void setGeofencesActive(String geofencesActive) {
        geofenceNumText.setText(geofencesActive);
    }

    //Button clicks
    public void syncLogs(View v){
        homePresenter.syncLogsToServerAsync(true);
    }

    public void fetchGeo(View v){
        homePresenter.fetchGeofencesFromServer(true);
    }
    public void viewLogs(View v){

    }

    public void viewGeo(View v){

    }

    @Override
    public void askToTurnOnLocation(String message) {
        Log.d(Constants.LOG_TAG_HOME, "Ask to turn on location");
        if(alertDialogMes != null && alertDialogs !=null && alertOkButton != null) {
            alertDialogMes.setText(message);
            alertOkButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    alertDialogs.dismiss();
                    homePresenter.turnOnLocation();
                }
            });
            alertDialogs.show();
        }
    }

    @Override
    public void askToConnectToInternet(String message) {
        Log.d(Constants.LOG_TAG_HOME, "Ask to connect to the internet.");
        if(alertDialogMes != null && alertDialogs !=null && alertOkButton != null) {
            alertDialogMes.setText(message);
            alertOkButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    alertDialogs.dismiss();
                    startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                }
            });
            alertDialogs.show();
        }

    }

    @Override
    public void permissionDeniedDialog(String mes) {
        Log.d(Constants.LOG_TAG_HOME, "Location Permission Denied");
        if (alertDialogMes != null && alertDialogs != null && alertOkButton != null) {
            alertDialogMes.setText(mes);
            alertOkButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    alertDialogs.dismiss();
                    finish();
                }
            });

            alertDialogs.show();
        }

    }

    @Override
    public void askForLocationPermission(String[] permissions, int reqId) {
        Log.d(Constants.LOG_TAG_HOME, "Ask for Location Permission");
        ActivityCompat.requestPermissions(this, permissions, reqId);
    }

    @Override
    public void setToHighAccuracy(String message) {
        Log.d(Constants.LOG_TAG_HOME, "Switch location to high accuracy");
        if(alertDialogMes != null && alertDialogs !=null && alertOkButton != null) {
            alertDialogMes.setText(message);
            alertOkButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    alertDialogs.dismiss();
                    homePresenter.launchLocationSettings();
                }
            });
            alertDialogs.show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case Constants.LOC_REQ_PERMISSIONS_ID:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    homePresenter.checkLocationProvider();
                } else {
                    homePresenter.permissionDenied();
                }
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        Intent intent = getIntent();
        if(intent != null) {
            if (Constants.ACTION_STOP_SERVICE.equals(intent.getAction())) {
                final DialogConfirmCreator dialog = new DialogConfirmCreator(HomeActivity.this);
                dialog.setAlertDialogMes(getString(R.string.confirm_stop_logs_mes));
                dialog.setAlertOkButtonClick(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        homePresenter.stopService();
                        homePresenter.unregisterReceiver(receiver);
                        homePresenter.stopAllJobs();
                        homePresenter.stopLocationUpdates();
                        homePresenter.removeAllGeofences();
                        //exit the whole app
                        dialog.dismissDialog();
                        finishAffinity();
                    }
                });
                dialog.setAlertCancelButtonClick(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismissDialog();
                    }
                });
                dialog.showDialog();
            } else {
                homePresenter.checkLocationProvider();
                homePresenter.checkGeofencesContent();
            }
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        //homePresenter.stopJob(Constants.SYNC_LOGS_TAG);
        //homePresenter.stopLocationUpdates();
        homePresenter.unregisterReceiver(receiver);
        homePresenter.removeAllGeofences();
        //homePresenter.stopService();
    }

    public static class LocationUpdateUIReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if(Constants.ACTION_UPDATE_LOC_UI.equals(intent.getAction())) {
                Log.d(Constants.LOG_TAG_RECEIVER, "Location has been updated! Updating UI . . .");

                String curLoc = prefs.getString(Constants.LOC_CURR_LATLNG_KEY, "");
                if (!curLoc.isEmpty()) {
                    String[] latlng = curLoc.split(",");
                    setLatLngStatic(latlng[0], latlng[1]);
                }
                if(intent.getBooleanExtra(Constants.HAS_SIG_LOC_CHANGE_KEY, false)){
                    if(homePresenter !=null) {
                        homePresenter.removeAllGeofences();
                        homePresenter.connectToGoogleApi();
                    }
                }
            }else if(Constants.ACTION_GPS_ON.equals(intent.getAction())){
                if(homePresenter != null) {
                    Log.d(Constants.LOG_TAG_RECEIVER, "Restarting geofences . . .");
                    homePresenter.checkGeofencesContent();
                }
            } else if(Constants.ACTION_GPS_OFF.equals(intent.getAction())){
                if(homePresenter != null) {
                    Log.d(Constants.LOG_TAG_RECEIVER, "Removing geofences . . .");
                    homePresenter.gpsOffAction();
                }
            } else if(Constants.ACTION_UPDATE_LOG_COUNT.equals(intent.getAction())){
                Log.d(Constants.LOG_TAG_RECEIVER, "Updating log count . . .");
                setLogCountStatic(Integer.toString(intent.getIntExtra(Constants.LOG_COUNT_KEY, 0)));
            } else if(Constants.ACTION_UPDATE_GEO_COUNT.equals(intent.getAction())){
                Log.d(Constants.LOG_TAG_RECEIVER, "Updating geofences count . . .");
                setGeofencesCountStatic(Integer.toString(intent.getIntExtra(Constants.GEOF_COUNT_KEY, 0)));
                homePresenter.connectToGoogleApi();
            }
        }
    }
}
