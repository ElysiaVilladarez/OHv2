package com.oohana.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.oohana.api.LogBody;
import com.oohana.api.RetrofitService;
import com.oohana.api.SyncServerResult;
import com.oohana.database.TriggeredLogs;
import com.oohana.home.HomeContract;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by SaperiumDev on 11/26/2017.
 */

public class SyncLogsToServer {

    public static void syncLogs(final Context c, RetrofitService getService, final HomeContract.HomeModelToPresenter homeModel){
        Call<SyncServerResult> postLogs;
        final ArrayList<TriggeredLogs> logs = homeModel.getLogs();
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
                    }
                }

                @Override
                public void onFailure(Call<SyncServerResult> call, Throwable t) {
                    Log.d(Constants.LOG_TAG_HOME, "Posting logs failed: " + t.toString());

                    SharedPreferences prefs = c.getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE);
                    prefs.edit().putBoolean(Constants.SHARED_PREF_HAS_SYNCED_BEFORE_KEY, true).apply();
                }
            });
        }
    }
}
