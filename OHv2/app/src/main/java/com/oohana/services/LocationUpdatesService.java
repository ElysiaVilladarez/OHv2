package com.oohana.services;

import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.location.LocationResult;
import com.oohana.R;
import com.oohana.helpers.Constants;
import com.oohana.helpers.HelperMethods;
import com.oohana.home.HomeActivity;
import com.oohana.home.HomeContract;
import com.oohana.home.HomeModel;

/**
 * Created by SaperiumDev on 11/22/2017.
 */

public class LocationUpdatesService extends Service {
//    public LocationUpdatesService(){
//        super("LocationUpdatesService");
//
//    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    private PowerManager.WakeLock cpuWakeLock;
//    @Override
//    protected void onHandleIntent(@Nullable Intent intent) {
//        //TO DO make service sticky and startforeground
//        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
//        cpuWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
//                "gps_service");
//        if ((cpuWakeLock != null) && (cpuWakeLock.isHeld() == false)) {
//            cpuWakeLock.acquire();
//        }
//
//        if (LocationResult.hasResult(intent)) {
//            SharedPreferences prefs = getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
//            LocationResult locationResult = LocationResult.extractResult(intent);
//            Location location = locationResult.getLastLocation();
//            if (location != null) {
//                String curLoc = prefs.getString(Constants.LOC_CURR_LATLNG_KEY, "");
//                if(!curLoc.isEmpty()) {
//                    HelperMethods hm = new HelperMethods();
//                    String[] latlng = curLoc.split(",");
//                    Location loc = new Location("");
//                    loc.setLatitude(Double.parseDouble(latlng[0]));
//                    loc.setLongitude(Double.parseDouble(latlng[1]));
//                    //if location change is significant, set Location difference of each geofence
//                    if (hm.haversine(loc.getLatitude(), loc.getLongitude(), location.getLatitude(), location.getLongitude()) > Constants.LOC_SIGNIFICANT_DIFF_THRESHOLD) {
//                        Log.d(Constants.LOG_TAG_LOCATIONSERVICE, "Detected significant location difference: " +
//                                hm.haversine(loc.getLatitude(), loc.getLongitude(), location.getLatitude(), location.getLongitude()) + " meters");
//                        HomeContract.HomeModelToPresenter homeModel = new HomeModel();
//                        homeModel.instantiateRealm(this);
//                        homeModel.setLocationDifference(loc);
//                    }
//                }
//                Intent locIntent = new Intent();
//                prefs = getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
//                prefs.edit().putString(Constants.LOC_CURR_LATLNG_KEY, Double.toString(location.getLatitude()) + ","+ Double.toString(location.getLongitude())).commit();
//                LocalBroadcastManager.getInstance(LocationUpdatesService.this).sendBroadcast(locIntent.setAction(Constants.ACTION_UPDATE_LOC_UI));
//                Log.d(Constants.LOG_TAG_LOCATIONSERVICE, "Accuracy: " + location.getAccuracy() + " Lat: " + location.getLatitude() + " Long: " + location.getLongitude());
//            }
//        }
//    }

    @Override
    public void onDestroy(){
        super.onDestroy();
//        if (cpuWakeLock.isHeld())
//            cpuWakeLock.release();
        stopForeground(true);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(Constants.LOG_TAG_LOCATIONSERVICE, "Received Start Foreground Intent ");

//        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
//        cpuWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
//                "gps_service");
//        if ((cpuWakeLock != null) && (cpuWakeLock.isHeld() == false)) {
//            cpuWakeLock.acquire();
//        }
        if (LocationResult.hasResult(intent)) {
            SharedPreferences prefs = getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
            LocationResult locationResult = LocationResult.extractResult(intent);
            Location location = locationResult.getLastLocation();
            Intent locIntent = new Intent();
            if (location != null) {
                String curLoc = prefs.getString(Constants.LOC_CURR_LATLNG_KEY, "");
                if (!curLoc.isEmpty()) {
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
                        intent.putExtra(Constants.HAS_SIG_LOC_CHANGE_KEY, true);
                    }
                }
                prefs = getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
                prefs.edit().putString(Constants.LOC_CURR_LATLNG_KEY, Double.toString(location.getLatitude()) + "," + Double.toString(location.getLongitude())).commit();
                LocalBroadcastManager.getInstance(LocationUpdatesService.this).sendBroadcast(locIntent.setAction(Constants.ACTION_UPDATE_LOC_UI));
                Log.d(Constants.LOG_TAG_LOCATIONSERVICE, "Accuracy: " + location.getAccuracy() + " Lat: " + location.getLatitude() + " Long: " + location.getLongitude());
            }
        }

        Intent homeIntent = new Intent(this, HomeActivity.class);
        homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        PendingIntent homePendingIntent = PendingIntent.getActivity(this, Constants.HOME_PEND_REQ_CODE, homeIntent, 0);

        Intent stopIntent = new Intent(this, HomeActivity.class);
        stopIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        stopIntent.setAction(Constants.ACTION_STOP_SERVICE);
        PendingIntent stopPendingIntent = PendingIntent.getActivity(this, Constants.STOP_PEND_REQ_CODE, stopIntent, 0);

        Bitmap icon = BitmapFactory.decodeResource(getResources(),
                R.mipmap.oohana_icon);

        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle(Constants.NOTIFICATION_TITLE)
                .setContentText(Constants.NOTIFICATION_CONTENT_TEXT)
                .setSmallIcon(R.mipmap.oohana_icon)
                .setLargeIcon(Bitmap.createScaledBitmap(icon, 128, 128, false))
                .setContentIntent(homePendingIntent)
                .setOngoing(true)
                .addAction(R.mipmap.ic_stop_white_24dp, "Stop Logs", stopPendingIntent)
                .build();

        startForeground(Constants.SERVICE_FOREGROUND_REQ_CODE, notification);

        if(Constants.ACTION_STOP_SERVICE.equals(intent.getAction())){
            stopForeground(true);
            stopSelf();
        }
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }
}
