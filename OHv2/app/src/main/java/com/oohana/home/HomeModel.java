package com.oohana.home;

import android.content.Context;

import android.location.Location;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.oohana.database.GeofenceLog;
import com.oohana.database.ServerGeofence;
import com.oohana.database.TriggeredLogs;
import com.oohana.helpers.Constants;
import com.oohana.helpers.HelperMethods;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Created by elysi on 10/29/2017.
 */

public class HomeModel implements HomeContract.HomeModelToPresenter {
    private  ArrayList<Geofence> geofences;
    private ArrayList<TriggeredLogs> logs;
    private HelperMethods hm;

    public HomeModel(){
        hm = new HelperMethods();
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
                    List<ServerGeofence> list;
                    if(rr.size() > 97) list = rr.subList(0, 97);
                    else list = rr;
                    for (ServerGeofence g: list) {
                        geofences.add(hm.createGeofence(Integer.toString(g.getGeofId()), g.getGeofLat(), g.getGeofLng(), g.getGeofRad()));
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
    public void setLocationDifference(final Location location) {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                RealmResults<ServerGeofence> sgList = realm.where(ServerGeofence.class).findAll();
                for (ServerGeofence a : sgList) {
                    a.setNearness(hm.haversine(location.getLatitude(), location.getLongitude(), a.getGeofLat(), a.getGeofLng()));
                }
            }
        });
        realm.close();
    }

    @Override
    public void addLogs(final int geofenceTransition, final List<Geofence> geofences) {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                for(Geofence g: geofences){
                    GeofenceLog tg = new GeofenceLog();
                    tg.setId(Integer.parseInt(g.getRequestId()));
                    tg.setStatus(hm.getStatus(geofenceTransition));
                    tg.setTimeStamp(Calendar.getInstance().getTime());
                    realm.insert(tg);
                }
            }
        });
        Log.d(Constants.LOG_TAG_HOME, "Logs in db: " + realm.where(GeofenceLog.class).count());
        realm.close();
    }

    @Override
    public ArrayList<TriggeredLogs> getLogs() {
        logs = new ArrayList<>();
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                RealmResults<GeofenceLog> rr  = realm.where(GeofenceLog.class).findAll();
                if (rr != null) {
                    for (GeofenceLog s: rr) {
                        logs.add(new TriggeredLogs(s.getId(), s.getStatus(), s.getTimeStamp()));
                    }
                }
            }
        });
        realm.close();
        return logs;
    }

    @Override
    public int getLogCount() {
        int count;
        Realm realm = Realm.getDefaultInstance();
        count = realm.where(GeofenceLog.class).findAll().size();
        realm.close();
        return count;
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
    @Override
    public void deleteGeofences() {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.delete(ServerGeofence.class);
            }
        });
        realm.close();
    }
}
