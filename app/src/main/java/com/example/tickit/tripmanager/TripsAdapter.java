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
import androidx.appcompat.content.res.AppCompatResources;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TripsAdapter extends RecyclerView.Adapter<TripsAdapter.ViewHolder> {

    public static final String TAG = "TripsAdapter";
    static Context mContext;
    private List<Trip> mTrips;
    private Activity mActivity;
    private List<String> mAllSavedTrips;
    private Map<String, String> mTripToSavedTrip = new HashMap<>();

    public TripsAdapter(Context context, List<Trip> mTrips, Activity activity, List<String> allSavedTrips) {
        this.mContext = context;
        this.mTrips = mTrips;
        this.mActivity = activity;
        this.mAllSavedTrips = allSavedTrips;
    }

    @NonNull
    @Override
    public TripsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_trip, parent, false);
        setTripToSavedTrip();
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    private void onTripClicked(ViewHolder viewHolder) {
        viewHolder.itemView.setOnTouchListener(new View.OnTouchListener() {
            GestureDetector gestureDetector = new GestureDetector(mContext, new GestureDetector.SimpleOnGestureListener(){
                @Override
                public boolean onDoubleTap(MotionEvent event) {
                    Trip trip = mTrips.get(viewHolder.getAdapterPosition());
                    if(!(trip.getUser().getObjectId().equals(ParseUser.getCurrentUser().getObjectId())) &&
                            !mTripToSavedTrip.containsKey(trip.getObjectId()) && (int) viewHolder.mSaveButton.getTag() == R.drawable.unsaved) {
                        saveSavedTrip(trip, viewHolder);
                    }
                    else if((int) viewHolder.mSaveButton.getTag() == R.drawable.saved) {
                        unSaveSavedTrip(trip, viewHolder);
                    }
                    else if(trip.getUser().getObjectId().equals(ParseUser.getCurrentUser().getObjectId())) {
                        Toast.makeText(mContext, R.string.cannot_save, Toast.LENGTH_SHORT).show();
                    }
                    return super.onDoubleTap(event);
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

    private void unSaveSavedTrip(Trip trip, ViewHolder viewHolder) {
        ParseQuery<SavedTrips> query = ParseQuery.getQuery(SavedTrips.class);
        String savedTripId = mTripToSavedTrip.get(trip.getObjectId());
        query.getInBackground(savedTripId, ((object, exception) -> {
            if(exception == null) {
                object.deleteInBackground(deleteException -> {
                    if(deleteException == null) {
                        Toast.makeText(mContext, R.string.unsave_trip, Toast.LENGTH_SHORT).show();
                        if(trip.getSaveCount() > 0) trip.setSaveCount(trip.getSaveCount() - 1);
                        trip.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                viewHolder.mSaveButton.setImageResource(R.drawable.unsaved);
                                viewHolder.mSaveButton.setTag(R.drawable.unsaved);
                                setTripToSavedTrip();

                            }
                        });
                    }
                });
            }
        }));
    }

    private void saveSavedTrip(Trip trip, ViewHolder viewHolder) {
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
                setTripToSavedTrip();
                trip.setSaveCount(trip.getSaveCount() + 1);
                trip.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException exception) {
                        if(exception != null) {
                            Log.e(TAG, "Error saving trip", exception);
                            return;
                        }
                        viewHolder.mSaveButton.setImageResource(R.drawable.saved);
                        viewHolder.mSaveButton.setTag(R.drawable.saved);

                    }
                });
            }
        });

    }

    private void setTripToSavedTrip() {
        ParseQuery<SavedTrips> query = ParseQuery.getQuery(SavedTrips.class);
        query.include(Trip.KEY_USER);
        query.whereEqualTo(Trip.KEY_USER, ParseUser.getCurrentUser());
        query.findInBackground(new FindCallback<SavedTrips>() {
            @Override
            public void done(List<SavedTrips> savedTrips, ParseException exception) {
                if(exception != null) {
                    Log.e(TAG, "Issue with getting saved trips", exception);
                }
                for(SavedTrips trip : savedTrips) {
                    mTripToSavedTrip.put(trip.getTrip().getObjectId(), trip.getObjectId());
                }
                Log.i(TAG, "trip to saved trip id: " + mTripToSavedTrip);
            }
        });
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Trip trip = mTrips.get(position);
        if(mAllSavedTrips.contains(trip.getObjectId())) {
            holder.mSaveButton.setImageResource(R.drawable.saved);
            holder.mSaveButton.setTag(R.drawable.saved);
        } else {
            holder.mSaveButton.setImageResource(R.drawable.unsaved);
            holder.mSaveButton.setTag(R.drawable.unsaved);
        }
        holder.mRootView.setTag(trip);
        try {
            holder.mTvTripName.setText(trip.fetchIfNeeded().getString(Trip.KEY_TITLE));
        } catch (ParseException exception) {
            exception.printStackTrace();
        }
        ParseFile image = trip.getImage();
        Glide.with(mContext).load(image.getUrl()).into(holder.mIvTripPic);
        onTripClicked(holder);
    }

    @Override
    public int getItemCount() {
        return mTrips.size();
    }

    // Clean all elements of the recycler
    public void clear() {
        mAllSavedTrips.clear();
        mTrips.clear();
        notifyDataSetChanged();
    }

    // Add a list of items -- change to type used
    public void addAll(List<Trip> list, List<String> savedTrips) {
        mAllSavedTrips.addAll(savedTrips);
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
        private TextView mTvTripName;
        private ImageView mSaveButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mRootView = itemView;
            mIvTripPic = itemView.findViewById(R.id.ivTripPic);
            mTvTripName = itemView.findViewById(R.id.tvTripName);
            mSaveButton = itemView.findViewById(R.id.ibSave);
        }

    }
}
