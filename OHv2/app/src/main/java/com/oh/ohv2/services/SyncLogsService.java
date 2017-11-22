package com.oh.ohv2.services;

import android.util.Log;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.oh.ohv2.api.RetrofitService;
import com.oh.ohv2.database.TriggeredLogs;
import com.oh.ohv2.helpers.Constants;

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
        if(job.getTag().equals(Constants.SYNC_LOGS_TAG)) {
            Log.d(Constants.LOG_TAG_JOB, "Posting logs to server . . .");
//        final HomeContract.HomeModelToPresenter homeModel = new HomeModel();
//        Retrofit retrofitApi = new Retrofit.Builder()
//                    .baseUrl(Constants.BASE_URL)
//                    .addConverterFactory(GsonConverterFactory.create())
//                    .build();
//
//            RetrofitService getService = retrofitApi.create(RetrofitService.class);
//            Call<Void> postLogs;
//            final ArrayList<TriggeredLogs> logs = homeModel.getLogs();
//            for(final TriggeredLogs l: logs) {
//                postLogs = getService.sendLog(l.getTriggeredGeofenceId(), l.getStatus(), l.getTimeStamp());
//                postLogs.enqueue(new Callback<Void>() {
//                    @Override
//                    public void onResponse(Call<Void> call, Response<Void> response) {
//                        Log.d(Constants.LOG_TAG_RECEIVER, "Posting logs successful: " + response.isSuccessful());
//                        if(l == logs.get(logs.size() - 1)) homeModel.deleteLogs();
//                    }
//
//                    @Override
//                    public void onFailure(Call<Void> call, Throwable t) {
//                        Log.d(Constants.LOG_TAG_HOME, "Posting logs failed: " + t.toString());
//
//                    }
//                });
//            }
        }
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters job) {

        return false;
    }
}
