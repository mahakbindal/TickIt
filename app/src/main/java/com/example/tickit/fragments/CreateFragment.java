package com.example.tickit.fragments;

import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.tickit.R;
import com.example.tickit.databinding.FragmentCreateBinding;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 */
public class CreateFragment extends Fragment {

    public static final String TAG = "CreateFragment";
    private FragmentCreateBinding mBinding;
    GoogleMap mGoogleMap;
    public List<List<Address>> mLocations;

    public CreateFragment() {
        // Required empty public constructor
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
                // Add polylines to the map.
                // Polylines are useful to show a route or some other connection between points.
                // [START maps_poly_activity_add_polyline_set_tag]
                // [START maps_poly_activity_add_polyline]
                mGoogleMap = googleMap;
                Polyline polyline1 = mGoogleMap.addPolyline(new PolylineOptions()
                        .clickable(true)
                        .add(
                                new LatLng(-35.016, 143.321),
                                new LatLng(-34.747, 145.592),
                                new LatLng(-34.364, 147.891),
                                new LatLng(-33.501, 150.217),
                                new LatLng(-32.306, 149.248),
                                new LatLng(-32.491, 147.309)));
                // [END maps_poly_activity_add_polyline]
                // [START_EXCLUDE silent]
                // Store a data object with the polyline, used here to indicate an arbitrary type.
                polyline1.setTag("A");
                // Position the map's camera near Alice Springs in the center of Australia,
                // and set the zoom factor so most of Australia shows on the screen.
                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-23.684, 133.903), 4));

            }
        });
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mBinding.btnRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLocations = new ArrayList<>();
                geoLocate(v);
            }
        });
    }

    private void geoLocate(View v) {
        String location1 = mBinding.etLocation1.getText().toString();
        String location2 = mBinding.etLocation2.getText().toString();
        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
        try {
            List<Address> addressList = geocoder.getFromLocationName(location1, 1);
            Log.i(TAG, "addressList: " + addressList);


            if(!addressList.isEmpty()){
                Address address = addressList.get(0);
                goToLocation(address.getLatitude(), address.getLongitude());
                LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                mGoogleMap.addMarker(new MarkerOptions().position(latLng));

                Toast.makeText(getContext(), address.getLocality(), Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void goToLocation(double latitude, double longitude) {
        LatLng latLng = new LatLng(latitude, longitude);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 18);
        mGoogleMap.moveCamera(cameraUpdate);
        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
    }
}