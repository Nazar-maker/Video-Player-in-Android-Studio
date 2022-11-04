package com.example.videoplayer;

import android.content.Context;
import android.content.Intent;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;

public class VideoFoldersAdapter extends RecyclerView.Adapter<VideoFoldersAdapter.ViewHolder> {
    private ArrayList<MediaFiles> mediaFiles;
    private ArrayList<String> folderPath;
    private Context context;

    public VideoFoldersAdapter(ArrayList<MediaFiles> mediaFiles, ArrayList<String> folderPath, Context context) {
        this.mediaFiles = mediaFiles;
        this.folderPath = folderPath;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.folder_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        int indexPath = folderPath.get(position).lastIndexOf("/");
        String nameOfFolder = folderPath.get(position).substring(indexPath+1);
        holder.folderName.setText(nameOfFolder);
        holder.folder_path.setText(folderPath.get(position));

        int numOfFiles = numOfFiles(folderPath.get(position));
        if (numOfFiles == 1) holder.numberOfFiles.setText(numOfFiles + " video");
        else holder.numberOfFiles.setText(numOfFiles + " videos");


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, VideoFilesActivity.class);
                intent.putExtra("folderName", nameOfFolder);
                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return folderPath.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView folderName;
        TextView folder_path;
        TextView numberOfFiles;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            folderName = itemView.findViewById(R.id.folderName);
            folder_path = itemView.findViewById(R.id.folderPath);
            numberOfFiles = itemView.findViewById(R.id.noOfFiles);
        }
    }

    int numOfFiles(String folder_name) {
        int files_num = 0;
        for (MediaFiles mediaFiles: mediaFiles) {
            if (mediaFiles.getPath().substring(0, mediaFiles.getPath().lastIndexOf("/"))
            .endsWith(folder_name)) {
                files_num++;
            }
        }
        return files_num;
    }
}
