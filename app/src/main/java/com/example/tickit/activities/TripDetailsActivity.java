package com.example.tickit.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.tickit.LocationDetailsView;
import com.example.tickit.R;
import com.example.tickit.Trip;
import com.example.tickit.TripDetails;
import com.example.tickit.databinding.ActivityTripDetailsBinding;
import com.example.tickit.fragments.CreateFragment;
import com.parse.FindCallback;
import com.parse.ParseException;
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
    public Trip mTrip;
    private List<TripDetails> mTripDetails;
    private List<LocationDetailsView> mLocationDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityTripDetailsBinding.inflate(getLayoutInflater());
        View view = mBinding.getRoot();
        setContentView(view);

        mTripDetails = new ArrayList<>();
        mLocationDetails = new ArrayList<>();

        Intent intent = getIntent();
        mTrip = Parcels.unwrap(intent.getParcelableExtra(TRIP));

        mBinding.tvTripTitle.setText(mTrip.getTitle());

        queryTripDetails(mTrip);

        addLocationDetailsView();

    }

    private void addLocationDetailsView() {
        Log.i(TAG, "Trip details list: " + mTripDetails);
        for(int i = 0; i < mTripDetails.size(); i++) {
            LocationDetailsView newLocation = new LocationDetailsView(this);
            mLocationDetails.add(newLocation);
            mBinding.locationList.addView(newLocation);
            newLocation.setTextValue(mTripDetails.get(i).getLocation());
        }
    }

    private void queryTripDetails(Trip trip) {
        ParseQuery<TripDetails> query = ParseQuery.getQuery(TripDetails.class);
        query.include(TripDetails.KEY_TRIP);
        ParseObject tripObjId = ParseObject.createWithoutData(TRIP_CLASS, trip.getObjectId());
        query.whereEqualTo(TripDetails.KEY_TRIP, tripObjId);
        Log.i(TAG, "Trip object id: " + trip.getObjectId());
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
                Log.i(TAG, "List" + mTripDetails.toString());
                addLocationDetailsView();
            }
        });
    }
}