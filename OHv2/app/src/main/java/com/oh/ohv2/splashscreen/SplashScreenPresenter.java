package com.oh.ohv2.splashscreen;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;

import com.oh.ohv2.R;
import com.oh.ohv2.helpers.Constants;
import com.oh.ohv2.helpers.PermissionIntent;

import java.lang.ref.WeakReference;

/**
 * Created by elysi on 10/29/2017.
 */

public class SplashScreenPresenter implements SplashScreenContract.SplashScreenPresenterToView {
    private Handler logoHandler = new Handler();
    private Activity act;
    protected static SplashScreenContract.SplashScreenViewToPresenter splashScreenView;
    private PermissionIntent pi;

    private static class StartMainActivityRunnable implements Runnable {
        private WeakReference mActivity;
        private StartMainActivityRunnable(Activity activity) {
            mActivity = new WeakReference(activity);
        }

        @Override
        public void run() {
            // 3. Check that the reference is valid and execute the code
            if (mActivity.get() != null) {

                Activity activity = (Activity) mActivity.get();
                splashScreenView.goToHomeActivity(activity);
            }
        }
    }

    public SplashScreenPresenter(SplashScreenContract.SplashScreenViewToPresenter sSV) {
        splashScreenView = sSV;
        this.act = splashScreenView.getActivity();
        pi = new PermissionIntent(act);
    }

    public void createWeakActivity() {
        Log.d(Constants.LOG_TAG_SPLASHSCREEN, "Create weak activity");
        logoHandler.postDelayed(new StartMainActivityRunnable(act), Constants.LOGO_DISPLAY_LENGTH_MS);
    }

    @Override
    public void destroyWeakActivity() {
        Log.d(Constants.LOG_TAG_SPLASHSCREEN, "Destroy weak activity");
        logoHandler.removeCallbacksAndMessages(null);
        logoHandler = null;
    }

    @Override
    public void checkLocationPermission() {
        Log.d(Constants.LOG_TAG_SPLASHSCREEN, "Check location permission");
        if(ContextCompat.checkSelfPermission(act, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            splashScreenView.askForLocationPermission(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, Constants.LOC_REQ_PERMISSIONS_ID);
        }else{
            checkLocationProvider();
        }
    }

    public void checkLocationProvider() {
        Log.d(Constants.LOG_TAG_SPLASHSCREEN, "Check location provider");
        LocationManager manager = (LocationManager) act.getApplicationContext().getSystemService( Context.LOCATION_SERVICE );
        if ( manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            createWeakActivity();
        } else {
            splashScreenView.askToTurnOnLocation(act.getString(R.string.turn_on_loc_mes));
        }
    }

    @Override
    public void permissionDenied() {
        Log.d(Constants.LOG_TAG_SPLASHSCREEN, "Permission denied");
        splashScreenView.permissionDeniedDialog(act.getString(R.string.give_permissions));
    }

    @Override
    public void changePermissions() {
        pi.changePermissionSettings();
    }

    @Override
    public void turnOnLocation() {
        pi.changeLocationSettings();
    }
}
