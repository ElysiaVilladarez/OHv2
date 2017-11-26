package com.oohana.home;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.oohana.R;
import com.oohana.api.GeofenceList;
import com.oohana.api.LogBody;
import com.oohana.api.RetrofitService;
import com.oohana.api.SyncServerResult;
import com.oohana.database.ServerGeofence;
import com.oohana.database.TriggeredLogs;
import com.oohana.helpers.Alarms;
import com.oohana.helpers.Constants;
import com.oohana.helpers.FirebaseDispatchers;
import com.oohana.helpers.PermissionIntent;
import com.oohana.helpers.SyncLogsToServer;
import com.oohana.services.GeofenceTriggeredReceiver;
import com.oohana.services.LocationUpdatesService;

import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by elysi on 10/29/2017.
 */

public class HomePresenter implements HomeContract.HomePresenterToModel, HomeContract.HomePresenterToView {
    private HomeContract.HomeModelToPresenter homeModel;
    private HomeContract.HomeViewToPresenter homeView;
    private Activity act;
    private PermissionIntent pi;
    private Alarms alarms;
    private FirebaseDispatchers fd;
    private Retrofit retrofitApi;
    private RetrofitService getService;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private GeofencingRequest.Builder geofencingRequestBuilder;

    public HomePresenter(HomeContract.HomeViewToPresenter homeView) {
        this.homeView = homeView;
        this.act = homeView.getActivity();
        homeModel = new HomeModel();

        homeModel.instantiateRealm(homeView.getActivity().getApplicationContext());
        pi = new PermissionIntent(act.getApplicationContext());
        alarms = new Alarms(act.getApplicationContext());

        fd = new FirebaseDispatchers(act.getApplication());
        createLocationRequest();

        if (retrofitApi == null) {
            retrofitApi = new Retrofit.Builder()
                    .baseUrl(Constants.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }

        getService = retrofitApi.create(RetrofitService.class);

    }

    @Override
    public void setUpResourcesAndServices(Context c) {
        fd.scheduleJob(Constants.SYNC_LOGS_TAG, true, Constants.SYNC_LOGS_TIME_MIN, true);

        fetchGeofencesFromServer();
        if(homeModel.getLogCount() > 0 && Constants.isInternetAvailable(act.getApplicationContext())) syncLogsToServerAsync();
        if(homeModel.getServerGeofenceCount() > 0){
            connectToGoogleApi();
        }
        else if(!Constants.isInternetAvailable(act.getApplicationContext())){
            homeView.askToConnectToInternet(act.getString(R.string.connect_to_internet_mes));
        }

    }

    @Override
    public void checkGeofencesContent() {
        if(homeModel.getServerGeofenceCount() > 0){
            connectToGoogleApi();
        }
        else if(!Constants.isInternetAvailable(act.getApplicationContext())){
            homeView.askToConnectToInternet(act.getString(R.string.connect_to_internet_mes));
        } else if(homeModel.getServerGeofenceCount() <= 0){
            fetchGeofencesFromServer();
        }
    }

    @Override
    public void registerReceiverToBroadcast(BroadcastReceiver receiver){
        LocalBroadcastManager.getInstance(act).registerReceiver(receiver, new IntentFilter(Constants.ACTION_UPDATE_LOC_UI));
        act.registerReceiver(receiver, new IntentFilter());
    }



    @Override
    public void turnOnLocation() {
        pi.changeLocationSettings();
    }

    @Override
    public void checkLocationProvider() {
        Log.d(Constants.LOG_TAG_HOME, "Check location provider");
        LocationManager manager = (LocationManager) act.getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            homeView.askToTurnOnLocation(act.getString(R.string.turn_on_loc_mes));
        }
    }

    @Override
    public void permissionDenied() {
        homeView.permissionDeniedDialog(act.getString(R.string.give_permissions));
    }

