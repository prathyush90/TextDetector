package com.example.noob.textdetector.retrofit;
import com.example.noob.textdetector.models.ResponseClass;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface ApiService {

    @POST("signboardroute/getsignboards")
    @Headers({ "Content-Type: application/json;charset=UTF-8"})
    Call<ResponseClass> sendPost(@Body JsonObject body);
}