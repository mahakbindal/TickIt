package com.example.tickit.fragments;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.tickit.R;
import com.example.tickit.Trip;
import com.example.tickit.TripDetails;
import com.example.tickit.WaypointView;
import com.example.tickit.activities.MainActivity;
import com.example.tickit.databinding.FragmentCreateBinding;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
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
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public class CreateFragment extends Fragment {

    public static final String TAG = "CreateFragment";
    public static final int CAMERA_PADDING = 100;
    public static final int WAYPOINT_LIMIT = 10;
    public static final int ADDRESS_RETRIEVAL_MAX = 1;
    public final static int PICK_PHOTO_CODE = 1046;
    public static final int IMAGE_PICK_CODE = 1000;
    public static final int PERMISSION_CODE = 1001;

    public String mPhotoFileName = "photo.jpg";
    private FragmentCreateBinding mBinding;
    GoogleMap mGoogleMap;
    ImageButton mIbRemove;
    public List<List<Address>> mLocations;
    public List<LatLng> mLatLngList;
    private GeoApiContext mGeoApiContext = null;
    public List<WaypointView> mWaypointsList;
    public List<MarkerOptions> mMarkerList;
    private ParseFile mPhotoFile;

    public CreateFragment() {
        // Required empty public constructor
        mWaypointsList = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Initialize view
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
                mMarkerList = new ArrayList<>();
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

        mBinding.btnSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPickPhoto();
            }
        });

    }

    /* Adds additional waypoints when user clicks the "Add" button */
    private void addWaypointView() {
//        final View waypointView = getLayoutInflater().inflate(R.layout.add_row_waypoint, null, false);
        if(mWaypointsList.size() != WAYPOINT_LIMIT) {
            WaypointView newWaypoint = new WaypointView(getContext());
            newWaypoint.setOnClickListenerToRemove(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    WaypointView waypointView = (WaypointView) v.getParent().getParent();
                    mWaypointsList.remove(waypointView);
                    mBinding.layoutList.removeView(waypointView);
                }
            });
            mWaypointsList.add(newWaypoint);
            mBinding.layoutList.addView(newWaypoint);
        }

    }

    /* Retrieves user input/location from each waypoint views, and converts input into a valid
    Address. Populates mLocations with the Address list. */
    public void getWaypointInput(Geocoder geocoder) throws IOException {

        List<String> locations = new ArrayList<>();
        for(int i = 0; i < mWaypointsList.size(); i++) {
            String loc = mWaypointsList.get(i).getEditTextValue();
            locations.add(loc);
            List<Address> addressList = geocoder.getFromLocationName(loc, ADDRESS_RETRIEVAL_MAX);
            mLocations.add(addressList);
            Log.e(TAG, String.valueOf(mLocations.get(i)));
        }
        Log.i(TAG, "String locations: " + locations);
    }

    /* Retrieves the addresses of the locations, creates LatLng object that is appended to mLatLngList. */
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
                Log.i(TAG, "Address" + address);
                LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                String markerTitle = address.getAddressLine(0);
                mLatLngList.add(latLng);
                Log.i(TAG, "latlng points: " + mLatLngList);
                addMarkers(latLng, markerTitle);
