package com.oohana.helpers;

import android.content.Context;
import android.util.Log;

import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.RetryStrategy;
import com.firebase.jobdispatcher.Trigger;
import com.oohana.services.SyncLogsService;

/**
 * Created by SaperiumDev on 11/21/2017.
 */

public class FirebaseDispatchers {
    private FirebaseJobDispatcher dispatcher;
    public FirebaseDispatchers(Context c){
        dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(c));
    }

    public void scheduleJob(String tag, boolean isRecurring, int executionWindow, boolean isReplacingCurrent){
        Job myJob;
        if(isRecurring) {
            myJob = dispatcher.newJobBuilder()
                    .setService(SyncLogsService.class)
                    .setTag(tag)
                    .setRecurring(isRecurring)
                    .setLifetime(Lifetime.UNTIL_NEXT_BOOT)
                    .setTrigger(Trigger.executionWindow(executionWindow, executionWindow))
                    .setReplaceCurrent(isReplacingCurrent)
                    .setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL)
                    .build();
        } else{
            myJob = dispatcher.newJobBuilder()
                    .setService(SyncLogsService.class)
                    .setTag(tag)
                    .setRecurring(isRecurring)
                    .setLifetime(Lifetime.UNTIL_NEXT_BOOT)
                    .setReplaceCurrent(isReplacingCurrent)
                    .setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL)
                    .build();
        }

        dispatcher.mustSchedule(myJob);
        Log.d(Constants.LOG_TAG_JOB, "Dispatching job: " + tag);
    }

    public void cancelJob(String tag){
        dispatcher.cancel(tag);
    }
    public void cancelAllJobs(){dispatcher.cancelAll();}
}
