package com.example.tickit.tripmanager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.tickit.R;
import com.example.tickit.databinding.ActivityTripDetailsBinding;
import com.example.tickit.models.Trip;
import com.example.tickit.models.TripComments;
import com.example.tickit.models.TripDetails;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.maps.GeoApiContext;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.jetbrains.annotations.NotNull;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TripDetailsActivity extends AppCompatActivity {

    public static final String TAG = "TripDetailsActivity";
    public static final String TRIP_EXTRA = "trip";
    public static final String TRIP_CLASS = "Trip";
    public static final String SHARE_TYPE = "text/plain";

    public ActivityTripDetailsBinding mBinding;
    GoogleMap mGoogleMap;
    private GeoApiContext mGeoApiContext = null;
    private GoogleMapRouteHelper mRouteHelper;
    public Trip mTrip;
    private List<TripComments> mAllComments;
    private TripCommentsAdapter mCommentsAdapter;
    private List<TripDetails> mTripDetails;
    private List<LocationDetailsView> mLocationDetails;
    private Map<String, String> mMarkerTitleDescription = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityTripDetailsBinding.inflate(getLayoutInflater());
        View view = mBinding.getRoot();
        setContentView(view);

        mTripDetails = new ArrayList<>();
        mLocationDetails = new ArrayList<>();
        mAllComments = new ArrayList<>();

        Intent intent = getIntent();
        mTrip = Parcels.unwrap(intent.getParcelableExtra(TRIP_EXTRA));

        // Initialize map fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapDetails);

        // Initialize Google Maps Directions API to calculate routes
        if(mGeoApiContext == null) {
            mGeoApiContext = new GeoApiContext.Builder().apiKey(getString(R.string.google_maps_api_key)).build();
        }

        // Async map
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull GoogleMap googleMap) {
                if(mGoogleMap == null) {
                    mGoogleMap = googleMap;
                }
                UiSettings uiSettings = mGoogleMap.getUiSettings();
                uiSettings.setZoomControlsEnabled(true);
                mRouteHelper = new GoogleMapRouteHelper(TripDetailsActivity.this, mGoogleMap, mGeoApiContext);
                queryTripAndInitializeUi(mTrip);
            }
        });

        mBinding.tvTripTitle.setText(mTrip.getTitle());

        mCommentsAdapter = new TripCommentsAdapter(this, mAllComments);
        mBinding.rvComments.setAdapter(mCommentsAdapter);
        mBinding.rvComments.setLayoutManager(new LinearLayoutManager(this));

        queryTripComments(mTrip);

        sendCommentClickListener();

    }

    private void sendCommentClickListener() {
        mBinding.btnSendComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String comment = mBinding.etComment.getText().toString();
                if(comment.isEmpty()) {
                    Toast.makeText(TripDetailsActivity.this, "Your comment is empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                saveTripComment(comment);
            }
        });
    }

    private void queryTripComments(Trip trip) {
        ParseQuery<TripComments> query = ParseQuery.getQuery(TripComments.class);
//        query.whereEqualTo(Trip.KEY_OBJECT_ID, mTrip.getObjectId());
        ParseObject tripObjId = ParseObject.createWithoutData(TRIP_CLASS, trip.getObjectId());
        query.whereEqualTo(TripComments.KEY_TRIP, tripObjId);
        Log.i(TAG, trip.getObjectId());
        query.addAscendingOrder(TripComments.KEY_CREATED_AT);
        query.findInBackground(new FindCallback<TripComments>() {
            @Override
            public void done(List<TripComments> tripComments, ParseException exception) {
                if(exception != null) {
                    Log.e(TAG, "Issue with getting trip comments", exception);
                    return;
                }
                mAllComments.addAll(tripComments);
                mCommentsAdapter.notifyDataSetChanged();
            }
        });
    }

    private void saveTripComment(String comment) {
        TripComments tripComment = new TripComments();
        tripComment.setTrip(mTrip);
        tripComment.setUser(ParseUser.getCurrentUser());
        tripComment.setComment(comment);

        tripComment.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException exception) {
                if(exception != null) {
                    Log.e(TAG, "Issue saving trip comment", exception);
                }
                mAllComments.add(tripComment);
                mBinding.etComment.setText("");
                mCommentsAdapter.notifyDataSetChanged();
            }
        });
    }

    public static Intent newIntent(Context context, Trip trip) {
        Intent intent = new Intent(context, TripDetailsActivity.class);
        intent.putExtra(TRIP_EXTRA, Parcels.wrap(trip));
        return intent;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.left_in, R.anim.right_out);
    }

    private void queryTripAndInitializeUi(Trip trip) {
        ParseQuery<TripDetails> query = ParseQuery.getQuery(TripDetails.class);
        query.include(TripDetails.KEY_TRIP);
        ParseObject tripObjId = ParseObject.createWithoutData(TRIP_CLASS, trip.getObjectId());
        query.whereEqualTo(TripDetails.KEY_TRIP, tripObjId);
        query.addAscendingOrder(TripDetails.KEY_LOCATION_INDEX);

        query.findInBackground(new FindCallback<TripDetails>() {
            @Override
            public void done(List<TripDetails> tripDetails, ParseException exception) {
                Log.i(TAG, "trip details: " + tripDetails);
                if(exception != null) {
                    Log.e(TAG, "Issue with getting trip details", exception);
                    return;
                }
                setTripDetails(tripDetails);
            }
        });
    }

    private void displayMarkerDetails() {
        Log.i(TAG, "Info window map: " + mMarkerTitleDescription);
        mGoogleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(@NonNull Marker marker) {
                String description = mMarkerTitleDescription.get(marker.getTitle());
                if(description != null && !description.equals("")) {
                    marker.setSnippet(description);
//                    marker.showInfoWindow();
                }
                marker.setTitle(marker.getTitle());
                marker.showInfoWindow();
                return true;
            }
        });
    }

    private void setTripDetails(List<TripDetails> tripDetails) {
        mTripDetails = tripDetails;
        List<String> waypointNameList = updateWaypointList();
        List<LatLng> waypointLatLngList = getLatLngListFromTripDetails();
        mRouteHelper.showRoute(waypointLatLngList, waypointNameList);
        mRouteHelper.setDurationCallback(new GoogleMapRouteHelper.DurationCallback() {
            @Override
            public void getDurationCallback() {
                String duration = mRouteHelper.getDuration();
                mBinding.tvDuration.setText(duration);
                String distance = mRouteHelper.getDistance() + " miles";
                mBinding.tvDistance.setText(distance);
            }
        });
    }

    private List<LatLng> getLatLngListFromTripDetails() {
        List<LatLng> waypointLatLngList = new ArrayList<>();
        for(TripDetails details : mTripDetails) {
            ParseGeoPoint geoPoint = details.getLatLng();
            LatLng latLng = new LatLng(geoPoint.getLatitude(), geoPoint.getLongitude());
            waypointLatLngList.add(latLng);
            mMarkerTitleDescription.put(details.getLocation(), details.getDescription());
        }
        displayMarkerDetails();
        return waypointLatLngList;
    }

    private List<String> updateWaypointList() {
        List<String> waypointNameList = new ArrayList<>();
        for(int i = 0; i < mTripDetails.size(); i++) {
            LocationDetailsView newLocation = new LocationDetailsView(this);
            mLocationDetails.add(newLocation);
            mBinding.locationList.addView(newLocation);
            String location = mTripDetails.get(i).getLocation();
            newLocation.setTextValue(i+1 + ". " + location);
            waypointNameList.add(location);
        }
        return waypointNameList;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem item = menu.findItem(R.id.action_share);
        item.setVisible(true);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_share, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.action_share) {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, "Sharing my trip: " + mBinding.tvTripTitle.getText().toString());
            sendIntent.setType(SHARE_TYPE);

            Intent shareIntent = Intent.createChooser(sendIntent, null);
            startActivity(shareIntent);
            return true;

        }
        return super.onOptionsItemSelected(item);
    }
}