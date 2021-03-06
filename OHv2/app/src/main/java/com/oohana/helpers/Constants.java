package com.oohana.helpers;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;

/**
 * Created by elysi on 10/28/2017.
 */

public class Constants {
    //debugging
    public final static String LOG_TAG_SPLASHSCREEN = "SPLASH_SCREEN";
    public final static String LOG_TAG_HOME = "HOME";
    public final static String LOG_TAG_RECEIVER = "GEOFENCE_RECEIVER";
    public final static String LOG_TAG_ALARMS = "ALARMS";
    public final static String LOG_TAG_JOB = "FIREBASE_JOB";
    public final static String LOG_TAG_LOCATIONSERVICE = "LOCATION_SERVICE";
    public final static String LOG_TAG_TRANS_DIALOG = "TRANS_DIALOG";


    public final static int LOC_REQ_PERMISSIONS_ID = 100;

    public final static int LOGO_DISPLAY_LENGTH_MS = 1500;

    public final static String PREFS_NAME = "OOHANA_PREFS";

    public final static String BASE_URL = "http://oohana.technotrekinc.com/";
    public final static String GET_GEOFENCE_API= "get_geofence_list.php";
    public final static String POST_LOGS_API= "insert_to_server.php";

    public final static int LOC_UPDATE_INTERVAL = 1000 * 60;
    public final static int LOC_UPDATE_FASTEST_INTERVAL = 1000 * 45;
    public final static String LOC_CURR_LATLNG_KEY = "CURR_LAT_LNG";
    public final static String HAS_SIG_LOC_CHANGE_KEY = "HAS_SIGNIFICANT_CHANGE";
    public final static double LOC_SIGNIFICANT_DIFF_THRESHOLD = 100; //in meters


    public final static int GEOF_TRANSITION_TYPES = Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_DWELL
            | Geofence.GEOFENCE_TRANSITION_EXIT;

    public static final double HARVERSIN_CONVERSION = 6372.8; // In kilometers
    public static final int GEOFENCE_RADIUS_DEFAULT_VALUE = 5;
    public static final int GEOFENCE_LOITERING_DELAY = 1000 * 60 * 10;
    public static final int GEOFENCE_PENDING_INTENT_ID = 220;
    public static final int LOC_UPDATES_PENDING_INTENT_ID = 240;

    public static final int GEOFENCE_INITIAL_TRIGGER = GeofencingRequest.INITIAL_TRIGGER_ENTER;

    public final static String SYNC_LOGS_TAG = "SYNC_LOGS_TAG";
    public static final int SYNC_LOGS_TIME_MIN = 60 * 60 * 2; //2 hours
    public final static String FETCH_LOGS_TAG = "FETCH_LOGS_TAG";
    public static final int FETCH_LOGS_TIME_MIN = 60 * 60 * 5; // 5 hours

    public static final String SHARED_PREF_HAS_SYNCED_BEFORE_KEY = "HAS_ATTEMPTED_SYNCED";
    public static final String SHARED_PREF_HAS_FETCHED_BEFORE_KEY = "HAS_ATTEMPTED_FETCHING";

    public static final String ACTION_GEOFENCE_TRIGGERED = "com.oohana.ohv2.ACTION_GEOFENCE_TRIGGERED";
    public final static String ACTION_UPDATE_LOC_UI = "com.oohana.ohv2.ACTION_UPDATE_LOC_UI";
    public final static String ACTION_PROVIDERS_CHANGED = "android.location.PROVIDERS_CHANGED";
    public final static String ACTION_STOP_SERVICE = "action.STOP_SERVICE";
    public final static String ACTION_GPS_OFF = "action.ACTION_GPS_OFF";
    public final static String ACTION_GPS_ON = "action.ACTION_GPS_ON";
    public final static String ACTION_WIFI_STATE_CHANGED = "android.net.wifi.WIFI_STATE_CHANGED";
    public final static String ACTION_STATE_CHANGED = "android.net.wifi.STATE_CHANGE";

    public final static String ACTION_UPDATE_LOG_COUNT = "action.ACTION_UPDATE_LOG_COUNT";
    public final static String ACTION_UPDATE_GEO_COUNT = "action.ACTION_UPDATE_GEO_COUNT";

    public final static String LOG_COUNT_KEY = "LOG_COUNT_KEY";
    public final static String GEOF_COUNT_KEY = "GEOF_COUNT_KEY";

    public final static String NOTIFICATION_TITLE = "OOHANA";
    public final static String NOTIFICATION_CONTENT_TEXT = "OOHANA is logging your location . . .";
    public final static int HOME_PEND_REQ_CODE = 200;
    public final static int STOP_PEND_REQ_CODE = 250;
    public final static int SERVICE_FOREGROUND_REQ_CODE = 250;


    public static final SimpleDateFormat LAST_UPDATED_FORMAT = new SimpleDateFormat("MMM. dd yyyy, EEE, h:mm a");
    public final static SimpleDateFormat SYNC_SERVER_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static boolean isInternetAvailable(Context c) {
        ConnectivityManager manager = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();

        boolean isAvailable = false;
        if (networkInfo != null && networkInfo.isConnected()) {
            isAvailable = true;
        }
        return isAvailable;
    }

    public static String getMACAddress(String interfaceName) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                if (interfaceName != null) {
                    if (!intf.getName().equalsIgnoreCase(interfaceName)) continue;
                }
                byte[] mac = intf.getHardwareAddress();
                if (mac==null) return "";
                StringBuilder buf = new StringBuilder();
                for (int idx=0; idx<mac.length; idx++)
                    buf.append(String.format("%02X:", mac[idx]));
                if (buf.length()>0) buf.deleteCharAt(buf.length()-1);
                return buf.toString();
            }
        } catch (Exception ex) { Log.d(Constants.LOG_TAG_HOME, ex.getMessage());}
        return "";
    }
}
