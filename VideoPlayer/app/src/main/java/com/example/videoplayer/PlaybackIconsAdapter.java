package com.example.videoplayer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class PlaybackIconsAdapter extends RecyclerView.Adapter<PlaybackIconsAdapter.ViewHolder> {
    private ArrayList<IconModel> iconModelList;
    private Context context;
    OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }
    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public PlaybackIconsAdapter(ArrayList<IconModel> iconModelList, Context context) {
        this.iconModelList = iconModelList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view =  LayoutInflater.from(context).inflate(R.layout.icons_layout, parent, false);
        return new ViewHolder(view, mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.icon.setImageResource(iconModelList.get(position).getImageView());
        holder.iconName.setText((iconModelList.get(position).getIconTitle()));
    }

    @Override
    public int getItemCount() {
        return iconModelList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView iconName;
        ImageView icon;
        public ViewHolder(@NonNull View itemView, OnItemClickListener listener) {
            super(itemView);
            icon =  itemView.findViewById(R.id.playback_icon);
            iconName = itemView.findViewById(R.id.icon_title);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onItemClick(position);
                        }
                    }
                }
            });
        }
    }
}
