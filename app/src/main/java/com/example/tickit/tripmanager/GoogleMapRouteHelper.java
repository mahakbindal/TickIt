package com.example.tickit.tripmanager;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import com.example.tickit.R;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.internal.PolylineEncoding;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.Duration;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class GoogleMapRouteHelper {

    public static final String TAG = "GoogleMapRouteHelper";
    public static final String KILOMETERS = "km";
    public static final String DECIMAL_FORMAT = "###.##";
    public static final int ADDRESS_RETRIEVAL_MAX = 1;
    public static final int CAMERA_PADDING = 100;
    public static final int HOURS_IN_DAY = 24;
    public static final int MINS_IN_HOUR = 60;
    public static final double KM_TO_MILES = 1.609;

    public Context mContext;
    public List<List<Address>> mLocationsList;
    public List<LatLng> mLatLngList;
    public List<MarkerOptions> mMarkerList;
    private GeoApiContext mGeoApiContext = null;
    GoogleMap mGoogleMap;
    Long mTotalDuration = 0L;
    List<String> mLegDistances;
    private DurationCallback mDurationCallback;

    public GoogleMapRouteHelper(Context context, GoogleMap googleMap, GeoApiContext geoApiContext) {
        this.mContext = context;
        this.mGoogleMap = googleMap;
        this.mGeoApiContext = geoApiContext;
        mLocationsList = new ArrayList<>();
        mLatLngList = new ArrayList<>();
        mMarkerList = new ArrayList<>();
        mLegDistances = new ArrayList<>();
    }

    /* Converts each user input from rawLocationList to a valid address by calling Google's Geocoder
    * API. Adds each address to mLocationsList, and retrieves the latitude and longitude of each
    * address which is appended to mLatLngList.  */
    public void geoLocate(List<String> rawLocationList) throws IOException {
        Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());
        List<String> markerTitleList = new ArrayList<>();
        for(String rawLocation : rawLocationList) {
            List<Address> addressList = geocoder.getFromLocationName(rawLocation, ADDRESS_RETRIEVAL_MAX);
            if(addressList.isEmpty()) {
                Toast.makeText(mContext, R.string.invalid_location, Toast.LENGTH_SHORT).show();
                return;
            }
            mLocationsList.add(addressList);
            Address address = addressList.get(0);
            LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
            markerTitleList.add(address.getAddressLine(0));
            mLatLngList.add(latLng);
//            drawPolyline();
        }

        showRoute(mLatLngList, markerTitleList);
    }

    /* Adds markers to the map based on the latitude and longitude of each location. */
    private void addMarkers(List<String> markerTitleList, List<LatLng> latLngList) {
        for(int i = 0; i < latLngList.size(); i++) {
            MarkerOptions marker = new MarkerOptions();
            mGoogleMap.addMarker(marker
                    .position(latLngList.get(i))
                    .title(markerTitleList.get(i)));
            mMarkerList.add(marker);
        }
    }

    /* Updates camera view of map based each marker position. */
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
     * routes for waypoints, if any. Once route is retrieved, calls method to draw route on map.
     * Also calls methods for adding markers, and shifting camera to appropriate view. */
    public void showRoute(List<LatLng> latLngList, List<String> markerTitleList){
        addMarkers(markerTitleList, latLngList);
        goToLocation();
        List<com.google.maps.model.LatLng> convertedLatLngList = convertCoordType(latLngList);

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
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void run() {
                Log.d(TAG, "run: result routes: " + result.routes.length);
                for(DirectionsRoute route: result.routes){
                    Log.d(TAG, "run: leg: " + route.legs[0].toString());
                    for(int i = 0; i < route.legs.length; i++) {
                        mTotalDuration += route.legs[i].duration.inSeconds;
                        mLegDistances.add(route.legs[i].distance.humanReadable);
                    }
                    if(mDurationCallback != null) {
                        mDurationCallback.getDurationCallback();
                    }

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
                    polyline.setColor(ContextCompat.getColor(mContext, R.color.olive));
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

    public List<List<Address>> getLocationList() { return mLocationsList; }

    public String getDuration() {
        int day = (int) TimeUnit.SECONDS.toDays(mTotalDuration);
        long hours = TimeUnit.SECONDS.toHours(mTotalDuration) - (day * HOURS_IN_DAY);
        long minute = TimeUnit.SECONDS.toMinutes(mTotalDuration) - (TimeUnit.SECONDS.toHours(mTotalDuration)* MINS_IN_HOUR);
        StringBuilder duration = new StringBuilder();
        duration.append(day + " days, " + hours + " hours, " + minute + " minutes");
        Log.i(TAG, "Duration" + duration.toString());
        return duration.toString();
    }

    public double getDistance() {
        double distance = 0;
        boolean miles = true;
        for(String legDistance : mLegDistances) {
            String filter = String.valueOf(legDistance).replace(",", "");
            String[] splitDistance = filter.split(" ");
            double numDistance = Double.parseDouble(splitDistance[0]);
            distance += numDistance;
            if(splitDistance[1].equals(KILOMETERS)) miles = false;
        }
        if(!miles) {
            distance /= KM_TO_MILES;
        }
        DecimalFormat df = new DecimalFormat(DECIMAL_FORMAT);
        return Double.parseDouble(df.format(distance));
    }

    public interface DurationCallback {
        void getDurationCallback();
    }

    public void setDurationCallback(DurationCallback durationCallback) {
        this.mDurationCallback = durationCallback;
    }
}
