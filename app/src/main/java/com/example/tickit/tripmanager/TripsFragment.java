package com.example.tickit.tripmanager;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.tickit.R;
import com.example.tickit.databinding.FragmentTripsBinding;
import com.example.tickit.helpers.EndlessRecyclerViewScrollListener;
import com.example.tickit.main.MainActivity;
import com.example.tickit.models.SavedTrips;
import com.example.tickit.models.Trip;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class TripsFragment extends Fragment {

    public static final String TAG = "TripsFragment";
    public static final int GRID_COUNT = 2;
    public static final int LIMIT = 6;

    private FragmentTripsBinding mBinding;
    private EndlessRecyclerViewScrollListener mScrollListener;
    protected List<Trip> mAllTrips;
    private List<String> mAllSavedTrips;
    GridLayoutManager mGridLayoutManager;
    MyTripsAdapter mAdapter;

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

        mAllSavedTrips = new ArrayList<>();
        queryAllSavedTrips();

        mAllTrips = new ArrayList<>();

        mAdapter = new MyTripsAdapter(getContext(), getActivity(), mAllTrips);

        mGridLayoutManager = new GridLayoutManager(getContext(), GRID_COUNT);
        mBinding.rvTrips.setAdapter(mAdapter);
        mBinding.rvTrips.setLayoutManager(mGridLayoutManager);

        mScrollListener = new EndlessRecyclerViewScrollListener(mGridLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                Date createdAt = mAllTrips.get(mAllTrips.size() - 1).getCreatedAt();
                loadNextDataFromApi(createdAt);
            }
        };
        mBinding.rvTrips.addOnScrollListener(mScrollListener);
        String username = getResources().getString(R.string.at_sign) + ParseUser.getCurrentUser().getUsername();
        mBinding.tvUsername.setText(username);
        queryTrips();
    }

    private void loadNextDataFromApi(Date createdAt) {
        ParseQuery<Trip> query = ParseQuery.getQuery(Trip.class);
        query.include(Trip.KEY_USER);
        query.setLimit(LIMIT);
        query.whereEqualTo(Trip.KEY_USER, ParseUser.getCurrentUser());
        query.addDescendingOrder(Trip.KEY_CREATED_AT);
        query.whereLessThan(Trip.KEY_CREATED_AT, createdAt);

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

    private void queryTrips() {
        ParseQuery<Trip> query = ParseQuery.getQuery(Trip.class);
        query.include(Trip.KEY_USER);
        query.setLimit(LIMIT);
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

    private void queryAllSavedTrips() {
        ParseQuery<SavedTrips> query = ParseQuery.getQuery(SavedTrips.class);
        query.include(Trip.KEY_USER);
        query.whereEqualTo(Trip.KEY_USER, ParseUser.getCurrentUser());
        query.findInBackground(new FindCallback<SavedTrips>() {
            @Override
            public void done(List<SavedTrips> savedTrips, ParseException exception) {
                if(exception != null) {
                    Log.e(TAG, "Issue with getting saved trips", exception);
                }
                for(SavedTrips trip : savedTrips) {
                    mAllSavedTrips.add(trip.getTrip().getObjectId());
                }
            }
        });
    }
}