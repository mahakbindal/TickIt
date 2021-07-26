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
import com.example.tickit.databinding.FragmentExploreTripsBinding;
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
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;

/**
 * A simple {@link Fragment} subclass.
 */
public class ExploreTripsFragment extends Fragment {

    public static final String TAG = "ExploreTripsFragment";
    public static final int FEATURED_TRIPS_LIMIT = 5;
    public static final int GRID_COUNT = 2;
    public static final int DISTANCE_LIMIT = 500;

    private FragmentExploreTripsBinding mBinding;
    protected List<Trip> mAllTrips;
    private List<String> mAllTripsIds;
    List<Trip> mFeaturedTrips;
    List<Trip> mFilteredTrips;
    List<Trip> mNearbyTrips;
    List<TripDetails> mLocationDetails;
    TripsAdapter mFeaturedAdapter;
    TripsAdapter mExploreAdapter;
    TripsAdapter mNearbyAdapter;
    CurrentLocation mCurrentLocation;

    public ExploreTripsFragment() {
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
        mBinding = FragmentExploreTripsBinding.inflate(inflater, container, false);
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
        mLocationDetails = new ArrayList<>();
        mAllTripsIds = new ArrayList<>();
        queryTrips();
        queryTripDetails();

        mFeaturedAdapter = new TripsAdapter(getContext(), mFeaturedTrips, getActivity());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        mBinding.rvFeatured.setAdapter(mFeaturedAdapter);
        mBinding.rvFeatured.setLayoutManager(linearLayoutManager);

        mExploreAdapter = new TripsAdapter(getContext(), mAllTrips, getActivity());
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), GRID_COUNT);
        mBinding.rvExploreTrips.setAdapter(mExploreAdapter);
        mBinding.rvExploreTrips.setLayoutManager(gridLayoutManager);

        mNearbyAdapter = new TripsAdapter(getContext(), mNearbyTrips, getActivity());
        LinearLayoutManager linearLayoutManagerNearby = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        mBinding.rvNearbyTrips.setAdapter(mNearbyAdapter);
        mBinding.rvNearbyTrips.setLayoutManager(linearLayoutManagerNearby);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_search, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filter(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filter(newText);
                return true;
            }
        });
    }

    private void filter(String query) {
        mFilteredTrips = new ArrayList<>();
        for(Trip trip : mAllTrips) {
            String tripTitle = trip.getTitle().toLowerCase();
            if(tripTitle.contains(query.toLowerCase())) {
                mFilteredTrips.add(trip);
            }
        }
        mExploreAdapter.filterList(mFilteredTrips);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    private void populateFeaturedTrips() {
        for(int i = 0; i < FEATURED_TRIPS_LIMIT; i++) {
            int tripIndex = (int)(Math.random() * mAllTrips.size());
            mFeaturedTrips.add(mAllTrips.remove(tripIndex));
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
                if(exception != null) {
                    Log.e(TAG, "Issue with getting trips", exception);
                    return;
                }

                mAllTrips.addAll(trips);
                for(Trip trip : trips) {
                    mAllTripsIds.add(trip.getObjectId());
                }
                mFeaturedAdapter.notifyDataSetChanged();
                mExploreAdapter.notifyDataSetChanged();
                populateFeaturedTrips();
            }
        });
    }

    private void queryTripDetails() {
        ParseQuery<TripDetails> query = ParseQuery.getQuery(TripDetails.class);
        query.findInBackground(new FindCallback<TripDetails>() {
            @Override
            public void done(List<TripDetails> tripDetails, ParseException exception) {
                if(exception != null) {
                    Log.e(TAG, "Issue with getting trip details", exception);
                    return;
                }
                mLocationDetails.addAll(tripDetails);
                mNearbyAdapter.notifyDataSetChanged();
                mBinding.pbLoading.setVisibility(ProgressBar.VISIBLE);
                mCurrentLocation.setLocationListenerCallback(new CurrentLocation.LocationListenerCallback() {
                    @Override
                    public void locationChanged() {
                        filterTripDetails();
                    }
                });
            }
        });
    }
;
    private void filterTripDetails() {
        List<String> tripIds = new ArrayList<>();
        for(TripDetails tripDetail : mLocationDetails) {
            ParseGeoPoint geoPoint = tripDetail.getLatLng();
            LatLng latLng = new LatLng(geoPoint.getLatitude(), geoPoint.getLongitude());
            double distanceMiles = mCurrentLocation.distance(latLng);
            Trip trip = (Trip) tripDetail.getTrip();
            if (distanceMiles <= DISTANCE_LIMIT && !tripIds.contains(trip.getObjectId()) && mAllTripsIds.contains(trip.getObjectId())) {
                mNearbyTrips.add(trip);
                tripIds.add(trip.getObjectId());
            }
        }
        Log.i(TAG, "Nearby Trips" + mNearbyTrips);
        mBinding.exploreLayout.removeView(mBinding.pbLoading);
        mBinding.pbLoading.setVisibility(ProgressBar.INVISIBLE);
        mNearbyAdapter.notifyDataSetChanged();
    }
}