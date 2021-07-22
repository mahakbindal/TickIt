package com.example.tickit.tripmanager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.tickit.R;
import com.example.tickit.models.Trip;
import com.parse.ParseFile;

import org.parceler.Parcels;

import java.util.List;

public class TripsAdapter extends RecyclerView.Adapter<TripsAdapter.ViewHolder> {

    public static final String TRIP_EXTRA = "trip";
    static Context mContext;
    private List<Trip> mTrips;
    private Activity mActivity;

    public TripsAdapter(Context context, List<Trip> mTrips, Activity activity) {
        this.mContext = context;
        this.mTrips = mTrips;
        this.mActivity = activity;
    }

    @NonNull
    @Override
    public TripsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_trip, parent, false);
//        return new ViewHolder(itemView, mContext);
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_trip, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        onTripClicked(viewHolder);
//        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Trip trip = mTrips.get(viewHolder.getAdapterPosition());
//                Intent intent = new Intent(mContext, TripDetailsActivity.class);
//                intent.putExtra(TRIP_EXTRA, Parcels.wrap(trip));
//                mContext.startActivity(intent);
//                mActivity.overridePendingTransition(R.anim.right_in, R.anim.left_out);
//            }
//        });
        return viewHolder;
    }

    private void onTripClicked(ViewHolder viewHolder) {
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Trip trip = mTrips.get(viewHolder.getAdapterPosition());
                newIntentForTripData(trip);
                mActivity.overridePendingTransition(R.anim.right_in, R.anim.left_out);
            }
        });
    }

    public static Intent newIntentForTripData(Trip trip) {
        Intent intent = new Intent(mContext, TripDetailsActivity.class);
        intent.putExtra(TRIP_EXTRA, Parcels.wrap(trip));
        mContext.startActivity(intent);
        return intent;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Trip trip = mTrips.get(position);
        holder.mRootView.setTag(trip);
        holder.mTvTripName.setText(trip.getTitle());
        ParseFile image = trip.getImage();
        Glide.with(mContext).load(image.getUrl()).into(holder.mIvTripPic);
    }

    @Override
    public int getItemCount() {
        return mTrips.size();
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

    public class ViewHolder extends RecyclerView.ViewHolder {

        private View mRootView;
        private ImageView mIvTripPic;
        private View mVPalette;
        private TextView mTvTripName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mRootView = itemView;
            mIvTripPic = itemView.findViewById(R.id.ivTripPic);
            mVPalette = itemView.findViewById(R.id.vPalette);
            mTvTripName = itemView.findViewById(R.id.tvTripName);
        }

    }
}
