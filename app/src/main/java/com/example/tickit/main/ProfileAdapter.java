package com.example.tickit.main;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.example.tickit.tripmanager.SavedTripsFragment;
import com.example.tickit.tripmanager.TripsFragment;


public class ProfileAdapter extends FragmentPagerAdapter {

    private int mNumTabs;
    private Context mContext;

    public ProfileAdapter(@NonNull FragmentManager fm, int numTabs, Context context) {
        super(fm);
        this.mNumTabs = numTabs;
        this.mContext = context;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                TripsFragment tripsFragment = new TripsFragment();
                return tripsFragment;
            case 1:
                SavedTripsFragment savedTripsFragment = new SavedTripsFragment();
                return savedTripsFragment;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return mNumTabs;
    }
}
