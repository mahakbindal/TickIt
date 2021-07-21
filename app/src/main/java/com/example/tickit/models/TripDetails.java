package com.example.tickit.models;

import com.parse.ParseClassName;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;

@ParseClassName("TripDetails")
public class TripDetails extends ParseObject {

    public static final String KEY_TRIP = "trip";
    public static final String KEY_LOCATION = "location";
    public static final String KEY_LOCATION_INDEX = "locationIndex";
    public static final String KEY_DESCRIPTION = "description";
    public static final String KEY_LATLNG = "latLng";

    public ParseObject getTrip() {
        return getParseObject(KEY_TRIP);
    }

    public void setTrip(ParseObject trip) {
        put(KEY_TRIP, trip);
    }

    public String getLocation() {
        return getString(KEY_LOCATION);
    }

    public void setLocation(String location) {
        put(KEY_LOCATION, location);
    }

    public int getLocationIndex() {
        return getInt(KEY_LOCATION_INDEX);
    }

    public void setLocationIndex(int locationIndex) {
        put(KEY_LOCATION_INDEX, locationIndex);
    }

    public String getDescription() {
        return getString(KEY_DESCRIPTION);
    }

    public void setDescription(String description) {
        put(KEY_DESCRIPTION, description);
    }

    public ParseGeoPoint getLatLng() {
        return getParseGeoPoint(KEY_LATLNG);
    }

    public void setLatLng(ParseGeoPoint latLng) {
        put(KEY_LATLNG, latLng);
    }
}
