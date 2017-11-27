package com.oohana.home;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.location.Location;

import com.google.android.gms.location.Geofence;
import com.oohana.database.GeofenceLog;
import com.oohana.database.ServerGeofence;
import com.oohana.database.TriggeredLogs;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by elysi on 10/29/2017.
 */

public interface HomeContract {
    //View to Presenter
    interface HomeViewToPresenter{
        Activity getActivity();
        void setLatLng(String lat, String lng);
        void setGeofencesActive(String geofencesActive);
        void setGeofencesCount(String geofCount);
        void setLogCount(String logsCount);
        void askToTurnOnLocation(String message);
        void askToConnectToInternet(String message);
        void permissionDeniedDialog(String mes);
        void askForLocationPermission(String[] permissionsNeeded, int reqId);
        void setToHighAccuracy(String message);
    }
    //Presenter To View
    interface HomePresenterToView{
        void setUpResourcesAndServices(Context c);
        void checkGeofencesContent();
        void registerReceiverToBroadcast(BroadcastReceiver receiver);
        void turnOnLocation();
        void checkLocationProvider();
        void permissionDenied();
        void fetchGeofencesFromServer(boolean showToasts);
        void syncLogsToServerAsync(boolean showToasts);
        void connectToGoogleApi();
        void launchLocationSettings();
        void stopJob(String tag);
        void stopAllJobs();
        void stopLocationUpdates();
        void unregisterReceiver(BroadcastReceiver receiver);
        void stopService();
        void removeAllGeofences();
        void gpsOffAction();
    }

    //Presenter to Model
    interface HomePresenterToModel{
        void startGeofencing();
        void startLocationUpdates();
    }

    //Model to Presenter
    interface HomeModelToPresenter{
        void instantiateRealm(Context c);
        ArrayList<Geofence> getNearestStoredGeofences();
        int getServerGeofenceCount();
        void addServerGeofences(List<ServerGeofence> sGs);
        void setLocationDifference(Location location);
        void addLogs(int geofenceTransition, List<Geofence> geofences);
        ArrayList<TriggeredLogs> getLogs();
        int getLogCount();
        void deleteLogs();
        void deleteGeofences();
    }

}
