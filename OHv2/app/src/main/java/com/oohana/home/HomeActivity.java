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
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.oohana.R;
import com.oohana.helpers.Constants;
import com.oohana.helpers.DialogConfirmCreator;

import java.util.Calendar;

public class HomeActivity extends AppCompatActivity implements HomeContract.HomeViewToPresenter {
    private HomeContract.HomePresenterToView homePresenter;
    private TextView latText, lngText, lastUpdated, geofenceNumText;
    private Dialog alertDialogs;
    private TextView alertDialogMes;
    private Button alertOkButton;
    private LocationUpdateUIReceiver receiver;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        homePresenter = new HomePresenter(this);

        alertDialogs = new Dialog(this);
        alertDialogs.requestWindowFeature(Window.FEATURE_NO_TITLE);
        alertDialogs.setContentView(R.layout.dialog_general_template);
        alertDialogs.setCancelable(false);
        alertDialogs.setCanceledOnTouchOutside(false);
        alertDialogs.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialogMes = (TextView)alertDialogs.findViewById(R.id.message);
        alertOkButton = (Button)alertDialogs.findViewById(R.id.ok_button);

        latText = (TextView)findViewById(R.id.locationLat);
        lngText = (TextView)findViewById(R.id.locationLng);
        lastUpdated = (TextView)findViewById(R.id.updatedTime);
        geofenceNumText = (TextView)findViewById(R.id.geofencesActive);

        receiver = new LocationUpdateUIReceiver();
        homePresenter.registerReceiverToBroadcast(receiver);

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

    @Override
    public void setLatLng(String lat, String lng) {
        latText.setText(lat);
        lngText.setText(lng);
        lastUpdated.setText(Constants.LAST_UPDATED_FORMAT.format(Calendar.getInstance().getTime()));
    }

    @Override
    public void setGeofencesActive(String geofencesActive) {
        geofenceNumText.setText(geofencesActive);
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
//        homePresenter.stopJob(Constants.SYNC_LOGS_TAG);
//        homePresenter.stopLocationUpdates();
//        homePresenter.unregisterReceiver(receiver);
//        homePresenter.stopService();
    }

    public class LocationUpdateUIReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if(Constants.ACTION_UPDATE_LOC_UI.equals(intent.getAction())) {
                Log.d(Constants.LOG_TAG_RECEIVER, "Location has been updated! Updating UI . . .");

                prefs = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE);
                String curLoc = prefs.getString(Constants.LOC_CURR_LATLNG_KEY, "");
                if (!curLoc.isEmpty()) {
                    String[] latlng = curLoc.split(",");
                    setLatLng(latlng[0], latlng[1]);
                }
                if(intent.getBooleanExtra(Constants.HAS_SIG_LOC_CHANGE_KEY, false)){
                    homePresenter.removeAllGeofences();
                    homePresenter.connectToGoogleApi();
                }
            } else if(Constants.ACTION_PROVIDERS_CHANGED.equals(intent.getAction())){
                homePresenter.checkGeofencesContent();
                Log.d(Constants.LOG_TAG_RECEIVER, "Restarting geofences . . .");
            }
        }
    }
}
