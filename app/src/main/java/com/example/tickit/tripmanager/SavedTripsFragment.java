package com.example.tickit.tripmanager;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.tickit.R;
import com.example.tickit.databinding.FragmentSavedTripsBinding;
import com.example.tickit.models.SavedTrips;
import com.example.tickit.models.Trip;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class SavedTripsFragment extends Fragment {

    public static final String TAG = "SavedTripsFragment";
    private static final int GRID_COUNT = 2;

    private FragmentSavedTripsBinding mBinding;
    private List<Trip> mSavedTrips;
    private List<String> mSavedTripsId;
    private TripsAdapter mAdapter;

    public SavedTripsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mBinding = FragmentSavedTripsBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mSavedTrips = new ArrayList<>();
        mSavedTripsId = new ArrayList<>();
        querySavedTrips();

        mAdapter = new TripsAdapter(getContext(), mSavedTrips, getActivity(), mSavedTripsId);
//        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
//        mBinding.rvSaved.setLayoutManager(linearLayoutManager);
//        mBinding.rvSaved.setAdapter(mAdapter);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), GRID_COUNT);
        mBinding.rvSaved.setAdapter(mAdapter);
        mBinding.rvSaved.setLayoutManager(gridLayoutManager);
    }

    private void querySavedTrips() {
        ParseQuery<SavedTrips> query = ParseQuery.getQuery(SavedTrips.class);
        query.include(SavedTrips.KEY_USER);
        query.whereEqualTo(SavedTrips.KEY_USER, ParseUser.getCurrentUser());
        query.addDescendingOrder(SavedTrips.KEY_CREATED_AT);

        query.findInBackground(new FindCallback<SavedTrips>() {
            @Override
            public void done(List<SavedTrips> savedTrips, ParseException exception) {
                if(exception != null) {
                    Log.e(TAG, "Issue querying saved trips", exception);
                    return;
                }
                for(SavedTrips trip : savedTrips) {
                    mSavedTrips.add((Trip) trip.getTrip());
                    mSavedTripsId.add(trip.getTrip().getObjectId());
                }
                Log.i(TAG, "Saved trips: " + mSavedTrips);
                mAdapter.notifyDataSetChanged();
            }
        });
    }
}