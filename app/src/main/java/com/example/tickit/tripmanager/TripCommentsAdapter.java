package com.example.tickit.tripmanager;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tickit.R;
import com.example.tickit.models.TripComments;
import com.parse.ParseException;

import java.util.List;

public class TripCommentsAdapter extends RecyclerView.Adapter<TripCommentsAdapter.ViewHolder> {

    private Context mContext;
    private List<TripComments> mTripComments;

    public TripCommentsAdapter(Context mContext, List<TripComments> mTripComments) {
        this.mContext = mContext;
        this.mTripComments = mTripComments;
    }

    @NonNull
    @Override
    public TripCommentsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_comment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TripCommentsAdapter.ViewHolder holder, int position) {
        TripComments comment = mTripComments.get(position);
        try {
            holder.bind(comment);
        } catch (ParseException exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return mTripComments.size();
    }

    public void clear() {
        mTripComments.clear();
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private TextView tvUsername;
        private TextView tvComment;
        private TextView tvCommentCreatedAt;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvComment = itemView.findViewById(R.id.tvComment);
            tvCommentCreatedAt = itemView.findViewById(R.id.tvCommentCreatedAt);
        }

        public void bind(TripComments comment) throws ParseException {
            tvUsername.setText(comment.getUser().fetchIfNeeded().getUsername());
            tvComment.setText(comment.getComment());
            tvCommentCreatedAt.setText(TripComments.calculateTimeAgo(comment.getCreatedAt()));
        }
    }
}
