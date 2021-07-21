package com.example.tickit.tripmanager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.tickit.R;
import com.example.tickit.databinding.ActivityTripDetailsBinding;
import com.example.tickit.models.Trip;
import com.example.tickit.models.TripDetails;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.GeoApiContext;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

public class TripDetailsActivity extends AppCompatActivity {

    public static final String TAG = "TripDetailsActivity";
    public static final String TRIP = "trip";
    public static final String TRIP_CLASS = "Trip";

    public ActivityTripDetailsBinding mBinding;
    private Context mContext;
    GoogleMap mGoogleMap;
    private GeoApiContext mGeoApiContext = null;

    public Trip mTrip;
    private List<TripDetails> mTripDetails;
    private List<LocationDetailsView> mLocationDetails;
    private List<LatLng> mLatLngList;
    private List<String> mMarkerTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityTripDetailsBinding.inflate(getLayoutInflater());
        View view = mBinding.getRoot();
        setContentView(view);

        mTripDetails = new ArrayList<>();
        mLocationDetails = new ArrayList<>();
        mLatLngList = new ArrayList<>();

        Intent intent = getIntent();
        mTrip = Parcels.unwrap(intent.getParcelableExtra(TRIP));

        // Initialize map fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapDetails);

        // Async map
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull GoogleMap googleMap) {
                mGoogleMap = googleMap;
                UiSettings uiSettings = mGoogleMap.getUiSettings();
                uiSettings.setZoomControlsEnabled(true);
            }
        });

        // Initialize Google Maps Directions API to calculate routes
        if(mGeoApiContext == null) {
            mGeoApiContext = new GeoApiContext.Builder().apiKey(getString(R.string.google_maps_api_key)).build();
        }

        mBinding.tvTripTitle.setText(mTrip.getTitle());

        queryTripDetails(mTrip);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.left_in, R.anim.right_out);
    }

    private void initTripDetails() {
        addLocationDetailsView();
        populateLatLngList();
        MapRoute mapRoute = new MapRoute(mContext, mGoogleMap, mGeoApiContext, mLatLngList);
        mapRoute.calculateDirections(mMarkerTitle);
    }

    private void addLocationDetailsView() {
        mMarkerTitle = new ArrayList<>();
        for(int i = 0; i < mTripDetails.size(); i++) {
            LocationDetailsView newLocation = new LocationDetailsView(this);
            mLocationDetails.add(newLocation);
            mBinding.locationList.addView(newLocation);
            String location = mTripDetails.get(i).getLocation();
            newLocation.setTextValue(location);
            mMarkerTitle.add(location);
        }
    }

    private void populateLatLngList() {
        for(TripDetails details : mTripDetails) {
            ParseGeoPoint geoPoint = details.getLatLng();
            LatLng latLng = new LatLng(geoPoint.getLatitude(), geoPoint.getLongitude());
            mLatLngList.add(latLng);
        }
    }

    private void queryTripDetails(Trip trip) {
        ParseQuery<TripDetails> query = ParseQuery.getQuery(TripDetails.class);
        query.include(TripDetails.KEY_TRIP);
        ParseObject tripObjId = ParseObject.createWithoutData(TRIP_CLASS, trip.getObjectId());
        query.whereEqualTo(TripDetails.KEY_TRIP, tripObjId);
        query.addAscendingOrder(TripDetails.KEY_LOCATION_INDEX);

        query.findInBackground(new FindCallback<TripDetails>() {
            @Override
            public void done(List<TripDetails> tripDetails, ParseException e) {
                Log.i(TAG, "trip details: " + tripDetails);
                if(e != null) {
                    Log.e(TAG, "Issue with getting trip details", e);
                    return;
                }
                mTripDetails.addAll(tripDetails);
                Log.i(TAG, "trip details: " + mTripDetails);
                initTripDetails();
            }
        });
    }
}