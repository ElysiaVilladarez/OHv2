package com.oh.ohv2.splashscreen;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.oh.ohv2.R;
import com.oh.ohv2.helpers.Constants;
import com.oh.ohv2.home.HomeActivity;

public class SplashScreenActivity extends AppCompatActivity implements SplashScreenContract.SplashScreenViewToPresenter {

    private SplashScreenContract.SplashScreenPresenterToView splashScreenPresenter;
    private Dialog alertDialogs;
    private TextView alertDialogMes;
    private Button alertOkButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        splashScreenPresenter = new SplashScreenPresenter(this);

        alertDialogs = new Dialog(this);
        alertDialogs.requestWindowFeature(Window.FEATURE_NO_TITLE);
        alertDialogs.setContentView(R.layout.dialog_general_template);
        alertDialogs.setCancelable(false);
        alertDialogs.setCanceledOnTouchOutside(false);
        alertDialogs.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialogMes = (TextView)alertDialogs.findViewById(R.id.message);
        alertOkButton = (Button)alertDialogs.findViewById(R.id.ok_button);

        splashScreenPresenter.checkLocationPermission();
    }

    @Override
    public Activity getActivity() {
        return this;
    }

    @Override
    public void askForLocationPermission(String[] permissionsNeeded, int requestId) {
        Log.d(Constants.LOG_TAG_SPLASHSCREEN, "Ask for Location Permission");
         ActivityCompat.requestPermissions(this, permissionsNeeded, requestId);
    }

    @Override
    public void askToTurnOnLocation(String message) {
        Log.d(Constants.LOG_TAG_SPLASHSCREEN, "Ask to turn on location");
        if(alertDialogMes != null && alertDialogs !=null && alertOkButton != null) {
        alertDialogMes.setText(message);
            alertOkButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    alertDialogs.dismiss();
                    splashScreenPresenter.turnOnLocation();
                }
            });
            alertDialogs.show();
        }

    }

    @Override
    public void permissionDeniedDialog(String message) {
        Log.d(Constants.LOG_TAG_SPLASHSCREEN, "Location Permission Denied");
        if(alertDialogMes != null && alertDialogs !=null&& alertOkButton != null) {
            alertDialogMes.setText(message);
            alertOkButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    alertDialogs.dismiss();
                    splashScreenPresenter.changePermissions();
                }
            });

            alertDialogs.show();
        }
    }

    @Override
    public void goToHomeActivity(Activity act) {
        Log.d(Constants.LOG_TAG_SPLASHSCREEN, "Navigate to Home Activity");
        Intent homeIntent = new Intent(act, HomeActivity.class);
        act.startActivity(homeIntent);
        act.finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case Constants.LOC_REQ_PERMISSIONS_ID:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    splashScreenPresenter.checkLocationProvider();
                } else {
                    splashScreenPresenter.permissionDenied();
                }
        }
    }

    //Override life cycles
    @Override
    public void onDestroy() {
        super.onDestroy();
        splashScreenPresenter.destroyWeakActivity();

    }
    @Override
    protected void onStop() {
        super.onStop();
        if(alertDialogs != null)
            alertDialogs.dismiss();
    }

}
