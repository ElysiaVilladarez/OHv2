package com.oohana.api;

import com.oohana.database.GeofenceLog;
import com.oohana.helpers.Constants;

import java.util.Date;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;

/**
 * Created by elysi on 10/29/2017.
 */

public interface RetrofitService {
    @GET(Constants.GET_GEOFENCE_API)
    Call<GeofenceList> geofenceList();

    @POST(Constants.POST_LOGS_API)
    Call<SyncServerResult> sendLog(@Body LogBody log);


}
