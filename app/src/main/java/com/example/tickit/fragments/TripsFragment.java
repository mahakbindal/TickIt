package com.example.tickit.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.tickit.R;
import com.example.tickit.Trip;
import com.example.tickit.TripsAdapter;
import com.example.tickit.databinding.FragmentTripsBinding;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.jetbrains.annotations.NotNull;

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

        mAllTrips = new ArrayList<>();
        queryTrips();
        mAdapter = new TripsAdapter(getContext(), mAllTrips);

        mGridLayoutManager = new GridLayoutManager(getContext(), GRID_COUNT);
        mBinding.rvTrips.setAdapter(mAdapter);
        mBinding.rvTrips.setLayoutManager(mGridLayoutManager);
    }

    private void queryTrips() {
        ParseQuery<Trip> query = ParseQuery.getQuery(Trip.class);
        query.include(Trip.KEY_USER);
        query.whereEqualTo(Trip.KEY_USER, ParseUser.getCurrentUser());
        query.addDescendingOrder(Trip.KEY_CREATED_AT);

        query.findInBackground(new FindCallback<Trip>() {
            @Override
            public void done(List<Trip> trips, ParseException e) {
                if(e != null) {
                    Log.e(TAG, "Issue with getting trips", e);
                    return;
                }

                mAllTrips.addAll(trips);
                mAdapter.notifyDataSetChanged();
            }
        });

    }
}