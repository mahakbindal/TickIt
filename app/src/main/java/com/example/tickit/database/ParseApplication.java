package com.example.tickit.database;

import android.app.Application;

import com.example.tickit.models.SavedTrips;
import com.example.tickit.models.Trip;
import com.example.tickit.models.TripComments;
import com.example.tickit.models.TripDetails;
import com.parse.Parse;
import com.parse.ParseObject;

public class ParseApplication extends Application {

    public static final String APP_ID = "EMq6eQfbMn45qY9a71QbhnCpXCTDT82FEHtAnmZm";
    public static final String CLIENT_KEY = "5c3Ftxz2lOevOPsi1bT5c9X6ORUHPKnoz43NNxaY";
    public static final String SERVER = "https://parseapi.back4app.com";

    @Override
    public void onCreate() {
        super.onCreate();

        // Register Parse models
        ParseObject.registerSubclass(Trip.class);
        ParseObject.registerSubclass(TripDetails.class);
        ParseObject.registerSubclass(SavedTrips.class);
        ParseObject.registerSubclass(TripComments.class);

        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId(APP_ID)
                .clientKey(CLIENT_KEY)
                .server(SERVER)
                .build()
        );
    }
}
