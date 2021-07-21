package com.example.tickit.fragments;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import com.example.tickit.BitmapScaler;
import com.example.tickit.MapRoute;
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
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
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
    public static final int WAYPOINT_LIMIT = 10;
    public final static int PICK_PHOTO_CODE = 1046;

    public String mPhotoFileName = "photo.jpg";
    private FragmentCreateBinding mBinding;
    GoogleMap mGoogleMap;
    ImageButton mIbRemove;
    private GeoApiContext mGeoApiContext = null;
    public List<WaypointView> mWaypointsList;
    private ParseFile mPhotoFile;
    private MapRoute mMapRoute;
    private List<String> mRawLocationList;
    private Context mContext;

    public CreateFragment() {
        // Required empty public constructor
        mWaypointsList = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Initialize view
        mBinding = FragmentCreateBinding.inflate(inflater, container, false);
        mContext = getContext();

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
                try {
                    getWaypointInput();
                    getRoute();
                } catch (IOException e) {
                    e.printStackTrace();
                }
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
        if(mWaypointsList.size() != WAYPOINT_LIMIT) {
            WaypointView newWaypoint = new WaypointView(getContext());
            newWaypoint.setOnClickListenerToRemove(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mWaypointsList.size() > 2) {
                        WaypointView waypointView = (WaypointView) v.getParent().getParent();
                        mWaypointsList.remove(waypointView);
                        mBinding.layoutList.removeView(waypointView);
                    }

                }
            });
            mWaypointsList.add(newWaypoint);
            mBinding.layoutList.addView(newWaypoint);
        }

    }

    /* Retrieves user input/location from each waypoint views, and converts input into a valid
    Address. Populates mLocations with the Address list. */
    public void getWaypointInput() throws IOException {
        mRawLocationList = new ArrayList<>();
        for(int i = 0; i < mWaypointsList.size(); i++) {
            String loc = mWaypointsList.get(i).getEditTextValue();
            mRawLocationList.add(loc);
        }
    }

    /* Initializes MapRoute object and begins the call to calculate the route between locations. */
    public void getRoute() throws IOException {
        List<LatLng> latLngList = new ArrayList<>();
        mMapRoute = new MapRoute(getContext(), mGoogleMap, mGeoApiContext, latLngList);
        mMapRoute.geoLocate(mRawLocationList);
    }

    // Trigger gallery selection for a photo
    public void onPickPhoto() {
        // Create intent for picking a photo from the gallery
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        startActivityForResult(intent, PICK_PHOTO_CODE);
//        if (intent.resolveActivity(mContext.getPackageManager()) != null) {
//            // Bring up gallery to select a photo
//            startActivityForResult(intent, PICK_PHOTO_CODE);
//        }
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

            Bitmap rawTakenImage = loadFromUri(photoUri);
            Bitmap resizedBitmap = BitmapScaler.scaleToFitWidth(rawTakenImage, 200);
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);

            mPhotoFile = conversionBitmapParseFile(resizedBitmap);
            mBinding.ivTripImage.setImageBitmap(resizedBitmap);
        }
    }

    /* Given an image of type Bitmap, converts the image into a ParseFile and returns it.
    * Source: https://findnerd.com/list/view/Convert-Bitmap-to-ParseFile-in-android/11025/ */
    public ParseFile conversionBitmapParseFile(Bitmap imageBitmap){
        ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.PNG,100,byteArrayOutputStream);
        byte[] imageByte = byteArrayOutputStream.toByteArray();
        ParseFile parseFile = new ParseFile(mPhotoFileName,imageByte);
        return parseFile;
    }

    /* Saves trip in Parse database along with trip details in respective tables.*/
    private void saveTrip(ParseUser currentUser, String title) {
        Trip trip = new Trip();
        trip.setTitle(title);
        trip.setUser(currentUser);
        if(mPhotoFile != null) {
            trip.setImage(mPhotoFile);
        }
        List<List<Address>> locations = mMapRoute.getLocationList();

        // Iterate through mWaypointsList to save each location in TripDetails table
        for(int i = 0; i < locations.size(); i++) {
            TripDetails tripDetails = new TripDetails();
            tripDetails.setTrip(trip);
            String locationName = locations.get(i).get(0).getAddressLine(0);
            mBinding.etTitle.setText("");
            mWaypointsList.get(i).setEditTextValue("");
            tripDetails.setLocation(locationName);
            tripDetails.setLocationIndex(i);

            Address address = locations.get(i).get(0);
            ParseGeoPoint geoPoint = new ParseGeoPoint(address.getLatitude(), address.getLongitude());
            tripDetails.setLatLng(geoPoint);

            tripDetails.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if(e != null) {
                        Log.e(TAG, "Error while creating");
                        Toast.makeText(getContext(), R.string.create_error, Toast.LENGTH_SHORT).show();
                        return;
                    }
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
}