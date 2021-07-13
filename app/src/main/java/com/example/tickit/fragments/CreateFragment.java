package com.example.tickit.fragments;

import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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
import java.util.Arrays;
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
    public List<LatLng> mLatLngList;

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

                mGoogleMap = googleMap;

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
                mLatLngList = new ArrayList<>();
                geoLocate(v);
            }
        });
    }

    private void geoLocate(View v) {
        ArrayList<Integer> locationId = new ArrayList<>(Arrays.asList(R.id.etLocation1, R.id.etLocation2));

        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
        try {
            for(int id: locationId) {
                EditText et = (EditText) getView().findViewById(id);
                String loc = et.getText().toString();
                Log.i(TAG, "locations: " + loc);

                List<Address> addressList = geocoder.getFromLocationName(loc, 1);
                mLocations.add(addressList);
                Log.i(TAG, "address: " + mLocations);
            }

            for(List<Address> location : mLocations) {
                if(location.isEmpty()) {
                    Toast.makeText(getContext(), R.string.invalid_location, Toast.LENGTH_SHORT).show();
                    return;
                }
                Address address = location.get(0);
                goToLocation(address.getLatitude(), address.getLongitude());
                LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                mLatLngList.add(latLng);
                Log.i(TAG, "latlng points: " + mLatLngList);
                mGoogleMap.addMarker(new MarkerOptions().position(latLng));
                drawPolyline();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    private void goToLocation(double latitude, double longitude) {
        LatLng latLng = new LatLng(latitude, longitude);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 4);
        mGoogleMap.moveCamera(cameraUpdate);
        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
    }
}