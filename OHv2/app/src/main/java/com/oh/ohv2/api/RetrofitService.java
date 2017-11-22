package com.oh.ohv2.api;

import com.oh.ohv2.database.GeofenceLog;
import com.oh.ohv2.helpers.Constants;

import java.util.Date;

import retrofit2.Call;
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

    @FormUrlEncoded
    @POST(Constants.POST_LOGS_API)
    Call<Void> sendLog(@Field("geof_id") int geofId, @Field("status") String status, @Field("timestamp") Date timestamp);

}
