package com.example.tickit.tripmanager;

import android.content.Context;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.tickit.R;

public class LocationDetailsView extends LinearLayout {

    TextView mLocation;

    public LocationDetailsView(Context context) {
        super(context);
        inflate(context, R.layout.add_row_loc_details, this);
        mLocation = findViewById(R.id.tvLocation);
    }

    public void setTextValue(String text) {
        mLocation.setText(text);
    }
}
