package com.example.tickit.tripmanager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.tickit.R;
import com.example.tickit.main.MainActivity;
import com.example.tickit.models.SavedTrips;
import com.example.tickit.models.Trip;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.parceler.Parcels;

import java.util.List;

public class TripsAdapter extends RecyclerView.Adapter<TripsAdapter.ViewHolder> {

    public static final String TAG = "TripsAdapter";
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
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_trip, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        onTripClicked(viewHolder);
        return viewHolder;
    }

    private void onTripClicked(ViewHolder viewHolder) {
        viewHolder.itemView.setOnTouchListener(new View.OnTouchListener() {
            GestureDetector gestureDetector = new GestureDetector(mContext, new GestureDetector.SimpleOnGestureListener(){
                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    Trip trip = mTrips.get(viewHolder.getAdapterPosition());
                    if(!(trip.getUser().getObjectId().equals(ParseUser.getCurrentUser().getObjectId()))) {
                        Log.i(TAG, "Trip user: " + trip.getUser());
                        Log.i(TAG, "Current user: " + ParseUser.getCurrentUser());
                        saveSavedTrip(trip);
                        Toast.makeText(mContext, "Doubled tapped", Toast.LENGTH_SHORT).show();
                    }

                    return super.onDoubleTap(e);
                }

                @Override
                public boolean onSingleTapConfirmed(MotionEvent e) {
                    Trip trip = mTrips.get(viewHolder.getAdapterPosition());
                    mContext.startActivity(TripDetailsActivity.newIntent(mContext, trip));
                    mActivity.overridePendingTransition(R.anim.right_in, R.anim.left_out);
                    return super.onSingleTapConfirmed(e);
                }
            });
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return false;
            }
        });
    }

    private void saveSavedTrip(Trip trip) {
        SavedTrips savedTrips = new SavedTrips();
        savedTrips.setTrip(trip);
        savedTrips.setUser(ParseUser.getCurrentUser());

        savedTrips.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                Toast.makeText(mContext, R.string.saveSuccess, Toast.LENGTH_SHORT).show();
            }
        });
    }



    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Trip trip = (Trip) mTrips.get(position);
        holder.mRootView.setTag(trip);
        try {
            holder.mTvTripName.setText(trip.fetchIfNeeded().getString("title"));
        } catch (ParseException exception) {
            exception.printStackTrace();
        }
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
