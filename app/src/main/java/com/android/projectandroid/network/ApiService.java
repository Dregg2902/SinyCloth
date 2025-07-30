package com.android.projectandroid.network;

import com.android.projectandroid.shipper.LocationResponse;

import retrofit2.Call;
import retrofit2.http.GET;

public interface ApiService {
    @GET("locations")
    Call<LocationResponse> getLocations();
}