package com.example.tickit;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.LinearLayout;

public class WaypointView extends LinearLayout {

    EditText mWaypoint;

    public WaypointView(Context context) {
        super(context);
        inflate(context, R.layout.add_row_waypoint, this);
        mWaypoint = findViewById(R.id.etWaypoint);
    }

    public String getEditTextValue() {
        return mWaypoint.getText().toString();
    }


}
