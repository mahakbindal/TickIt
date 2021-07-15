package com.example.tickit.fragments;

import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.tickit.R;
import com.example.tickit.Trip;
import com.example.tickit.TripDetails;
import com.example.tickit.WaypointView;
import com.example.tickit.databinding.FragmentCreateBinding;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.internal.PolylineEncoding;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A simple {@link Fragment} subclass.
 */
public class CreateFragment extends Fragment {

    public static final String TAG = "CreateFragment";
    private FragmentCreateBinding mBinding;
    GoogleMap mGoogleMap;
    ImageButton mIbRemove;
    public List<List<Address>> mLocations;
    public List<LatLng> mLatLngList;
    private GeoApiContext mGeoApiContext = null;
    public List<WaypointView> mWaypointsList;

    public CreateFragment() {
        // Required empty public constructor
        mWaypointsList = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Initialize view
//        View view = inflater.inflate(R.layout.fragment_create, container, false);
        mBinding = FragmentCreateBinding.inflate(inflater, container, false);


        // Initialize map fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);

        // Async map
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull GoogleMap googleMap) {
                mGoogleMap = googleMap;

            }
        });

        // Initialize Google Maps Directions API to calculate routes
        if(mGeoApiContext == null) {
            mGeoApiContext = new GeoApiContext.Builder().apiKey(getString(R.string.google_maps_api_key)).build();
        }

        // Initialize minimum two waypoint/location fields
        addWaypointView();
        addWaypointView();

        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mIbRemove = (ImageButton) getView().findViewById(R.id.ibRemove);

        mBinding.btnRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGoogleMap.clear();
                mLocations = new ArrayList<>();
                mLatLngList = new ArrayList<>();
                geoLocate();
            }
        });

        mBinding.btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addWaypointView();
            }
        });

        mBinding.btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ParseUser currentUser = ParseUser.getCurrentUser();
                String title = mBinding.etTitle.getText().toString();
                saveTrip(currentUser, title);
            }
        });

    }

    // https://github.com/droidcodes/DynamicViews/blob/master/app/src/main/java/com/dynamicviews/MainActivity.java
    private void addWaypointView() {
//        final View waypointView = getLayoutInflater().inflate(R.layout.add_row_waypoint, null, false);
        if(mWaypointsList.size() != 25) {
            WaypointView newWaypoint = new WaypointView(getContext());
            mWaypointsList.add(newWaypoint);
            mBinding.layoutList.addView(newWaypoint);
        }
//        mIbRemove.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mBinding.layoutList.removeView(v);
//            }
//        });

    }

    public void getWaypointInput(Geocoder geocoder) throws IOException {
        List<String> locations = new ArrayList<>();
        for(int i = 0; i < mWaypointsList.size(); i++) {
            String loc = mWaypointsList.get(i).getEditTextValue();
            locations.add(loc);
            List<Address> addressList = geocoder.getFromLocationName(loc, 1);
            mLocations.add(addressList);
        }
        Log.i(TAG, "String locations: " + locations);
    }

    private void geoLocate() {
        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
        try {
            getWaypointInput(geocoder);
            for(List<Address> location : mLocations) {
                if(location.isEmpty()) {
                    Toast.makeText(getContext(), R.string.invalid_location, Toast.LENGTH_SHORT).show();
                    return;
                }
                Address address = location.get(0); // gets the first result google retrieves from addressList
                goToLocation(address.getLatitude(), address.getLongitude());
                LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                mLatLngList.add(latLng);
                Log.i(TAG, "latlng points: " + mLatLngList);


                mGoogleMap.addMarker(new MarkerOptions().position(latLng));
//                drawPolyline();
            }
            calculateDirections();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void goToLocation(double latitude, double longitude) {
        LatLng latLng = new LatLng(latitude, longitude);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 4);
        mGoogleMap.moveCamera(cameraUpdate);
        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
    }

    private void calculateDirections(){
        Log.d(TAG, "calculateDirections: calculating directions.");
        List<com.google.maps.model.LatLng> convertedLatLngList = convertCoordType(mLatLngList);

        com.google.maps.model.LatLng destination = convertedLatLngList.get(convertedLatLngList.size()-1);
        DirectionsApiRequest directions = new DirectionsApiRequest(mGeoApiContext);


        directions.alternatives(false);
        directions.origin(convertedLatLngList.get(0));
        Log.i(TAG, "size: " + mLatLngList.size());
        if(mLatLngList.size() > 2) {
            List<com.google.maps.model.LatLng> waypointsList = convertedLatLngList.subList(1, convertedLatLngList.size()-1);
            directions.waypoints(waypointsList.toArray(new com.google.maps.model.LatLng[waypointsList.size()]));
        }
        Log.i(TAG, "Origin location: " + mLatLngList.get(0));
        Log.d(TAG, "calculateDirections: destination: " + destination.toString());
        directions.destination(destination).setCallback(new PendingResult.Callback<DirectionsResult>() {
            @Override
            public void onResult(DirectionsResult result) {
                Log.d(TAG, "onResult: routes: " + result.routes[0].toString());
                Log.d(TAG, "onResult: geocodedWayPoints: " + result.geocodedWaypoints[0].toString());
                addPolylinesToMap(result);
            }

            @Override
            public void onFailure(Throwable e) {
                Log.e(TAG, "onFailure: ", e);

            }
        });
    }

    // https://github.com/googlemaps/google-maps-services-java
    private void addPolylinesToMap(final DirectionsResult result){
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "run: result routes: " + result.routes.length);

                for(DirectionsRoute route: result.routes){
                    Log.d(TAG, "run: leg: " + route.legs[0].toString());
                    List<com.google.maps.model.LatLng> decodedPath = PolylineEncoding.decode(route.overviewPolyline.getEncodedPath());

                    List<LatLng> newDecodedPath = new ArrayList<>();

                    // This loops through all the LatLng coordinates of ONE polyline.
                    for(com.google.maps.model.LatLng latLng: decodedPath){

//                        Log.d(TAG, "run: latlng: " + latLng.toString());

                        newDecodedPath.add(new LatLng(
                                latLng.lat,
                                latLng.lng
                        ));
                    }
                    Polyline polyline = mGoogleMap.addPolyline(new PolylineOptions().addAll(newDecodedPath));
                    polyline.setColor(ContextCompat.getColor(getActivity(), R.color.olive));
                    polyline.setClickable(true);

                }
            }
        });
    }

    // https://stackoverflow.com/questions/60245464/different-latlng-imports
    static List<com.google.maps.model.LatLng> convertCoordType(List<com.google.android.gms.maps.model.LatLng> list) {
        List<com.google.maps.model.LatLng> resultList = new ArrayList<>();
        for (com.google.android.gms.maps.model.LatLng item : list) {
            resultList.add(new com.google.maps.model.LatLng(item.latitude, item.longitude));
        }
        return resultList;
    }

    private void saveTrip(ParseUser currentUser, String title) {
        Trip trip = new Trip();
        trip.setTitle(title);
        trip.setUser(currentUser);

        for(int i = 0; i < mWaypointsList.size(); i++) {
            TripDetails tripDetails = new TripDetails();
            tripDetails.setTrip(trip);
            String loc = mWaypointsList.get(i).getEditTextValue();
            tripDetails.setLocation(loc);
            tripDetails.setLocationIndex(i);

            tripDetails.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if(e != null) {
                        Log.e(TAG, "Error while creating");
                        Toast.makeText(getContext(), R.string.create_error, Toast.LENGTH_SHORT).show();
                    }
                    Toast.makeText(getContext(), R.string.create_success, Toast.LENGTH_SHORT).show();
                }
            });
        }

        trip.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e != null) {
                    Log.e(TAG, "Error while creating");
                    Toast.makeText(getContext(), R.string.create_error, Toast.LENGTH_SHORT).show();
                }
                Toast.makeText(getContext(), R.string.create_success, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Call getFromLocation in gelocation() in background thread; not currently used
    private void launchGeolocate() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                geoLocate();
            }
        });
    }

    private void drawPolyline() {

        // Add polylines to the map.
        // Polylines are useful to show a route or some other connection between points.
        // [START maps_poly_activity_add_polyline_set_tag]
        // [START maps_poly_activity_add_polyline]
        Polyline polyline1 = mGoogleMap.addPolyline(new PolylineOptions()
                .clickable(true)
                .addAll(mLatLngList));
        // [END maps_poly_activity_add_polyline]
    }
}