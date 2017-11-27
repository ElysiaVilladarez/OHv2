package com.oohana.helpers;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.oohana.R;
import com.oohana.api.GeofenceList;
import com.oohana.api.LogBody;
import com.oohana.api.RetrofitService;
import com.oohana.api.SyncServerResult;
import com.oohana.database.ServerGeofence;
import com.oohana.database.TriggeredLogs;
import com.oohana.home.HomeContract;
import com.oohana.home.HomePresenter;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by SaperiumDev on 11/26/2017.
 */

public class SyncLogsToServer {

    public static void syncLogs(final Context c, RetrofitService getService, final HomeContract.HomeModelToPresenter homeModel, final boolean showToasts){
        Call<SyncServerResult> postLogs;
        final ArrayList<TriggeredLogs> logs = homeModel.getLogs();
        if(logs.size() > 0) {
            for (final TriggeredLogs l : logs) {
                Log.d(Constants.LOG_TAG_JOB, Integer.toString(l.getTriggeredGeofenceId()) + " " + l.getStatus() + " " +
                        Constants.SYNC_SERVER_FORMAT.format(l.getTimeStamp()));
                postLogs = getService.sendLog(new LogBody(Integer.toString(l.getTriggeredGeofenceId()), l.getStatus(), l.getTimeStamp()));
                postLogs.enqueue(new Callback<SyncServerResult>() {
                    @Override
                    public void onResponse(Call<SyncServerResult> call, Response<SyncServerResult> response) {
                        Log.d(Constants.LOG_TAG_RECEIVER, "Posting logs successful: " + response.body().result);
                        if (l == logs.get(logs.size() - 1)) {
                            Log.d(Constants.LOG_TAG_RECEIVER, "Deleting " + logs.size() + " logs from db . . .");
                            homeModel.deleteLogs();
                            Log.d(Constants.LOG_TAG_RECEIVER, "Log deleted. Log count : " + homeModel.getLogCount());

                            SharedPreferences prefs = c.getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE);
                            prefs.edit().putBoolean(Constants.SHARED_PREF_HAS_SYNCED_BEFORE_KEY, false).apply();

                            if (showToasts) {
                                Toast.makeText(c, c.getString(R.string.toast_sync_success), Toast.LENGTH_SHORT).show();
                            }
                            Intent trigIntent = new Intent();
                            trigIntent.setAction(Constants.ACTION_UPDATE_LOG_COUNT);
                            trigIntent.putExtra(Constants.LOG_COUNT_KEY, homeModel.getLogCount());
                            c.sendBroadcast(trigIntent);
                        }
                    }

                    @Override
                    public void onFailure(Call<SyncServerResult> call, Throwable t) {
                        Log.d(Constants.LOG_TAG_HOME, "Posting logs failed: " + t.toString());

                        SharedPreferences prefs = c.getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE);
                        prefs.edit().putBoolean(Constants.SHARED_PREF_HAS_SYNCED_BEFORE_KEY, true).apply();

                        if (showToasts) {
                            if (!Constants.isInternetAvailable(c))
                                Toast.makeText(c, c.getString(R.string.toast_sync_fail) + " "+c.getString(R.string.connect_to_internet_mes2), Toast.LENGTH_LONG).show();
                            else
                                Toast.makeText(c, c.getString(R.string.toast_sync_fail) + " "+ t.toString(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        } else if(showToasts){
            Toast.makeText(c, "There are no logs to sync.", Toast.LENGTH_SHORT).show();
        }
    }
    public static void fetchGeofencesFromServer(final Context c, RetrofitService getService, final HomeContract.HomeModelToPresenter homeModel, final boolean showToasts){
        Call<GeofenceList> fetch = getService.geofenceList();
        fetch.enqueue(new Callback<GeofenceList>() {
            @Override
            public void onResponse(Call<GeofenceList> call, Response<GeofenceList> response) {
                Log.d(Constants.LOG_TAG_HOME, "Getting geofence list successful: " + response.body().getGeofenceList.size() + " items");
                List<ServerGeofence> result = response.body().getGeofenceList;
                homeModel.deleteGeofences();
                homeModel.addServerGeofences(result);

                if(showToasts){
                    Toast.makeText(c, c.getString(R.string.toast_fetch_success), Toast.LENGTH_SHORT).show();
                }

                Intent trigIntent = new Intent();
                trigIntent.setAction(Constants.ACTION_UPDATE_GEO_COUNT);
                trigIntent.putExtra(Constants.GEOF_COUNT_KEY, homeModel.getServerGeofenceCount());
                c.sendBroadcast(trigIntent);
            }

            @Override
            public void onFailure(Call<GeofenceList> call, Throwable t) {
                Log.d(Constants.LOG_TAG_HOME, "Getting geofence list fail: " + t.toString());
                if(showToasts){
                    if(!Constants.isInternetAvailable(c))
                        Toast.makeText(c, c.getString(R.string.toast_fetch_fail) + c.getString(R.string.connect_to_internet_mes2), Toast.LENGTH_LONG).show();
                    else
                        Toast.makeText(c, c.getString(R.string.toast_fetch_fail) + t.toString(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
