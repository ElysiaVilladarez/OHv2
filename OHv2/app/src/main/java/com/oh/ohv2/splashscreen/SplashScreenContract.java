package com.oh.ohv2.splashscreen;

import android.app.Activity;

/**
 * Created by elysi on 10/28/2017.
 */

public interface SplashScreenContract {
    //Presenter to view
    interface SplashScreenPresenterToView{
        void destroyWeakActivity();
        void checkLocationPermission();
        void checkLocationProvider();
        void permissionDenied();
        void changePermissions();
        void turnOnLocation();
    }

   //View to presenter
    interface SplashScreenViewToPresenter{
       Activity getActivity();
       void askForLocationPermission(String[] permissionNeeded, int requestId);
       void askToTurnOnLocation(String message);
       void permissionDeniedDialog(String message);
       void goToHomeActivity(Activity act);
   }
}
