package com.example.tickit.tripmanager;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import com.example.tickit.R;
import com.example.tickit.databinding.FragmentAllTripsBinding;
import com.example.tickit.databinding.FragmentFeaturedTripsBinding;
import com.example.tickit.helpers.EndlessRecyclerViewScrollListener;
import com.example.tickit.main.MainActivity;
import com.example.tickit.models.Trip;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AllTripsFragment extends Fragment {

    public static final String TAG = "AllTripsFragment";
    public static final int GRID_COUNT = 2;
    private FragmentAllTripsBinding mBinding;
    private List<Trip> mAllTrips;
    private TripsAdapter mAllTripsAdapter;
    private List<Trip> mFilteredTrips;

    public AllTripsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mBinding = FragmentAllTripsBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mAllTrips = new ArrayList<>();

        mAllTripsAdapter = new TripsAdapter(getContext(), mAllTrips, getActivity());
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), GRID_COUNT);
        mBinding.rvAllTrips.setAdapter(mAllTripsAdapter);
        mBinding.rvAllTrips.setLayoutManager(gridLayoutManager);

        queryTrips();
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
                mAllTripsAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem item = menu.findItem(R.id.action_search);
        item.setVisible(true);
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
        mAllTripsAdapter.filterList(mFilteredTrips);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}