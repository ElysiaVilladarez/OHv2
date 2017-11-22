package com.oh.ohv2.home;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.oh.ohv2.R;
import com.oh.ohv2.helpers.Constants;

import java.util.Calendar;
import java.util.Date;

public class HomeActivity extends AppCompatActivity implements HomeContract.HomeViewToPresenter {
    private HomeContract.HomePresenterToView homePresenter;
    private TextView latText, lngText, lastUpdated, geofenceNumText;
    private Dialog alertDialogs;
    private TextView alertDialogMes;
    private Button alertOkButton;
    private LocationUpdateUIReceiver receiver;

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
    public void onResume(){
        super.onResume();
        homePresenter.checkLocationProvider();
        //TO DO also check permissions
       // homePresenter.fetchGeofencesFromServer();
        homePresenter.connectToGoogleApi();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        homePresenter.stopJob(Constants.SYNC_LOGS_TAG);
    }

    public class LocationUpdateUIReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if(Constants.ACTION_UPDATE_LOC_UI.matches(intent.getAction())) {
                Log.d(Constants.LOG_TAG_RECEIVER, "Location has been updated! Updating UI . . .");
                String curLoc = intent.getStringExtra(Constants.LOC_CURR_LATLNG_KEY);
                if(!curLoc.isEmpty()) {
                    String[] latlng = curLoc.split(",");
                    setLatLng(latlng[0], latlng[1]);
                }
            }
        }
    }
}
