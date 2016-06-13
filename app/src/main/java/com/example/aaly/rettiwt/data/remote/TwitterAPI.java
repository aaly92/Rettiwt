package com.example.aaly.rettiwt.data.remote;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public interface TwitterAPI {

    String API_URL = "https://hooks.slack.com/services/T026B13VA/B1F7H2L9Y/";


    class Factory {
        private static TwitterAPI service;

        public static TwitterAPI getInstance() {
            if (service == null) {
                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(API_URL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();

                service = retrofit.create(TwitterAPI.class);
            }
            return service;
        }
    }
}
