package com.example.tickit.tripmanager;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.SearchView;

import com.example.tickit.R;
import com.example.tickit.databinding.FragmentFeaturedTripsBinding;
import com.example.tickit.models.SavedTrips;
import com.example.tickit.models.Trip;
import com.example.tickit.models.TripDetails;
import com.google.android.gms.maps.model.LatLng;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;

/**
 * A simple {@link Fragment} subclass.
 */
public class FeaturedTripsFragment extends Fragment {

    public static final String TAG = "FeaturedTripsFragment";
    public static final int FEATURED_TRIPS_LIMIT = 5;
    public static final int DISTANCE_LIMIT = 500;

    private FragmentFeaturedTripsBinding mBinding;
    protected List<Trip> mAllTrips;
    private List<String> mAllTripsIds;
    HashMap<String, Integer> mSavedTripsTracker = new HashMap<>();
    List<Trip> mFeaturedTrips;
    List<Trip> mNearbyTrips;
    List<TripDetails> mTripDetails;
    TripsAdapter mFeaturedAdapter;
    TripsAdapter mNearbyAdapter;
    CurrentLocation mCurrentLocation;

    public FeaturedTripsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
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
        mTripDetails = new ArrayList<>();
        mAllTripsIds = new ArrayList<>();
        queryTrips();
//        queryTripDetails();

        mFeaturedAdapter = new TripsAdapter(getContext(), mFeaturedTrips, getActivity());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        mBinding.rvFeatured.setAdapter(mFeaturedAdapter);
        mBinding.rvFeatured.setLayoutManager(linearLayoutManager);

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
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected(item);
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
                Log.i(TAG, "queryTrips trips: " + trips);
                mAllTrips.addAll(trips);
                for (Trip trip : trips) {
                    mAllTripsIds.add(trip.getObjectId());
                }
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
            public void done(List<Trip> trips, ParseException e) {
                Log.i(TAG, "Trip object ids: " + trips);
                List<String> tripIds = new ArrayList<>();
                for (Trip trip : trips) {
                    tripIds.add(trip.getObjectId());
                }
                Log.i(TAG, "My trip ids: " + tripIds);
                ParseQuery<TripDetails> tripDetailsQuery = ParseQuery.getQuery(TripDetails.class);
                Log.i(TAG, "latitude: " + geoPoint.getLatitude() + " longitude: " + geoPoint.getLongitude());
                tripDetailsQuery.whereWithinMiles(TripDetails.KEY_LATLNG, geoPoint, DISTANCE_LIMIT);
                tripDetailsQuery.whereNotEqualTo(TripDetails.KEY_TRIP, ParseUser.getCurrentUser());
                tripDetailsQuery.whereNotContainedIn(TripDetails.KEY_TRIP, tripIds);
                tripDetailsQuery.findInBackground(new FindCallback<TripDetails>() {
                    @Override
                    public void done(List<TripDetails> tripDetails, ParseException e) {
                        Log.i(TAG, "Trip details filtered: " + tripDetails);
                        for (TripDetails trip : tripDetails) {
                            Log.i(TAG, "Trip details location: " + trip.getLocation() + " obj id: " + trip.getTrip().getObjectId());
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
}