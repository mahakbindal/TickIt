package com.example.tickit.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.tickit.R;
import com.example.tickit.Trip;
import com.example.tickit.databinding.ActivityTripDetailsBinding;
import com.example.tickit.fragments.CreateFragment;

import org.parceler.Parcels;

public class TripDetailsActivity extends AppCompatActivity {

    public static final String TRIP = "trip";
    ActivityTripDetailsBinding mBinding;
    Trip mTrip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityTripDetailsBinding.inflate(getLayoutInflater());
        View view = mBinding.getRoot();
        setContentView(view);

        Intent intent = getIntent();
        mTrip = Parcels.unwrap(intent.getParcelableExtra(TRIP));

        mBinding.tvTripTitle.setText(mTrip.getTitle());

        queryTripDetails(mTrip);

    }

    private void queryTripDetails(Trip trip) {
    }
}