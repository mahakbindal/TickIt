package com.example.tickit.main;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.example.tickit.R;
import com.google.android.material.tabs.TabLayout;

import org.jetbrains.annotations.NotNull;

public class ExploreTripsFragment extends Fragment {

    public ExploreTripsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_explore_trips, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TabLayout tabLayout = getView().findViewById(R.id.tabLayoutExplore);
        ViewPager viewPager = getView().findViewById(R.id.viewPagerExplore);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(R.string.exploreTrip);
        ExploreTripsAdapter exploreTripsAdapter = new ExploreTripsAdapter(getChildFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(exploreTripsAdapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull @NotNull MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}