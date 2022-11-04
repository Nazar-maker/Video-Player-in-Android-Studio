package com.example.videoplayer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;

import java.util.ArrayList;
import java.util.Locale;

public class VideoFilesActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {
    public static final String MY_PREF = "my_pref";
    RecyclerView recyclerView;
    private ArrayList<MediaFiles> videoFilesArrayList = new ArrayList<>();
    static VideoFilesAdapter videoFilesAdapter;
    String folder_name;
    SwipeRefreshLayout swipeRefreshLayout;
    String sortOrder;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        folder_name = getIntent().getStringExtra("folderName");
        getSupportActionBar().setTitle(folder_name);
        setContentView(R.layout.activity_video_files);
        recyclerView = findViewById(R.id.videos_rv);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_videos);
        SharedPreferences.Editor editor = getSharedPreferences(MY_PREF, MODE_PRIVATE).edit();
        editor.putString("playlistFolderName", folder_name);
        editor.apply();

        showVideoFiles();
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                showVideoFiles();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void showVideoFiles() {
        videoFilesArrayList = fetchMedia(folder_name);
        videoFilesAdapter = new VideoFilesAdapter(videoFilesArrayList, this, 0);
        recyclerView.setAdapter(videoFilesAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        videoFilesAdapter.notifyDataSetChanged();
    }


    private ArrayList<MediaFiles> fetchMedia(String folderName) {
        SharedPreferences preferences = getSharedPreferences(MY_PREF, MODE_PRIVATE);
        String sort_value = preferences.getString("sort", "abcd");

        ArrayList<MediaFiles> videoFiles = new ArrayList<>();
        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        if (sort_value.equals("sortName")) {
            if (sort_value.equals("ascending")) {
                sortOrder = MediaStore.MediaColumns.DISPLAY_NAME+" ASC";
            } else if (sort_value.equals("descending")){
                sortOrder = MediaStore.MediaColumns.DISPLAY_NAME+" DESC";
            }
            sortOrder = MediaStore.MediaColumns.DISPLAY_NAME+" ASC";
        } else if (sort_value.equals("sortSize")) {
            if (sort_value.equals("ascending")) {
                sortOrder = MediaStore.MediaColumns.SIZE+" ASC";
            } else if (sort_value.equals("descending")) {
                sortOrder = MediaStore.MediaColumns.SIZE+" DESC";
            }
        } else if (sort_value.equals("sortDate")) {
            if (sort_value.equals("ascending")) {
                sortOrder = MediaStore.MediaColumns.DATE_ADDED+" ASC";
            } else if (sort_value.equals("descending")) {
                sortOrder = MediaStore.MediaColumns.DATE_ADDED+" DESC";
            }
        } else {
            if (sort_value.equals("ascending")) {
                sortOrder = MediaStore.Video.Media.DURATION+" ASC";
            } else if (sort_value.equals("descending")) {
                sortOrder = MediaStore.Video.Media.DURATION+" DESC";
            }

        }
        String selection = MediaStore.Video.Media.DATA+" like?";
        String[] selectionArg = new String[]{"%"+folderName+"%"};
        Cursor cursor = getContentResolver().query(uri, null, selection, selectionArg, sortOrder);
        if (cursor != null && cursor.moveToNext()) {
            do {
                @SuppressLint("Range") String id = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media._ID));
                @SuppressLint("Range") String title = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.TITLE));
                @SuppressLint("Range") String displayName = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME));
                @SuppressLint("Range") String size = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.SIZE));
                @SuppressLint("Range") String duration = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DURATION));
                @SuppressLint("Range") String path = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA));
                @SuppressLint("Range") String dateAdded = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATE_ADDED));
                MediaFiles mediaFiles = new MediaFiles(id, title, displayName, size, duration, path,dateAdded);
                videoFiles.add(mediaFiles);
            }while (cursor.moveToNext());
        }
        return videoFiles;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.video_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.search_video);
        SearchView searchView =  (SearchView) menuItem.getActionView();
        searchView.setOnQueryTextListener(this);
        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        SharedPreferences preferences = getSharedPreferences(MY_PREF, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        final String[] option = new String[1];
        int id = item.getItemId();
        switch (id) {
            case R.id.refresh_files:
                finish();
                startActivity(getIntent());
                break;
            case R.id.sort_by:
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
                AlertDialog.Builder alertDialog1 = new AlertDialog.Builder(this);
                alertDialog.setTitle("Sort By");

                alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (option[0].equals("Name")) alertDialog1.setTitle("Name");
                        else if (option[0].equals("Size")) alertDialog1.setTitle("Size");
                        else if (option[0].equals("Date")) alertDialog1.setTitle("Date");
                        else if (option[0].equals("Length")) alertDialog1.setTitle("Length");
                        alertDialog1.create().show();
                    }
                });
                alertDialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });

                alertDialog1.setPositiveButton("DONE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        editor.apply();
                        finish();
                        startActivity(getIntent());
                        dialogInterface.dismiss();
                    }
                });
                alertDialog1.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });

                String[] items = {"Name", "Size", "Date", "Length"};
                String[] mode = {"Ascending", "Descending"};
                alertDialog.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switch (i) {
                            case 0:
                                editor.putString("sort", "sortName");
                                option[0] = "Name";
                                break;
                            case 1:
                                editor.putString("sort", "sortSize");
                                option[0] = "Size";
                                break;
                            case 2:
                                editor.putString("sort", "sortDate");
                                option[0] = "Date";
                                break;
                            case 3:
                                editor.putString("sort", "sortLength");
                                option[0] = "Length";
                                break;

                        }
                    }
                });

                alertDialog1.setSingleChoiceItems(mode, -1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switch (i){
                            case 0:
                                editor.putString("sort", "ascending");
                                break;
                            case 1:
                                editor.putString("sort", "descending");
                        }
                    }
                });
                alertDialog.create().show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onQueryTextSubmit(String s) {

        return false;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        String inputs = s.toLowerCase();
        ArrayList<MediaFiles> mediaFiles = new ArrayList<>();
        for(MediaFiles media: videoFilesArrayList) {
            if (media.getTitle().toLowerCase().contains(inputs)) {
                mediaFiles.add(media);
            }
        }
        VideoFilesActivity.videoFilesAdapter.updateVideoFiles(mediaFiles);
        return true;
    }
}