package com.example.tickit.tripmanager;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import com.example.tickit.R;
import com.example.tickit.databinding.FragmentExploreTripsBinding;
import com.example.tickit.models.Trip;
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
public class ExploreTripsFragment extends Fragment {

    public static final String TAG = "ExploreTripsFragment";
    public static final int GRID_COUNT = 2;

    private FragmentExploreTripsBinding mBinding;
    protected List<Trip> mAllTrips;
    List<Trip> mFeaturedTrips;
    List<Trip> mFilteredTrips;
    TripsAdapter mFeaturedAdapter;
    TripsAdapter mExploreAdapter;

    public ExploreTripsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mBinding = FragmentExploreTripsBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mAllTrips = new ArrayList<>();
        mFeaturedTrips = new ArrayList<>();
        queryTrips();
        mFeaturedAdapter = new TripsAdapter(getContext(), mFeaturedTrips, getActivity());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        mBinding.rvFeatured.setAdapter(mFeaturedAdapter);
        mBinding.rvFeatured.setLayoutManager(linearLayoutManager);

        mExploreAdapter = new TripsAdapter(getContext(), mAllTrips, getActivity());
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), GRID_COUNT);
        mBinding.rvExploreTrips.setAdapter(mExploreAdapter);
        mBinding.rvExploreTrips.setLayoutManager(gridLayoutManager);

    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_search, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filter(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filter(newText);
                return true;
            }
        });
    }

    private void filter(String query) {
        mFilteredTrips = new ArrayList<>();
        for(Trip trip : mAllTrips) {
            String tripTitle = trip.getTitle().toLowerCase();
            if(tripTitle.contains(query.toLowerCase())) {
                mFilteredTrips.add(trip);
            }
        }
        mExploreAdapter.filterList(mFilteredTrips);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    private void populateFeaturedTrips() {
        for(int i = 0; i < 5; i++) {
            int tripIndex = (int)(Math.random() * mAllTrips.size());
            mFeaturedTrips.add(mAllTrips.remove(tripIndex));
        }
    }

    private void queryTrips() {
        ParseQuery<Trip> query = ParseQuery.getQuery(Trip.class);
        query.include(Trip.KEY_USER);
        query.whereNotEqualTo(Trip.KEY_USER, ParseUser.getCurrentUser());
        query.addDescendingOrder(Trip.KEY_CREATED_AT);

        query.findInBackground(new FindCallback<Trip>() {
            @Override
            public void done(List<Trip> trips, ParseException exception) {
                if(exception != null) {
                    Log.e(TAG, "Issue with getting trips", exception);
                    return;
                }

                mAllTrips.addAll(trips);
                mFeaturedAdapter.notifyDataSetChanged();
                mExploreAdapter.notifyDataSetChanged();
                populateFeaturedTrips();
            }
        });

    }
}