package com.oh.ohv2.home;

import android.app.Activity;
import android.content.Context;
import android.location.Location;

import com.google.android.gms.location.Geofence;
import com.oh.ohv2.database.GeofenceLog;
import com.oh.ohv2.database.ServerGeofence;

import java.util.ArrayList;
import java.util.List;

import io.realm.RealmObject;

/**
 * Created by elysi on 10/29/2017.
 */

public interface HomeContract {
    //View to Presenter
    interface HomeViewToPresenter{
        Activity getActivity();
        void setLatLng(String lat, String lng);
        void setGeofencesActive(String geofencesActive);
        void askToTurnOnLocation(String message);
    }
    //Presenter To View
    interface HomePresenterToView{
        void instantiateDatabase(Context c);
        void turnOnLocation();
        void checkLocationProvider();
        void fetchGeofencesFromServer();
        void connectToGoogleApi();
        void startSyncTimer();
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
        ArrayList<GeofenceLog> getLogs();
        void deleteLogs();
    }

}
