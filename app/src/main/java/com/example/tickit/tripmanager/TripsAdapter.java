package com.example.tickit.tripmanager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
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
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

public class TripsAdapter extends RecyclerView.Adapter<TripsAdapter.ViewHolder> {

    public static final String TAG = "TripsAdapter";
    public static final String TRIP_TITLE = "title";
    static Context mContext;
    private List<Trip> mTrips;
    private Activity mActivity;
    private List<String> mSavedTrips;
    private List<String> mAllSavedTrips;
    Trip mTrip;

    public TripsAdapter(Context context, List<Trip> mTrips, Activity activity, List<String> allSavedTrips) {
        this.mContext = context;
        this.mTrips = mTrips;
        this.mActivity = activity;
        this.mAllSavedTrips = allSavedTrips;
        mSavedTrips = new ArrayList<>();
    }

    @NonNull
    @Override
    public TripsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_trip, parent, false);
//        querySavedTrips();
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
//                    if(!(trip.getUser().getObjectId().equals(ParseUser.getCurrentUser().getObjectId())) && !mSavedTrips.contains(trip.getObjectId())) {
//                        saveSavedTrip(trip);
//                    }
                    if(!(trip.getUser().getObjectId().equals(ParseUser.getCurrentUser().getObjectId())) && (int) viewHolder.mSaveButton.getTag() == R.mipmap.unsaved_foreground) {
                        saveSavedTrip(trip);
                        viewHolder.mSaveButton.setImageResource(R.mipmap.saved_foreground);
                        viewHolder.mSaveButton.setTag(R.mipmap.saved_foreground);
                    }
                    else if((int)viewHolder.mSaveButton.getTag() == R.mipmap.saved_foreground) {
                        viewHolder.mSaveButton.setImageResource(R.mipmap.unsaved_foreground);
                        viewHolder.mSaveButton.setTag(R.mipmap.unsaved_foreground);
                        SavedTrips savedTrip = new SavedTrips();
                        savedTrip.setTrip(trip);
                        mTrip.setSaveCount(mTrip.getSaveCount() - 1);
                        savedTrip.setUser(ParseUser.getCurrentUser());
                        savedTrip.deleteInBackground(new DeleteCallback() {
                            @Override
                            public void done(ParseException e) {

                            }
                        });

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
            public void done(ParseException exception) {
                if(exception != null) {
                    Log.e(TAG, "Error saving trip", exception);
                }
                Toast.makeText(mContext, R.string.saveSuccess, Toast.LENGTH_SHORT).show();
            }
        });

        trip.setSaveCount(trip.getSaveCount() + 1);
        trip.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException exception) {
                if(exception != null) {
                    Log.e(TAG, "Error saving trip", exception);
                }
            }
        });

    }

//    private void querySavedTrips() {
//        ParseQuery<SavedTrips> query = ParseQuery.getQuery(SavedTrips.class);
//        query.include(Trip.KEY_USER);
//        query.whereEqualTo(Trip.KEY_USER, ParseUser.getCurrentUser());
//        query.findInBackground(new FindCallback<SavedTrips>() {
//            @Override
//            public void done(List<SavedTrips> savedTrips, ParseException exception) {
//                if(exception != null) {
//                    Log.e(TAG, "Issue with getting saved trips", exception);
//                }
//                for(SavedTrips trip : savedTrips) {
//                    String objId = trip.getTrip().getObjectId();
//                    if(!mSavedTrips.contains(objId)) {
//                        mSavedTrips.add(trip.getTrip().getObjectId());
//                    }
//
//                }
//            }
//        });
//    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
//        Trip trip = (Trip) mTrips.get(position);
        mTrip = (Trip) mTrips.get(position);

        if(mAllSavedTrips.contains(mTrip.getObjectId())) {
            holder.mSaveButton.setImageResource(R.mipmap.saved_foreground);
            holder.mSaveButton.setTag(R.mipmap.saved_foreground);
        } else {
            holder.mSaveButton.setImageResource(R.mipmap.unsaved_foreground);
            holder.mSaveButton.setTag(R.mipmap.unsaved_foreground);
        }
        holder.mRootView.setTag(mTrip);
        try {
            holder.mTvTripName.setText(mTrip.fetchIfNeeded().getString(TRIP_TITLE));
        } catch (ParseException exception) {
            exception.printStackTrace();
        }
        ParseFile image = mTrip.getImage();
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

    public void filterList(List<Trip> filteredTrips) {
        mTrips = filteredTrips;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private View mRootView;
        private ImageView mIvTripPic;
        private View mVPalette;
        private TextView mTvTripName;
        private ImageButton mSaveButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mRootView = itemView;
            mIvTripPic = itemView.findViewById(R.id.ivTripPic);
            mVPalette = itemView.findViewById(R.id.vPalette);
            mTvTripName = itemView.findViewById(R.id.tvTripName);
            mSaveButton = itemView.findViewById(R.id.ibSave);
        }

    }
}
