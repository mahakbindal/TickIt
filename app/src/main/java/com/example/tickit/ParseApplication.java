package com.example.tickit;

import android.app.Application;

import com.parse.Parse;

public class ParseApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId("EMq6eQfbMn45qY9a71QbhnCpXCTDT82FEHtAnmZm")
                .clientKey("5c3Ftxz2lOevOPsi1bT5c9X6ORUHPKnoz43NNxaY")
                .server("https://parseapi.back4app.com")
                .build()
        );
    }
}
