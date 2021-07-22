package com.example.tickit.models;

import com.parse.Parse;
import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

@ParseClassName("SavedTrips")
public class SavedTrips extends ParseObject {

    public static final String KEY_USER = "user";
    public static final String KEY_TRIP = "trip";

    public ParseUser getUser() {
        return getParseUser(KEY_USER);
    }

    public void setUser(ParseUser user) {
        put(KEY_USER, user);
    }

    public ParseObject getTrip() {
        return getParseObject(KEY_TRIP);
    }

    public void setTrip(ParseObject trip) {
        put(KEY_TRIP, trip);
    }
}
