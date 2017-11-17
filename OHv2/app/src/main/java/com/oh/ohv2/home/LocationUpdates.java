package com.oh.ohv2.home;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.v4.app.ActivityCompat;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.oh.ohv2.helpers.Constants;

/**
 * Created by elysi on 10/29/2017.
 */

public class LocationUpdates {
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;

    public LocationUpdates(GoogleApiClient googleApiClient) {
        this.googleApiClient = googleApiClient;
    }


}
