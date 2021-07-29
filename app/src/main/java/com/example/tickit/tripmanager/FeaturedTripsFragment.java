package com.example.tickit.tripmanager;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
import java.util.Collections;
import java.util.List;

import javax.security.auth.callback.Callback;

/**
 * A simple {@link Fragment} subclass.
 */
public class FeaturedTripsFragment extends Fragment {

    public static final String TAG = "FeaturedTripsFragment";
    public static final int FEATURED_TRIPS_LIMIT = 5;
    public static final int SAVED_TRIPS_LIMIT = 5;
    public static final int DISTANCE_LIMIT_DEFAULT = 100;
    public static final String[] MILES = new String[]{"100", "500", "1000", "5000", "10000"};
    public static final String[] ORDER = new String[]{"Near to Far", "Far to Near"};

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
    int mMiles = DISTANCE_LIMIT_DEFAULT;
    boolean mReverse = false;

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

        ArrayAdapter<String> milesAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, MILES);
        mBinding.milesDropdown.setAdapter(milesAdapter);
        ArrayAdapter<String> sortAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, ORDER);
        mBinding.orderDropdown.setAdapter(sortAdapter);

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
                queryNearbyTrips(DISTANCE_LIMIT_DEFAULT);
                onFilterClick();
                onSortClicked();
            }
        });

        // Set Saved trips recycler view
        mSavedAdapter = new TripsAdapter(getContext(), mSavedTrips, getActivity());
        mSavedAdapter.notifyDataSetChanged();
        LinearLayoutManager linearLayoutManagerSaved = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        mBinding.rvMostSaved.setAdapter(mSavedAdapter);
        mBinding.rvMostSaved.setLayoutManager(linearLayoutManagerSaved);
    }

    private void onFilterClick() {
        mBinding.milesDropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mMiles = Integer.parseInt(mBinding.milesDropdown.getSelectedItem().toString());
                queryNearbyTrips(mMiles);
                mNearbyAdapter.clear();
                mNearbyAdapter.notifyDataSetChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void onSortClicked() {
        mBinding.orderDropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position == 0) {
                    mReverse = false;
                }
                else if(position == 1) {
                    mReverse = true;
                }
                queryNearbyTrips(mMiles);
                mNearbyAdapter.clear();
                mNearbyAdapter.notifyDataSetChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void populateFeaturedTrips() {
        List<Integer> indexList = new ArrayList<>();
        for (int i = 0; i < FEATURED_TRIPS_LIMIT; i++) {
            int tripIndex = (int) (Math.random() * mAllTrips.size());
            if(!indexList.contains(tripIndex)) {
                mFeaturedTrips.add(mAllTrips.get(tripIndex));
                indexList.add(tripIndex);
            }
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

    private void queryNearbyTrips(int miles) {
        mTripDetails.clear();
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
                tripDetailsQuery.whereWithinMiles(TripDetails.KEY_LATLNG, geoPoint, miles);
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
        mNearbyTrips.clear();
        List<String> tripObjectIds = new ArrayList<>();
        for(TripDetails tripDetail : mTripDetails) {
            String tripId = tripDetail.getTrip().getObjectId();
            if(!tripObjectIds.contains(tripId)) {
                tripObjectIds.add(tripId);
                mNearbyTrips.add((Trip)tripDetail.getTrip());
            }
        }
        if(mReverse) {
            Collections.reverse(mNearbyTrips);
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