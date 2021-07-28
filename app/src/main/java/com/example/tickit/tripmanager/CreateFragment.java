package com.example.tickit.tripmanager;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.location.Address;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.tickit.helpers.BitmapScaler;
import com.example.tickit.R;
import com.example.tickit.main.MainActivity;
import com.example.tickit.databinding.FragmentCreateBinding;
import com.example.tickit.models.Trip;
import com.example.tickit.models.TripDetails;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.maps.GeoApiContext;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public class CreateFragment extends Fragment {

    public static final String TAG = "CreateFragment";
    public static final int WAYPOINT_LIMIT = 10;
    public static final int LOCATION_MIN = 2;
    public final static int PICK_PHOTO_CODE = 1046;
    public static final int IMG_WIDTH = 200;
    public static final int IMG_QUALITY = 100;
    public static final int AUTOCOMPLETE_CODE = 100;

    public String mPhotoFileName = "photo.jpg";
    private FragmentCreateBinding mBinding;
    GoogleMap mGoogleMap;
    private GeoApiContext mGeoApiContext = null;
    public List<WaypointView> mWaypointsList;
    private WaypointView mWaypoint;
    private ParseFile mPhotoFile;
    private GoogleMapRouteHelper mGoogleMapRouteHelper;
    private List<String> mRawLocationList;
    public PostTransitionCallback mPostTransitionCallback;
    private Map<String, String> locationTitleDesc = new HashMap<>();

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
                if(mGoogleMap == null) {
                    mGoogleMap = googleMap;
                }
                UiSettings uiSettings = mGoogleMap.getUiSettings();
                uiSettings.setZoomControlsEnabled(true);
            }
        });

        // Initialize Google Maps Directions API to calculate routes
        if(mGeoApiContext == null) {
            mGeoApiContext = new GeoApiContext.Builder().apiKey(getString(R.string.google_maps_api_key)).build();
        }

        if (!Places.isInitialized()) {
            Places.initialize(getActivity().getApplicationContext(), getString(R.string.google_maps_api_key));
        }

        // Initialize minimum two waypoint/location fields
        addWaypointView();
        addWaypointView();

        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        onRouteClickListener();
        onAddClickListener();
        onCreateClickListener();
        onSelectImageClickListener();
    }

    private void onRouteClickListener() {
        mBinding.btnRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGoogleMap.clear();
                try {
                    getWaypointInput();
                    getRoute();
                    showAlertDialogForMarker();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void onAddClickListener() {
        mBinding.btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addWaypointView();
            }
        });
    }

    private void onCreateClickListener() {
        mBinding.btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ParseUser currentUser = ParseUser.getCurrentUser();
                String title = mBinding.etTitle.getText().toString();
                saveTrip(currentUser, title);
            }
        });
    }

    private void onSelectImageClickListener() {
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
            newWaypoint.setOnRemoveListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mWaypointsList.size() > LOCATION_MIN) {
                        WaypointView waypointView = (WaypointView) v.getParent().getParent();
                        mWaypointsList.remove(waypointView);
                        mBinding.layoutList.removeView(waypointView);
                    }
                }
            });
            mWaypointsList.add(newWaypoint);
            mBinding.layoutList.addView(newWaypoint);

            newWaypoint.setOnAutocompleteListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mWaypoint = newWaypoint;
                    List<Place.Field> fields = Arrays.asList(Place.Field.ADDRESS, Place.Field.LAT_LNG, Place.Field.NAME);
                    Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields).build(getActivity());
                    startActivityForResult(intent, AUTOCOMPLETE_CODE);
                }
            });
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

    private void showAlertDialogForMarker() {
        View descriptionView = LayoutInflater.from(getContext()).inflate(R.layout.location_description_item, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
        alertDialogBuilder.setView(descriptionView);
        final AlertDialog alertDialog = alertDialogBuilder.create();
        mGoogleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(@NonNull @NotNull Marker marker) {
                alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                EditText etSnippet = alertDialog.findViewById(R.id.etSnippet);
                                String snippet = etSnippet.getText().toString();
                                locationTitleDesc.put(marker.getTitle(), snippet);
                                if(!snippet.equals("")) {
                                    marker.setSnippet(snippet);
                                }
                                marker.showInfoWindow();
                                etSnippet.setText("");
                            }
                        });
                alertDialog.show();
            }
        });
    }

    /* Initializes MapRoute object and begins the call to calculate the route between locations. */
    public void getRoute() throws IOException {
        if(mGoogleMap == null) {
            Toast.makeText(getContext(), R.string.nullMap, Toast.LENGTH_SHORT).show();
            return;
        }
        mGoogleMapRouteHelper = new GoogleMapRouteHelper(getContext(), mGoogleMap, mGeoApiContext);
        mGoogleMapRouteHelper.geoLocate(mRawLocationList);
    }

    // Trigger gallery selection for a photo
    public void onPickPhoto() {
        // Create intent for picking a photo from the gallery
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        startActivityForResult(intent, PICK_PHOTO_CODE);
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
            Bitmap resizedBitmap = BitmapScaler.scaleToFitWidth(rawTakenImage, IMG_WIDTH);
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, IMG_QUALITY, bytes);

            mPhotoFile = conversionBitmapParseFile(resizedBitmap);
            mBinding.ivTripImage.setImageBitmap(resizedBitmap);
        }

        else if(requestCode == AUTOCOMPLETE_CODE && resultCode == RESULT_OK) {
            Place place = Autocomplete.getPlaceFromIntent(data);
            Log.i(TAG, "Place: " + place.getAddress());
            mWaypoint.setEditTextValue(place.getAddress());
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /* Given an image of type Bitmap, converts the image into a ParseFile and returns it.
    * Source: https://findnerd.com/list/view/Convert-Bitmap-to-ParseFile-in-android/11025/ */
    public ParseFile conversionBitmapParseFile(Bitmap imageBitmap){
        ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.PNG, IMG_QUALITY,byteArrayOutputStream);
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
        List<List<Address>> locations = mGoogleMapRouteHelper.getLocationList();

        // Iterate through mWaypointsList to save each location in TripDetails table
        for(int i = 0; i < locations.size(); i++) {
            TripDetails tripDetails = new TripDetails();
            tripDetails.setTrip(trip);
            String locationName = locations.get(i).get(0).getAddressLine(0);
            mBinding.etTitle.setText("");
            mWaypointsList.get(i).setEditTextValue("");
            tripDetails.setLocation(locationName);
            tripDetails.setLocationIndex(i);
            if(locationTitleDesc.containsKey(locationName)) {
                tripDetails.setDescription(locationTitleDesc.get(locationName));
            }

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
                mPostTransitionCallback.goTripsFragment();
            }
        });
    }

    public interface PostTransitionCallback {
        void goTripsFragment();
    }

    public void setPostTransitionCallback(PostTransitionCallback postTransitionCallback) {
        this.mPostTransitionCallback = postTransitionCallback;
    }
}