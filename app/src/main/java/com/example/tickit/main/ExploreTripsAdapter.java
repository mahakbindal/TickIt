package com.example.tickit.main;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.tickit.tripmanager.AllTripsFragment;
import com.example.tickit.tripmanager.FeaturedTripsFragment;

import org.jetbrains.annotations.NotNull;

public class ExploreTripsAdapter extends FragmentPagerAdapter {

    private int mNumTabs;

    public ExploreTripsAdapter(@NonNull FragmentManager fm, int numTabs) {
        super(fm);
        this.mNumTabs = numTabs;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                FeaturedTripsFragment featuredTripsFragment = new FeaturedTripsFragment();
                return featuredTripsFragment;
            case 1:
                AllTripsFragment allTripsFragment = new AllTripsFragment();
                return allTripsFragment;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return mNumTabs;
    }
}
