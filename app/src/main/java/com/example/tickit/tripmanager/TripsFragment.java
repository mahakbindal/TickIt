package com.example.tickit.tripmanager;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.tickit.databinding.FragmentTripsBinding;
import com.example.tickit.models.Trip;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class TripsFragment extends Fragment {

    public static final String TAG = "TripsFragment";
    public static final int GRID_COUNT = 2;

    private FragmentTripsBinding mBinding;
    protected List<Trip> mAllTrips;
    GridLayoutManager mGridLayoutManager;
    TripsAdapter mAdapter;

    public TripsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mBinding = FragmentTripsBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Lookup the swipe container view
        // Setup refresh listener which triggers new data loading
        mBinding.swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Your code to refresh the list here.
                // Make sure you call swipeContainer.setRefreshing(false)
                // once the network request has completed successfully.
                fetchTimelineAsync();
            }
        });
        // Configure the refreshing colors
        mBinding.swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        mAllTrips = new ArrayList<>();

        mAdapter = new TripsAdapter(getContext(), mAllTrips, getActivity());

        mGridLayoutManager = new GridLayoutManager(getContext(), GRID_COUNT);
        mBinding.rvTrips.setAdapter(mAdapter);
        mBinding.rvTrips.setLayoutManager(mGridLayoutManager);
        queryTrips();
    }

    private void fetchTimelineAsync() {
        mAdapter.clear();
        queryTrips();
        mBinding.swipeContainer.setRefreshing(false);
    }

    private void queryTrips() {
        ParseQuery<Trip> query = ParseQuery.getQuery(Trip.class);
        query.include(Trip.KEY_USER);
        query.whereEqualTo(Trip.KEY_USER, ParseUser.getCurrentUser());
        query.addDescendingOrder(Trip.KEY_CREATED_AT);

        query.findInBackground(new FindCallback<Trip>() {
            @Override
            public void done(List<Trip> trips, ParseException exception) {
                if(exception != null) {
                    Log.e(TAG, "Issue with getting trips", exception);
                    return;
                }

                mAllTrips.addAll(trips);
                mAdapter.notifyDataSetChanged();
            }
        });

    }
}