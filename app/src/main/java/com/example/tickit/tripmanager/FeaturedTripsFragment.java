package com.example.tickit.tripmanager;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.tickit.R;
import com.example.tickit.databinding.FragmentFeaturedTripsBinding;
import com.example.tickit.models.SavedTrips;
import com.example.tickit.models.Trip;
import com.example.tickit.models.TripDetails;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.security.auth.callback.Callback;

import static android.app.Activity.RESULT_OK;

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
    private static final int AUTOCOMPLETE_CODE = 100;
    public static final int MAX_ADDRESS = 1;
    private static final int FLAG = 0;

    private FragmentFeaturedTripsBinding mBinding;
    protected List<Trip> mAllTrips;
    List<Trip> mSavedTrips;
    List<String> mAllSavedTrips;
    List<Trip> mFeaturedTrips;
    List<Trip> mNearbyTrips;
    List<TripDetails> mTripDetails;
    TripsAdapter mFeaturedAdapter;
    TripsAdapter mNearbyAdapter;
    TripsAdapter mSavedAdapter;
    CurrentLocation mCurrentLocation;
    ParseGeoPoint mGeopoint = null;
    int mMiles = DISTANCE_LIMIT_DEFAULT;
    boolean mReverse = false;
    private SwipeRefreshLayout swipeContainer;

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

        if (!Places.isInitialized()) {
            Places.initialize(getActivity().getApplicationContext(), getString(R.string.google_maps_api_key));
        }
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
        mAllSavedTrips = new ArrayList<>();
        mTripDetails = new ArrayList<>();
        queryTrips();
        queryAllSavedTrips();
        querySavedTrips();
        onLocationFilterClick();

        ArrayAdapter<String> milesAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, MILES);
        mBinding.milesDropdown.setAdapter(milesAdapter);
        ArrayAdapter<String> sortAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, ORDER);
        mBinding.orderDropdown.setAdapter(sortAdapter);

        mBinding.pbLoading.setVisibility(ProgressBar.VISIBLE);
        mCurrentLocation.setLocationListenerCallback(new CurrentLocation.LocationListenerCallback() {
            @Override
            public void locationChanged() {
                queryNearbyTrips(DISTANCE_LIMIT_DEFAULT);
                onFilterClick();
                onSortClicked();
            }
        });

        // Set Featured trips recycler view
        mFeaturedAdapter = new TripsAdapter(getContext(), mFeaturedTrips, getActivity(), mAllSavedTrips);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        mBinding.rvFeatured.setAdapter(mFeaturedAdapter);
        mBinding.rvFeatured.setLayoutManager(linearLayoutManager);

        // Set Nearby trips recycler view
        mNearbyAdapter = new TripsAdapter(getContext(), mNearbyTrips, getActivity(), mAllSavedTrips);
        LinearLayoutManager linearLayoutManagerNearby = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        mBinding.rvNearbyTrips.setAdapter(mNearbyAdapter);
        mBinding.rvNearbyTrips.setLayoutManager(linearLayoutManagerNearby);

        // Set Saved trips recycler view
        mSavedAdapter = new TripsAdapter(getContext(), mSavedTrips, getActivity(), mAllSavedTrips);
        mSavedAdapter.notifyDataSetChanged();
        LinearLayoutManager linearLayoutManagerSaved = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        mBinding.rvMostSaved.setAdapter(mSavedAdapter);
        mBinding.rvMostSaved.setLayoutManager(linearLayoutManagerSaved);
    }

    private void getLatLng() {
        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
        mBinding.btnFind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Address> addressList = new ArrayList<>();
                try {
                    addressList = geocoder.getFromLocationName(mBinding.etLocation.getText().toString(), MAX_ADDRESS);
                    Address address = addressList.get(0);
                    mGeopoint = new ParseGeoPoint(address.getLatitude(), address.getLongitude());

                } catch (IOException e) {
                    e.printStackTrace();
                }
                if(addressList.isEmpty()) {
                    Toast.makeText(getContext(), R.string.nearbyLocation, Toast.LENGTH_SHORT).show();
                    mGeopoint = null;
                }
                InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(v.getApplicationWindowToken(), FLAG);
                queryNearbyTrips(mMiles);
                mNearbyAdapter.clear();
                mNearbyAdapter.notifyDataSetChanged();
            }
        });
    }

    private void onLocationFilterClick() {
        mBinding.etLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Place.Field> fields = Arrays.asList(Place.Field.ADDRESS, Place.Field.LAT_LNG, Place.Field.NAME);
                Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields).build(getActivity());
                startActivityForResult(intent, AUTOCOMPLETE_CODE);
            }
        });
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
        query.whereEqualTo(Trip.KEY_PRIVATE, false);
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
        tripQuery.whereEqualTo(Trip.KEY_PRIVATE, false);
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
                ParseGeoPoint geoPoint = mCurrentLocation.getCurrentLocation();
                if(mGeopoint != null) {
                    geoPoint = mGeopoint;
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

    private void queryAllSavedTrips() {
        ParseQuery<SavedTrips> query = ParseQuery.getQuery(SavedTrips.class);
        query.include(Trip.KEY_USER);
        query.whereEqualTo(Trip.KEY_USER, ParseUser.getCurrentUser());
        query.whereEqualTo(Trip.KEY_PRIVATE, false);
        query.findInBackground(new FindCallback<SavedTrips>() {
            @Override
            public void done(List<SavedTrips> savedTrips, ParseException exception) {
                if(exception != null) {
                    Log.e(TAG, "Issue with getting saved trips", exception);
                }
                for(SavedTrips trip : savedTrips) {
                    mAllSavedTrips.add(trip.getTrip().getObjectId());
                }
            }
        });
    }

    private void querySavedTrips() {
        ParseQuery<Trip> query = new ParseQuery<>(Trip.class);
        query.include(Trip.KEY_SAVE_COUNT);
        query.whereNotEqualTo(Trip.KEY_SAVE_COUNT, 0);
        query.whereEqualTo(Trip.KEY_PRIVATE, false);
        query.orderByDescending(Trip.KEY_SAVE_COUNT);
        query.setLimit(SAVED_TRIPS_LIMIT);
        query.findInBackground(new FindCallback<Trip>() {
            @Override
            public void done(List<Trip> savedTrips, ParseException exception) {
                Log.i(TAG, "Saved trips: " + savedTrips);
                mSavedTrips.addAll(savedTrips);
                mSavedAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == AUTOCOMPLETE_CODE && resultCode == RESULT_OK) {
            Place place = Autocomplete.getPlaceFromIntent(data);
            mBinding.etLocation.setText(place.getAddress());
            getLatLng();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void notifyAdapterChange() {
        mSavedAdapter.notifyDataSetChanged();
        mFeaturedAdapter.notifyDataSetChanged();
        mNearbyAdapter.notifyDataSetChanged();
    }
}