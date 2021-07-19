package com.example.tickit;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;

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

    public void clickRemove(LinearLayout layout) {
        mRemove.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                layout.removeView(v);
            }
        });
    }

    public int getRemoveId() {
        return mRemove.getId();
    }

    public void setOnClickListenerToRemove(OnClickListener onClickListener){
        mRemove.setOnClickListener(onClickListener);
    }
}
