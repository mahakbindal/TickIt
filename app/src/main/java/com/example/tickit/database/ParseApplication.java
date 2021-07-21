package com.example.tickit.database;

import android.app.Application;

import com.example.tickit.models.Trip;
import com.example.tickit.models.TripDetails;
import com.parse.Parse;
import com.parse.ParseObject;

public class ParseApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Register Parse models
        ParseObject.registerSubclass(Trip.class);
        ParseObject.registerSubclass(TripDetails.class);

        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId("EMq6eQfbMn45qY9a71QbhnCpXCTDT82FEHtAnmZm")
                .clientKey("5c3Ftxz2lOevOPsi1bT5c9X6ORUHPKnoz43NNxaY")
                .server("https://parseapi.back4app.com")
                .build()
        );
    }
}