//                drawPolyline();
            }
            goToLocation();
            calculateDirections();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /* Adds markers to the map based on the latitude and longitude of the location. */
    private void addMarkers(LatLng latLng, String markerTitle) {
        MarkerOptions marker = new MarkerOptions();
        mGoogleMap.addMarker(marker
                .position(latLng)
                .title(markerTitle));
        mMarkerList.add(marker);
    }

    /* Updates camera view of map based marker position */
    private void goToLocation() {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for(MarkerOptions marker : mMarkerList) {
            builder.include(marker.getPosition());
        }
        LatLngBounds bounds = builder.build();
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, CAMERA_PADDING);
        mGoogleMap.moveCamera(cameraUpdate);
        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
    }

    /* Calculates the route between a minimum of two locations (origin and destination), and includes
    * routes for waypoints, if any. Once route is retrieved, calls method to draw route on map. */
    private void calculateDirections(){
        List<com.google.maps.model.LatLng> convertedLatLngList = convertCoordType(mLatLngList);

        com.google.maps.model.LatLng destination = convertedLatLngList.get(convertedLatLngList.size()-1);
        DirectionsApiRequest directions = new DirectionsApiRequest(mGeoApiContext);

        directions.alternatives(false);
        directions.origin(convertedLatLngList.get(0));
        // Retrieves the waypoints between origin and destination
        if(convertedLatLngList.size() > 2) {
            List<com.google.maps.model.LatLng> waypointsList = convertedLatLngList.subList(1, convertedLatLngList.size()-1);
            directions.waypoints(waypointsList.toArray(new com.google.maps.model.LatLng[waypointsList.size()]));
        }
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

    /* Draws route on map based on the result/directions received from calculateDirections.
    *  Source: https://github.com/googlemaps/google-maps-services-java */
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

    /* Helper to convert LatLng coordinates from com.google.android.gms.maps.model.LatLng to
    * com.google.maps.model.LatLng (type required to calculate directions).
    * Source: https://stackoverflow.com/questions/60245464/different-latlng-imports */
    static List<com.google.maps.model.LatLng> convertCoordType(List<com.google.android.gms.maps.model.LatLng> list) {
        List<com.google.maps.model.LatLng> resultList = new ArrayList<>();
        for (com.google.android.gms.maps.model.LatLng item : list) {
            resultList.add(new com.google.maps.model.LatLng(item.latitude, item.longitude));
        }
        return resultList;
    }

    // Trigger gallery selection for a photo
    public void onPickPhoto() {
        // Create intent for picking a photo from the gallery
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        // If you call startActivityForResult() using an intent that no app can handle, your app will crash.
        // So as long as the result is not null, it's safe to use the intent.
        if (intent.resolveActivity(getContext().getPackageManager()) != null) {
            // Bring up gallery to select a photo
            startActivityForResult(intent, PICK_PHOTO_CODE);
        }
    }

    public Bitmap loadFromUri(Uri photoUri) {
        Bitmap image = null;
        try {
            // check version of Android on device
            if(Build.VERSION.SDK_INT > 27){
                // on newer versions of Android, use the new decodeBitmap method
                ImageDecoder.Source source = ImageDecoder.createSource(getContext().getContentResolver(), photoUri);
                image = ImageDecoder.decodeBitmap(source);
            } else {
                // support older versions of Android by using getBitmap
                image = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), photoUri);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if ((data != null) && requestCode == PICK_PHOTO_CODE) {
            Uri photoUri = data.getData();

            // Load the image located at photoUri into selectedImage
            Bitmap selectedImage = loadFromUri(photoUri);

            mBinding.ivTripImage.setImageBitmap(selectedImage);
            mPhotoFile = conversionBitmapParseFile(selectedImage);
        }
    }

    public ParseFile conversionBitmapParseFile(Bitmap imageBitmap){
        ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.PNG,100,byteArrayOutputStream);
        byte[] imageByte = byteArrayOutputStream.toByteArray();
        ParseFile parseFile = new ParseFile("image_file.png",imageByte);
        return parseFile;
    }

    /* Saves trip in Parse database along with trip details in respective tables.*/
    private void saveTrip(ParseUser currentUser, String title) {
        Trip trip = new Trip();
        trip.setTitle(title);
        trip.setUser(currentUser);
        trip.setImage(mPhotoFile);

        // Iterate through mWaypointsList to save each location in TripDetails table
        for(int i = 0; i < mWaypointsList.size(); i++) {
            TripDetails tripDetails = new TripDetails();
            tripDetails.setTrip(trip);
            String loc = mWaypointsList.get(i).getEditTextValue();
            mWaypointsList.get(i).setEditTextValue("");
            tripDetails.setLocation(loc);
            tripDetails.setLocationIndex(i);

            tripDetails.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if(e != null) {
                        Log.e(TAG, "Error while creating");
                        Toast.makeText(getContext(), R.string.create_error, Toast.LENGTH_SHORT).show();
                        return;
                    }
//                    Toast.makeText(getContext(), R.string.create_success, Toast.LENGTH_SHORT).show();
                }
            });
        }

        trip.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e != null) {
                    Log.e(TAG, "Error while creating");
                    Toast.makeText(getContext(), R.string.create_error, Toast.LENGTH_SHORT).show();
                    return;
                }
                Toast.makeText(getContext(), R.string.create_success, Toast.LENGTH_SHORT).show();
                ((MainActivity)getActivity()).updateTrips();
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