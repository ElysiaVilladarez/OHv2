package com.oh.ohv2.helpers;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;

import java.text.SimpleDateFormat;

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


    public final static int LOC_REQ_PERMISSIONS_ID = 100;

    public final static int LOGO_DISPLAY_LENGTH_MS = 1500;

    public final static String BASE_URL = "http://oohana.technotrekinc.com/";
    public final static String GET_GEOFENCE_API= "get_geofence_list.php";
    public final static String POST_LOGS_API= "insert_to_server.php";

    public final static int LOC_UPDATE_INTERVAL = 1000 * 60;
    public final static int LOC_UPDATE_FASTEST_INTERVAL = 1000 * 45;
    public final static String LOC_CURR_LATLNG_KEY = "CURR_LAT_LNG";
    public final static double LOC_SIGNIFICANT_DIFF_THRESHOLD = 100; //in meters


    public final static int GEOF_TRANSITION_TYPES = Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_DWELL
            | Geofence.GEOFENCE_TRANSITION_EXIT;

    public static final double HARVERSIN_CONVERSION = 6372.8; // In kilometers
    public static final int GEOFENCE_RADIUS_DEFAULT_VALUE = 5;
    public static final int GEOFENCE_LOITERING_DELAY = 1000 * 60 * 10;
    public static final int GEOFENCE_PENDING_INTENT_ID = 220;
    public static final int SYNC_PENDING_INTENT_ID = 230;
    public static final int FETCH_PENDING_INTENT_ID = 240;

    public static final int GEOFENCE_INITIAL_TRIGGER = GeofencingRequest.INITIAL_TRIGGER_ENTER;
    //public static final long SYNC_LOGS_TIME = 1000 * 60 * 60 * 2;
    public static final long FETCH_LOGS_TIME = 1000 * 60 * 25;

    public final static String SYNC_LOGS_TAG = "SYNC_LOGS_TAG";
    public static final int SYNC_LOGS_TIME_MIN = 1000 * 60 * 60 * 2;
    public static final int FETCH_LOGS_TIME_MIN = 60 * 25;

    public static final String ACTION_GEOFENCE_TRIGGERED = "com.oh.ohv2.ACTION_GEOFENCE_TRIGGERED";
    public static final String ACTION_SYNC_LOGS = "com.oh.ohv2.ACTION_SYNC_LOGS";
    public static final String ACTION_FETCH_GEOFENCES ="com.oh.ohv2.ACTION_FETCH_GEOFENCES";
    public final static String ACTION_UPDATE_LOC_UI = "com.oh.ohv2.ACTION_UPDATE_LOC_UI";
    public final static String ACTION_PROVIDERS_CHANGED = "android.location.PROVIDERS_CHANGED";

    public static final SimpleDateFormat LAST_UPDATED_FORMAT = new SimpleDateFormat("MMM. dd yyyy, EEE, h:mm a");
}
