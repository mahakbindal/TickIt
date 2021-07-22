package com.example.tickit.tripmanager;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.example.tickit.R;

public class WaypointView extends LinearLayout {

    private EditText mWaypoint;
    private ImageButton mRemove;

    public WaypointView(Context context) {
        super(context);
        inflate(context, R.layout.add_row_waypoint, this);
        mWaypoint = findViewById(R.id.etWaypoint);
        mRemove = findViewById(R.id.ibRemove);
    }

    public String getEditTextValue() {
        return mWaypoint.getText().toString();
    }

    public void setEditTextValue(String text) {
        mWaypoint.setText(text);
    }

    public void setOnRemoveListener(OnClickListener onClickListener){
        mRemove.setOnClickListener(onClickListener);
    }

    public void setOnAutocompleteListener(OnClickListener onClickListener){
        mWaypoint.setOnClickListener(onClickListener);
    }
}
