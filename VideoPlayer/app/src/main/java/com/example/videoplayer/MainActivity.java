package com.example.videoplayer;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private final ArrayList<String> allFolderList = new ArrayList<>();
    private RecyclerView recyclerView;
    private TextView noOfFolders;
    SwipeRefreshLayout swipeRefreshLayout;
    int number = 0;

    @SuppressLint("SetTextI18n")
    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Folders");
        recyclerView = findViewById(R.id.RVFolders);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_folders);
        noOfFolders = findViewById(R.id.noOfFolders);

//        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
//            startActivity(new Intent(MainActivity.this, AllowAccessActivity.class));
//            finish();
//        } else {
        showFolders();
        if (number == 1) noOfFolders.setText("----- " + number + " folder  -----");
        else noOfFolders.setText("-----  " + number + " folders  -----");

        swipeRefreshLayout.setOnRefreshListener(() -> {
            showFolders();
            if (number == 1) noOfFolders.setText("----- " + number + " folder  -----");
            else noOfFolders.setText("-----  " + number + " folders  -----");
            swipeRefreshLayout.setRefreshing(false);
        });
    }


    @SuppressLint("NotifyDataSetChanged")
    private void showFolders() {
        ArrayList<MediaFiles> mediaFiles = fetchMedia();
        VideoFoldersAdapter adapter = new VideoFoldersAdapter(mediaFiles, allFolderList, this);
        number = adapter.getItemCount();
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        adapter.notifyDataSetChanged();
    }

    private ArrayList<MediaFiles> fetchMedia() {
        ArrayList<MediaFiles> mediaFilesArrayList = new ArrayList<>();
        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;

        @SuppressLint("Recycle") Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        if (cursor != null && cursor.moveToNext()) {
            do {
                @SuppressLint("Range") String id = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media._ID));
                @SuppressLint("Range") String title = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.TITLE));
                @SuppressLint("Range") String displayName = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME));
                @SuppressLint("Range") String size = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.SIZE));
                @SuppressLint("Range") String duration = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DURATION));
                @SuppressLint("Range") String path = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA));
                @SuppressLint("Range") String dateAdded = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATE_ADDED));
                MediaFiles mediaFiles = new MediaFiles(id, title, displayName, size, duration, path, dateAdded);
                int index = path.lastIndexOf("/");
                String substring = path.substring(0, index);

                if (!allFolderList.contains(substring)) {
                    allFolderList.add(substring);
                }
                mediaFilesArrayList.add(mediaFiles);
            } while (cursor.moveToNext());
        }

        return mediaFilesArrayList;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.folder_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.rateUs:
                Uri uri = Uri.parse("https://play.google.com/store/apps/details?id="
                        + getApplicationContext().getPackageName());
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
                break;
            case R.id.refresh_folder:
                finish();
                startActivity(getIntent());
                break;
            case R.id.share_app:
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_TEXT, "Check this app via\n" +
                        "https://play.google.com/store/apps/details?id="
                        + getApplicationContext().getPackageName());
                shareIntent.setType("text/plain");
                startActivity(Intent.createChooser(shareIntent, "Share app via"));
                break;
        }
        return super.onOptionsItemSelected(item);
    }



    boolean doubleBackToExitPressedOnce = false;

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 1000);
    }

}
//    private void getVideos() {
//        ContentResolver contentResolver = getContentResolver();
//        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
//
//        Cursor cursor = contentResolver.query(uri, null, null, null, null);
//        if(cursor!=null && cursor.moveToNext()) {
//            do {
//                @SuppressLint("Range") String videoTitle = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.TITLE));
//                @SuppressLint("Range") String videoPath = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA));
//                Bitmap videoThumbnail = ThumbnailUtils.createVideoThumbnail(videoPath, MediaStore.Images.Thumbnails.MINI_KIND);
//
//                videoRVModalArrayList.add(new VideoRVModal(videoTitle, videoPath, videoThumbnail));
//            }while(cursor.moveToNext());
//        }
//
//        videoRVAdapter.notifyDataSetChanged();
//    }

//    @Override
//    public void onVideoClick(int position) {
//        Intent i = new Intent(MainActivity.this, VideoPlayerActivity.class);
//        i.putExtra("videoName", videoRVModalArrayList.get(position).getVideoName());
//        i.putExtra("videoPath", videoRVModalArrayList.get(position).getVideoPath());
//        startActivity(i);
//    }
