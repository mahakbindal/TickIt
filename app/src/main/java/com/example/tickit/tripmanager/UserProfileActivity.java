package com.example.tickit.tripmanager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.tickit.R;
import com.example.tickit.databinding.ActivityUserProfileBinding;
import com.example.tickit.models.Trip;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class UserProfileActivity extends AppCompatActivity {

    public static final String TAG = "UserProfileActivity";
    public static final String USER_ID_EXTRA = "user_object_id";
    public static final int GRID_COUNT = 2;
    private ActivityUserProfileBinding mBinding;
    private List<Trip> mAllUserTrips;
    private TripsAdapter mTripsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityUserProfileBinding.inflate(getLayoutInflater());
        View view = mBinding.getRoot();
        setContentView(view);

        mAllUserTrips = new ArrayList<>();

        Intent intent = getIntent();
        ParseUser userObjectId = intent.getParcelableExtra(USER_ID_EXTRA);
        queryUserTrips(userObjectId);

        mTripsAdapter = new TripsAdapter(this, mAllUserTrips, UserProfileActivity.this);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, GRID_COUNT);
        mBinding.rvUserTrips.setAdapter(mTripsAdapter);
        mBinding.rvUserTrips.setLayoutManager(gridLayoutManager);
    }

    private void queryUserTrips(ParseUser userObjectId) {
        ParseQuery<Trip> query = ParseQuery.getQuery(Trip.class);
        query.include(Trip.KEY_USER);
        query.whereEqualTo(Trip.KEY_USER, userObjectId);
        query.addDescendingOrder(Trip.KEY_CREATED_AT);

        query.findInBackground(new FindCallback<Trip>() {
            @Override
            public void done(List<Trip> trips, ParseException exception) {
                if(exception != null) {
                    Log.e(TAG, "Issue with getting user's trips", exception);
                    return;
                }
                Log.i(TAG, "Users trips: " + trips);
                mAllUserTrips.addAll(trips);
                mTripsAdapter.notifyDataSetChanged();
            }
        });
    }

    public static Intent userInfoIntent(Context context, ParseUser userObjectId) {
        Intent intent = new Intent(context, UserProfileActivity.class);
        intent.putExtra(USER_ID_EXTRA, userObjectId);
        return intent;
    }
}