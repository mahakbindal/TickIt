package com.example.tickit.tripmanager;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.example.tickit.databinding.FragmentFeaturedTripsBinding;
import com.example.tickit.models.Trip;
import com.example.tickit.models.TripDetails;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

import javax.security.auth.callback.Callback;

/**
 * A simple {@link Fragment} subclass.
 */
public class FeaturedTripsFragment extends Fragment {

    public static final String TAG = "FeaturedTripsFragment";
    public static final int FEATURED_TRIPS_LIMIT = 5;
    public static final int SAVED_TRIPS_LIMIT = 5;
    public static final int DISTANCE_LIMIT = 500;

    private FragmentFeaturedTripsBinding mBinding;
    protected List<Trip> mAllTrips;
    List<Trip> mSavedTrips;
    List<Trip> mFeaturedTrips;
    List<Trip> mNearbyTrips;
    List<TripDetails> mTripDetails;
    TripsAdapter mFeaturedAdapter;
    TripsAdapter mNearbyAdapter;
    TripsAdapter mSavedAdapter;
    CurrentLocation mCurrentLocation;

    public FeaturedTripsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mBinding = FragmentFeaturedTripsBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);
        mCurrentLocation = new CurrentLocation(getContext());
        mCurrentLocation.getLocation();
        mAllTrips = new ArrayList<>();
        mFeaturedTrips = new ArrayList<>();
        mNearbyTrips = new ArrayList<>();
        mSavedTrips = new ArrayList<>();
        mTripDetails = new ArrayList<>();
        queryTrips();
        querySavedTrips();

        // Set Featured trips recycler view
        mFeaturedAdapter = new TripsAdapter(getContext(), mFeaturedTrips, getActivity());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        mBinding.rvFeatured.setAdapter(mFeaturedAdapter);
        mBinding.rvFeatured.setLayoutManager(linearLayoutManager);

        // Set Nearby trips recycler view
        mNearbyAdapter = new TripsAdapter(getContext(), mNearbyTrips, getActivity());
        LinearLayoutManager linearLayoutManagerNearby = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        mBinding.rvNearbyTrips.setAdapter(mNearbyAdapter);
        mBinding.rvNearbyTrips.setLayoutManager(linearLayoutManagerNearby);

        mBinding.pbLoading.setVisibility(ProgressBar.VISIBLE);
        mCurrentLocation.setLocationListenerCallback(new CurrentLocation.LocationListenerCallback() {
            @Override
            public void locationChanged() {
                queryNearbyTrips();
            }
        });

        // Set Saved trips recycler view
        mSavedAdapter = new TripsAdapter(getContext(), mSavedTrips, getActivity());
        mSavedAdapter.notifyDataSetChanged();
        LinearLayoutManager linearLayoutManagerSaved = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        mBinding.rvMostSaved.setAdapter(mSavedAdapter);
        mBinding.rvMostSaved.setLayoutManager(linearLayoutManagerSaved);
    }

    private void populateFeaturedTrips() {
        for (int i = 0; i < FEATURED_TRIPS_LIMIT; i++) {
            int tripIndex = (int) (Math.random() * mAllTrips.size());
            mFeaturedTrips.add(mAllTrips.get(tripIndex));
        }
    }

    private void queryTrips() {
        ParseQuery<Trip> query = ParseQuery.getQuery(Trip.class);
        query.include(Trip.KEY_USER);
        query.whereNotEqualTo(Trip.KEY_USER, ParseUser.getCurrentUser());
        query.addDescendingOrder(Trip.KEY_CREATED_AT);

        query.findInBackground(new FindCallback<Trip>() {
            @Override
            public void done(List<Trip> trips, ParseException exception) {
                if (exception != null) {
                    Log.e(TAG, "Issue with getting trips", exception);
                    return;
                }
                mAllTrips.addAll(trips);
                mFeaturedAdapter.notifyDataSetChanged();
                populateFeaturedTrips();
            }
        });
    }

    private void queryNearbyTrips() {
        ParseQuery<Trip> tripQuery = ParseQuery.getQuery(Trip.class);
        tripQuery.include(Trip.KEY_USER);
        tripQuery.whereEqualTo(Trip.KEY_USER, ParseUser.getCurrentUser());
        ParseGeoPoint geoPoint = mCurrentLocation.getCurrentLocation();
        tripQuery.findInBackground(new FindCallback<Trip>() {
            @Override
            public void done(List<Trip> trips, ParseException exception) {
                if(exception != null) {
                    Log.e(TAG, "Issue with getting trips", exception);
                }
                List<String> tripIds = new ArrayList<>();
                for (Trip trip : trips) {
                    tripIds.add(trip.getObjectId());
                }
                ParseQuery<TripDetails> tripDetailsQuery = ParseQuery.getQuery(TripDetails.class);
                Log.i(TAG, "latitude: " + geoPoint.getLatitude() + " longitude: " + geoPoint.getLongitude());
                tripDetailsQuery.whereWithinMiles(TripDetails.KEY_LATLNG, geoPoint, DISTANCE_LIMIT);
                tripDetailsQuery.whereNotEqualTo(TripDetails.KEY_TRIP, ParseUser.getCurrentUser());
                tripDetailsQuery.whereNotContainedIn(TripDetails.KEY_TRIP, tripIds);
                tripDetailsQuery.findInBackground(new FindCallback<TripDetails>() {
                    @Override
                    public void done(List<TripDetails> tripDetails, ParseException exception) {
                        if(exception != null) {
                            Log.e(TAG, "Issue with getting trip details", exception);
                        }
                        mTripDetails.addAll(tripDetails);
                        filterTripDetails();
                    }
                });
            }
        });
    }

    private void filterTripDetails() {
        List<String> tripObjectIds = new ArrayList<>();
        for(TripDetails tripDetail : mTripDetails) {
            String tripId = tripDetail.getTrip().getObjectId();
            if(!tripObjectIds.contains(tripId)) {
                tripObjectIds.add(tripId);
                mNearbyTrips.add((Trip)tripDetail.getTrip());
            }
        }
        mNearbyAdapter.notifyDataSetChanged();
        mBinding.exploreLayout.removeView(mBinding.pbLoading);
        mBinding.pbLoading.setVisibility(ProgressBar.INVISIBLE);
    }

    private void querySavedTrips() {
        ParseQuery<Trip> query = new ParseQuery<>(Trip.class);
        query.include(Trip.KEY_SAVE_COUNT);
        query.whereNotEqualTo(Trip.KEY_SAVE_COUNT, 0);
        query.orderByDescending(Trip.KEY_SAVE_COUNT);
        query.setLimit(SAVED_TRIPS_LIMIT);
        query.findInBackground(new FindCallback<Trip>() {
            @Override
            public void done(List<Trip> savedTrips, ParseException e) {
                Log.i(TAG, "Saved trips: " + savedTrips);
                mSavedTrips.addAll(savedTrips);
                mSavedAdapter.notifyDataSetChanged();
            }
        });
    }
}