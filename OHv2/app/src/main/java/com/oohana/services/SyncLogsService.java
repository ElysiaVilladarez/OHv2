package com.oohana.services;

import android.content.SharedPreferences;
import android.util.Log;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.oohana.api.LogBody;
import com.oohana.api.RetrofitService;
import com.oohana.api.SyncServerResult;
import com.oohana.database.TriggeredLogs;
import com.oohana.helpers.Constants;
import com.oohana.helpers.SyncLogsToServer;
import com.oohana.home.HomeContract;
import com.oohana.home.HomeModel;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by SaperiumDev on 11/21/2017.
 */

public class SyncLogsService extends JobService {
    @Override
    public boolean onStartJob(JobParameters job) {
        Log.d(Constants.LOG_TAG_JOB, "Sync Logs Job Activated!");
        final HomeContract.HomeModelToPresenter homeModel = new HomeModel();
        if (job.getTag().equals(Constants.SYNC_LOGS_TAG) && homeModel.getLogCount() > 0) {
            if(Constants.isInternetAvailable(getApplicationContext())){
                Log.d(Constants.LOG_TAG_JOB, "Posting logs to server . . .");
                Retrofit retrofitApi = new Retrofit.Builder()
                        .baseUrl(Constants.BASE_URL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();

                RetrofitService getService = retrofitApi.create(RetrofitService.class);
                SyncLogsToServer.syncLogs(getApplicationContext(), getService, homeModel, false);
            }else{
                Log.d(Constants.LOG_TAG_JOB, "No internet connection. Attempt to sync is true!");
                SharedPreferences prefs = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE);
                prefs.edit().putBoolean(Constants.SHARED_PREF_HAS_SYNCED_BEFORE_KEY, true).apply();
            }

        }else if(job.getTag().equals(Constants.FETCH_LOGS_TAG)){
            if(Constants.isInternetAvailable(getApplicationContext())){
                Log.d(Constants.LOG_TAG_JOB, "Fetching geofences . . .");
                Retrofit retrofitApi = new Retrofit.Builder()
                        .baseUrl(Constants.BASE_URL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();

                RetrofitService getService = retrofitApi.create(RetrofitService.class);
                SyncLogsToServer.fetchGeofencesFromServer(getApplicationContext(), getService, homeModel, false);
            }else{
                Log.d(Constants.LOG_TAG_JOB, "No internet connection. Attempt to sync is true!");
                SharedPreferences prefs = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE);
                prefs.edit().putBoolean(Constants.SHARED_PREF_HAS_FETCHED_BEFORE_KEY, true).apply();
            }
        }
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters job) {

        return false;
    }
}
