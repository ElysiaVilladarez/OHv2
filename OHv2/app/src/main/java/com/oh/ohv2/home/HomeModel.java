package com.oh.ohv2.home;

import android.content.Context;

import android.location.Location;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.oh.ohv2.database.GeofenceLog;
import com.oh.ohv2.database.ServerGeofence;
import com.oh.ohv2.helpers.Constants;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.Sort;
import io.realm.annotations.RealmClass;

/**
 * Created by elysi on 10/29/2017.
 */

public class HomeModel implements HomeContract.HomeModelToPresenter {
    private  ArrayList<Geofence> geofences;
    private ArrayList<GeofenceLog> logs;

    public HomeModel(){
    }

    @Override
    public void instantiateRealm(Context c) {
        Realm.init(c);
    }

    @Override
    public ArrayList<Geofence> getNearestStoredGeofences() {
        geofences = new ArrayList<>();
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                RealmResults<ServerGeofence> rr  = realm.where(ServerGeofence.class).findAllSorted("nearness", Sort.ASCENDING);
                if (rr != null) {
                    if(rr.size() > 97) rr.subList(0, 97);
                    for (ServerGeofence g: rr) {
                        geofences.add(createGeofence(g.getGeofName(), g.getGeofLat(), g.getGeofLng(), g.getGeofRad()));
                    }
                }
            }
        });
        realm.close();
        return geofences;
    }

    @Override
    public int getServerGeofenceCount() {
        int count;
        Realm realm = Realm.getDefaultInstance();
        count = realm.where(ServerGeofence.class).findAll().size();
        realm.close();
        return count;
    }

    @Override
    public void addServerGeofences(final List<ServerGeofence> sGs) {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.copyToRealmOrUpdate(sGs);
            }
        });
        realm.close();
    }

    @Override
    public void setLocationDifference(Location location) {

    }

    @Override
    public void addLogs(final int geofenceTransition, final List<Geofence> geofences) {
        Log.d(Constants.LOG_TAG_HOME, "Addings logs to db: " + geofences.size() + " logs");
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                for(Geofence g: geofences){
                    GeofenceLog tg = new GeofenceLog();
                    tg.setTriggeredGeofence(realm.where(ServerGeofence.class).equalTo("geofName", g.getRequestId()).findFirst());
                    tg.setStatus(getStatus(geofenceTransition));
                    tg.setTimeStamp(Calendar.getInstance().getTime());
                    realm.insert(tg);
                }
            }
        });
        realm.close();
    }

    @Override
    public ArrayList<GeofenceLog> getLogs() {
        logs = new ArrayList<>();
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                RealmResults<GeofenceLog> rr  = realm.where(GeofenceLog.class).findAll();
                if (rr != null) {
                    for (GeofenceLog s: rr) {
                        logs.add(s);
                    }
                }
            }
        });
        realm.close();
        return logs;
    }

    @Override
    public void deleteLogs() {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.delete(GeofenceLog.class);
            }
        });
        realm.close();
    }

    protected double haversine(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.sin(dLon / 2) * Math.sin(dLon / 2) * Math.cos(lat1) * Math.cos(lat2);
        double c = 2 * Math.asin(Math.sqrt(a));
        return Constants.HARVERSIN_CONVERSION * c * 1000;
    }

    protected String getStatus(int geofenceTransition) {
        String status;
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            status = "Entering ";
        } else if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL) {
            status = "Dwelling ";
        } else if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            status = "Exiting ";
        }else{
            status = "Normal";
        }
        return status;
    }
    protected void setNearestLocation(final Location location){
            Realm realm = Realm.getDefaultInstance();
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    RealmResults<ServerGeofence> sgList = realm.where(ServerGeofence.class).findAll();
                    for (ServerGeofence a : sgList) {
                        a.setNearness(haversine(location.getLatitude(), location.getLongitude(), a.getGeofLat(), a.getGeofLng()));
                    }
                }
            });
        realm.close();
    }
    protected Geofence createGeofence(String name, double lat, double lng, float radius) {
        Log.d(Constants.LOG_TAG_HOME, "Creating geofence: " + name);
        if (radius <= 0) radius = Constants.GEOFENCE_RADIUS_DEFAULT_VALUE;
        return new Geofence.Builder()
                .setRequestId(name)
                .setCircularRegion(lat, lng, radius)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER
                        | Geofence.GEOFENCE_TRANSITION_DWELL
                        | Geofence.GEOFENCE_TRANSITION_EXIT)
                .setLoiteringDelay(Constants.GEOFENCE_LOITERING_DELAY)
                .build();
    }
}
