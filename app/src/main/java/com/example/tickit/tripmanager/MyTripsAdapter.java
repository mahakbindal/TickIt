package com.example.tickit.tripmanager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.tickit.R;
import com.example.tickit.models.Trip;
import com.parse.ParseException;
import com.parse.ParseFile;

import java.util.List;


public class MyTripsAdapter extends RecyclerView.Adapter<MyTripsAdapter.ViewHolder> {

    public static final String TAG = "MyTripsAdapter";
    public static final String TRIP_TITLE = "title";
    private Context mContext;
    private List<Trip> mTrips;
    private Activity mActivity;

    public MyTripsAdapter(Context mContext, Activity activity, List<Trip> mTrips) {
        this.mContext = mContext;
        this.mActivity = activity;
        this.mTrips = mTrips;
    }

    @NonNull
    @Override
    public MyTripsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_my_trip, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Trip trip = mTrips.get(viewHolder.getAdapterPosition());
                mContext.startActivity(TripDetailsActivity.newIntent(mContext, trip));
                mActivity.overridePendingTransition(R.anim.right_in, R.anim.left_out);
            }
        });
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyTripsAdapter.ViewHolder holder, int position) {
        Trip trip = mTrips.get(position);
        holder.mRootView.setTag(trip);
        try {
            holder.mTvTripName.setText(trip.fetchIfNeeded().getString(TRIP_TITLE));
        } catch (ParseException exception) {
            exception.printStackTrace();
        }
        ParseFile image = trip.getImage();
        Glide.with(mContext).load(image.getUrl()).into(holder.mIvTripPic);
    }

    // Clean all elements of the recycler
    public void clear() {
        mTrips.clear();
        notifyDataSetChanged();
    }

    // Add a list of items -- change to type used
    public void addAll(List<Trip> list) {
        mTrips.addAll(list);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mTrips.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private View mRootView;
        private ImageView mIvTripPic;
        private TextView mTvTripName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mRootView = itemView;
            mIvTripPic = itemView.findViewById(R.id.ivTripPic);
            mTvTripName = itemView.findViewById(R.id.tvTripName);
        }
    }
}