    @Override
    public void fetchGeofencesFromServer() {
        Call<GeofenceList> fetch = getService.geofenceList();
        fetch.enqueue(new Callback<GeofenceList>() {
            @Override
            public void onResponse(Call<GeofenceList> call, Response<GeofenceList> response) {
                Log.d(Constants.LOG_TAG_HOME, "Getting geofence list successful: " + response.body().getGeofenceList.size() + " items");
                List<ServerGeofence> result = response.body().getGeofenceList;
                homeModel.deleteGeofences();
                homeModel.addServerGeofences(result);
                homeView.setGeofencesActive(Integer.toString(result.size()));
                connectToGoogleApi();
            }

            @Override
            public void onFailure(Call<GeofenceList> call, Throwable t) {
                Log.d(Constants.LOG_TAG_HOME, "Getting geofence list fail: " + t.toString());
            }
        });
    }

    @Override
    public void syncLogsToServerAsync() {
        SyncLogsToServer.syncLogs(act.getApplicationContext(), getService, homeModel);
    }


    //Connect to Google Play services
    public void connectToGoogleApi() {
        if (googleApiClient == null) {
            Log.d(Constants.LOG_TAG_HOME, "Building Google API . . .");
            googleApiClient = new GoogleApiClient.Builder(act)
                    .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                        @Override
                        public void onConnected(@Nullable Bundle bundle) {
                            Log.d(Constants.LOG_TAG_HOME, "Google API connected!");
                            if (homeModel.getServerGeofenceCount() > 0) {
                                homeView.setGeofencesActive(Integer.toString(homeModel.getServerGeofenceCount()));
                                startLocationUpdates();
                                startGeofencing();
                            }
                        }

                        @Override
                        public void onConnectionSuspended(int i) {
                            if (googleApiClient != null) {
                                googleApiClient.connect();
                            }
                        }
                    })
                    .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                        @Override
                        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                            Log.d(Constants.LOG_TAG_HOME, "Failed to connect to Google API: " + connectionResult.getErrorMessage());
                        }
                    })
                    .addApi(LocationServices.API).build();

        } else if (googleApiClient != null && !googleApiClient.isConnected() && !googleApiClient.isConnecting()) {
            Log.d(Constants.LOG_TAG_HOME, "Reconnecting Google API . . .");
            googleApiClient.connect();
        }
    }

    @Override
    public void launchLocationSettings() {
        pi.changeLocationSettings();
    }

    @Override
    public void stopJob(String tag) {
        fd.cancelJob(tag);
    }

    @Override
    public void stopAllJobs() {
        fd.cancelAllJobs();
    }

    //Get location
    protected void createLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(Constants.LOC_UPDATE_INTERVAL);
        locationRequest.setFastestInterval(Constants.LOC_UPDATE_FASTEST_INTERVAL);
       // locationRequest.setSmallestDisplacement(0);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public void startLocationUpdates() {
        if (locationRequest != null) {
            Log.d(Constants.LOG_TAG_HOME, "Starting location updates . . .");
            if (ActivityCompat.checkSelfPermission(act, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(act, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                homeView.askToTurnOnLocation(act.getString(R.string.turn_on_loc_mes));
            } else {
                Location location = LocationServices.FusedLocationApi.getLastLocation(
                        googleApiClient);

                Intent locationUpdatesIntent = new Intent(act, LocationUpdatesService.class);
                if(location != null) {
                    homeView.setLatLng(String.valueOf(location.getLatitude()), String.valueOf(location.getLongitude()));
                    homeModel.setLocationDifference(location);
                    SharedPreferences prefs = act.getApplicationContext().getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
                    prefs.edit().putString(Constants.LOC_CURR_LATLNG_KEY, Double.toString(location.getLatitude()) + ","+ Double.toString(location.getLongitude())).commit();
                    //locationUpdatesIntent.addCategory(Double.toString(location.getLatitude()) + ","+ Double.toString(location.getLongitude()));
                }
//                LocationServices.FusedLocationApi.requestLocationUpdates(
//                        googleApiClient, locationRequest, this);
                PendingIntent locationUpdatesPendingIntent = PendingIntent.getService(act.getApplicationContext(), Constants.FETCH_PENDING_INTENT_ID,
                        locationUpdatesIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, locationUpdatesPendingIntent);
            }
        }
    }

    @Override
    public void stopLocationUpdates() {
        if(googleApiClient != null) {
            Intent locationUpdatesIntent = new Intent(act, LocationUpdatesService.class);
            PendingIntent locationUpdatesPendingIntent = PendingIntent.getService(act.getApplicationContext(), Constants.FETCH_PENDING_INTENT_ID,
                    locationUpdatesIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            LocationServices.FusedLocationApi.removeLocationUpdates(
                    googleApiClient, locationUpdatesPendingIntent);
        }
    }

    @Override
    public void unregisterReceiver(BroadcastReceiver receiver) {
        try {
            LocalBroadcastManager.getInstance(act).unregisterReceiver(receiver);
            act.unregisterReceiver(receiver);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stopService() {
        Intent i = new Intent(act, LocationUpdatesService.class);
        i.setAction(Constants.ACTION_STOP_SERVICE);
        act.startService(i);
    }
    //Geofence methods
    @Override
    public void startGeofencing() {
        removeAllGeofences();
        ArrayList<Geofence> gL = homeModel.getNearestStoredGeofences();
        Log.d(Constants.LOG_TAG_HOME, "Starting geofencing . . . " + gL.size() + " Geofences");
        addToGeofencingRequest(gL);
        activateGeofencingApi();
    }

    protected void addToGeofencingRequest(ArrayList<Geofence> gL) {
        if (geofencingRequestBuilder == null) {
            geofencingRequestBuilder = new GeofencingRequest.Builder()
                    .setInitialTrigger(Constants.GEOFENCE_INITIAL_TRIGGER);
        }
        geofencingRequestBuilder.addGeofences(gL);


    }

    @Override
    public void removeAllGeofences(){
        if(googleApiClient != null) {
            Intent geofenceIntent = new Intent(act.getApplicationContext(), GeofenceTriggeredReceiver.class);
            geofenceIntent.setAction(Constants.ACTION_GEOFENCE_TRIGGERED);
            PendingIntent geofencePendingIntent = PendingIntent.getBroadcast(act.getApplicationContext(), Constants.GEOFENCE_PENDING_INTENT_ID,
                    geofenceIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            LocationServices.GeofencingApi.removeGeofences(googleApiClient, geofencePendingIntent);
        }
    }

    @Override
    public void gpsOffAction() {
        if(googleApiClient != null && googleApiClient.isConnected()) {
            Intent geofenceIntent = new Intent(act.getApplicationContext(), GeofenceTriggeredReceiver.class);
            geofenceIntent.setAction(Constants.ACTION_GEOFENCE_TRIGGERED);
            PendingIntent geofencePendingIntent = PendingIntent.getBroadcast(act.getApplicationContext(), Constants.GEOFENCE_PENDING_INTENT_ID,
                    geofenceIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            LocationServices.GeofencingApi.removeGeofences(googleApiClient, geofencePendingIntent);
            googleApiClient = null;
        }else if(googleApiClient != null){
            googleApiClient = null;
        }
    }

    protected void activateGeofencingApi() {
        Intent geofenceIntent = new Intent(act.getApplicationContext(), GeofenceTriggeredReceiver.class);
        geofenceIntent.setAction(Constants.ACTION_GEOFENCE_TRIGGERED);
        PendingIntent geofencePendingIntent = PendingIntent.getBroadcast(act.getApplicationContext(), Constants.GEOFENCE_PENDING_INTENT_ID,
                geofenceIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        if (ActivityCompat.checkSelfPermission(act, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            homeView.askForLocationPermission(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, Constants.LOC_REQ_PERMISSIONS_ID);
        }else {
            LocationServices.GeofencingApi.addGeofences(googleApiClient, geofencingRequestBuilder.build(), geofencePendingIntent)
                    .setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(Status status) {
                            if (status.isSuccess()) {
                                Log.d(Constants.LOG_TAG_HOME, "Geofences successfully added!");
                                Toast.makeText(act.getApplicationContext(), "Geofences have been activated!", Toast.LENGTH_SHORT).show();

                            } else if (status.getStatusCode() == 1000) {
                                Log.d(Constants.LOG_TAG_HOME, "Error adding geofences: " + status.getStatusCode() + ":" + status.getStatusMessage());
                                homeView.setToHighAccuracy(act.getString(R.string.switch_to_high_mes));
                            } else {
                                Log.d(Constants.LOG_TAG_HOME, "Error adding geofences: " + status.getStatusCode() + ":" + status.getStatusMessage());
                                Toast.makeText(act.getApplicationContext(), "Error status code: " + status.getStatusCode(), Toast.LENGTH_LONG).show();
                            }

                        }
                    });
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
