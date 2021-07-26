package com.example.tickit.tripmanager;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.tickit.main.MainActivity;
import com.example.tickit.models.Trip;
import com.example.tickit.models.TripDetails;
import com.google.android.gms.maps.model.LatLng;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;

public class CurrentLocation {

    private static final String TAG = "CurrentLocation";
    private LocationManager mLocationManager;
    private LocationListener mLocationListener;
    private Context mContext;
    private boolean mGpsEnabled = false;
    private boolean mNetworkEnabled = false;
    public List<Trip> mNearbyTrips;
    private double mLatitude;
    private double mLongitude;
    private LocationListenerCallback mLocationListenerCallback;

    public CurrentLocation(Context context) {
        mLocationListener = new MyLocationListener();
        this.mContext = context;
        mLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        mNearbyTrips = new ArrayList<>();

    }

    public void getLocation() {
        enableProvider();
        checkLocationPermission();
    }

    private void enableProvider() {
        try {
            mGpsEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception exception) {

        }

        if(!mGpsEnabled && !mNetworkEnabled) {
            Toast.makeText(mContext, "Location services not enabled", Toast.LENGTH_SHORT).show();
        }

        if (mGpsEnabled) {
            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, mLocationListener);
        }

        if(mNetworkEnabled) {
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, mLocationListener);
        }
    }

    private boolean checkLocationPermission() {
        int fineLocation = ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION);
        int coarseLocation = ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION);
        List<String> listPermission = new ArrayList<>();
        if(fineLocation != PackageManager.PERMISSION_GRANTED) {
            listPermission.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if(coarseLocation != PackageManager.PERMISSION_GRANTED) {
            listPermission.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }
        if(!listPermission.isEmpty()) {
            ActivityCompat.requestPermissions((Activity) mContext, listPermission.toArray(new String[listPermission.size()]), 1);
        }
        return true;
    }

    public double distance(LatLng coordinates)
    {
        double lon1 = Math.toRadians(mLongitude);
        double lon2 = Math.toRadians(coordinates.longitude);
        double lat1 = Math.toRadians(mLatitude);
        double lat2 = Math.toRadians(coordinates.latitude);
        // Haversine formula
        double dlon = lon2 - lon1;
        double dlat = lat2 - lat1;
        double a = Math.pow(Math.sin(dlat / 2), 2)
                + Math.cos(lat1) * Math.cos(lat2)
                * Math.pow(Math.sin(dlon / 2),2);

        double c = 2 * Math.asin(Math.sqrt(a));
        // Radius of earth in kilometers. Use 3956
        // for miles, 6731 kilometers
        double r = 3956;
        return(c * r);
    }

    class MyLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(@NonNull Location location) {
            if(location != null) {
                Log.d(TAG, "onLocationChanged has been called!");
                mLocationManager.removeUpdates(mLocationListener);
                mLatitude = location.getLatitude();
                mLongitude = location.getLongitude();
                mLocationListenerCallback.locationChanged();
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(@NonNull String provider) {

        }

        @Override
        public void onProviderDisabled(@NonNull String provider) {

        }
    }

    public interface LocationListenerCallback {
        void locationChanged();
    }

    public void setLocationListenerCallback(LocationListenerCallback locationListenerCallback) {
        this.mLocationListenerCallback = locationListenerCallback;
    }
}
