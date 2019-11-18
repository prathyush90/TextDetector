package com.example.noob.textdetector;

import com.example.noob.textdetector.retrofit.ApiService;
import com.example.noob.textdetector.retrofit.RetrofitClient;

/**
 * Created by prathyush on 18/10/18.
 */

public class ApiUtils {

    private ApiUtils() {}

    public static final String BASE_URL = "http://development.wisefly.in/";

    public static ApiService getAPIService() {

        return RetrofitClient.getClient(BASE_URL).create(ApiService.class);
    }
}
