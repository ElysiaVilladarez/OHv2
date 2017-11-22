package com.oh.ohv2.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.PowerManager;
import android.renderscript.Double2;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.common.stats.WakeLockEvent;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.oh.ohv2.helpers.Constants;
import com.oh.ohv2.helpers.HelperMethods;
import com.oh.ohv2.home.HomeContract;
import com.oh.ohv2.home.HomeModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by SaperiumDev on 11/22/2017.
 */

public class LocationUpdatesService extends IntentService {
    public LocationUpdatesService(){
        super("LocationUpdatesService");

    }
    public LocationUpdatesService(String name) {
        super(name);
    }

    private PowerManager.WakeLock cpuWakeLock;
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        //TO DO make service sticky and startforeground
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        cpuWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "gps_service");
        if ((cpuWakeLock != null) && (cpuWakeLock.isHeld() == false)) {
            cpuWakeLock.acquire();
        }

        if (LocationResult.hasResult(intent)) {
            LocationResult locationResult = LocationResult.extractResult(intent);
            Location location = locationResult.getLastLocation();
            if (location != null) {
                if(intent.getCategories()!= null){
                List setList = new ArrayList<>(intent.getCategories());
                String curLoc = setList.get(0).toString();
                if(curLoc != null && !curLoc.isEmpty()) {
                    HelperMethods hm = new HelperMethods();
                    String[] latlng = curLoc.split(",");
                    Location loc = new Location("");
                    loc.setLatitude(Double.parseDouble(latlng[0]));
                    loc.setLongitude(Double.parseDouble(latlng[1]));
                    //if location change is significant, set Location difference of each geofence
                    if (hm.haversine(loc.getLatitude(), loc.getLongitude(), location.getLatitude(), location.getLongitude()) > Constants.LOC_SIGNIFICANT_DIFF_THRESHOLD) {
                        Log.d(Constants.LOG_TAG_LOCATIONSERVICE, "Detected significant location difference: " +
                                hm.haversine(loc.getLatitude(), loc.getLongitude(), location.getLatitude(), location.getLongitude()) + " meters");
                        HomeContract.HomeModelToPresenter homeModel = new HomeModel();
                        homeModel.instantiateRealm(this);
                        homeModel.setLocationDifference(loc);
                    }
                }
                }
                Intent locIntent = new Intent();
                locIntent.putExtra(Constants.LOC_CURR_LATLNG_KEY, Double.toString(location.getLatitude()) + ","+ Double.toString(location.getLongitude()));
                LocalBroadcastManager.getInstance(LocationUpdatesService.this).sendBroadcast(locIntent.setAction(Constants.ACTION_UPDATE_LOC_UI));
                Log.d(Constants.LOG_TAG_LOCATIONSERVICE, "Accuracy: " + location.getAccuracy() + " Lat: " + location.getLatitude() + " Long: " + location.getLongitude());
            }
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if (cpuWakeLock.isHeld())
            cpuWakeLock.release();

    }
}
