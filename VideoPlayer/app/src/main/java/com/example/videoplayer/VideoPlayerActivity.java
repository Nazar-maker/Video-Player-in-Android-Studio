package com.example.videoplayer;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.compose.ui.window.DialogProperties;
import androidx.core.app.ActivityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ReportFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.PictureInPictureParams;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Camera;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.SessionConfiguration;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.AudioManager;
import android.media.ImageReader;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.PlaybackParams;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.Settings;
import android.service.controls.Control;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Rational;
import android.util.Size;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

//import com.example.videoplayer.ml.Model;

import com.github.angads25.filepicker.controller.DialogSelectionListener;
import com.github.angads25.filepicker.model.DialogConfigs;
import com.github.angads25.filepicker.view.FilePickerDialog;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.MergingMediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.SingleSampleMediaSource;
import com.google.android.exoplayer2.source.hls.DefaultHlsDataSourceFactory;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.DefaultTimeBar;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.MimeTypes;
import com.google.android.exoplayer2.util.Util;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.SimpleTimeZone;
import java.util.concurrent.Semaphore;

public class VideoPlayerActivity extends AppCompatActivity implements View.OnClickListener {
    ArrayList<MediaFiles> mVideoFiles = new ArrayList<>();
    PlayerView playerView;
    SimpleExoPlayer player;
    int position;
    String video_title;
    TextView title;
    private ControlsMode controlsMode;
    public enum ControlsMode{
        LOCK, FULLSCREEN;
    }

    ImageView videoBack, lock, unlock, scaling, videoList;
    VideoFilesAdapter videoFilesAdapter;
    RelativeLayout root;
    ImageView nextButton, previousButton;
    // horizontal recyclerview variables
    private ArrayList<IconModel> iconModelArrayList = new ArrayList<>();
    PlaybackIconsAdapter playbackIconsAdapter;
    RecyclerView recyclerViewIcons;
    boolean expand = false;
    View nightMode;
    boolean dark = false;
    boolean mute = false;
    PlaybackParameters parameters;
    float speed;
    com.github.angads25.filepicker.model.DialogProperties dialogProperties;
    FilePickerDialog filePickerDialog;
    Uri uriSubtitle;
    PictureInPictureParams.Builder pictureInPicture;
    boolean isCrossChecked;
    // horizontal recyclerview variables

    // swipe and zoom variables
    private int device_height, device_width, brightness, media_volume;
    boolean start = false;
    boolean left, right;
    private float baseX, baseY;
    boolean swipe_move = false;
    private long diffX, diffY;
    public static final int MINIMUM_DISTANCE = 100;
    boolean success = false;
    TextView vol_text, brt_text;
    ProgressBar vol_progress, brt_progress;
    LinearLayout vol_progress_container, vol_text_container, brt_progress_container, brt_text_container;
    ImageView vol_icon, brt_icon;
    AudioManager audioManager;
    MediaPlayer mediaPlayer;
    private ContentResolver contentResolver;
    private Window window;
    boolean singleTap = false;

    // swipe and zoom variables
    ConcatenatingMediaSource concatenatingMediaSource;
    Handler backgroundHandler;
    private HandlerThread backgroundThread;
    private static final int CAMERA_PERMISSION = 1122;
    TextView totalDuration;



    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setFullScreen();
        setContentView(R.layout.activity_video_player);

        playerView = findViewById(R.id.exoplayer_view);
        title = findViewById(R.id.video_title);
        nextButton = findViewById(R.id.exo_next);
        previousButton = findViewById(R.id.exo_prev);
        totalDuration = findViewById(R.id.exo_duration);
        videoBack = findViewById(R.id.video_back);
        lock = findViewById(R.id.lock);
        unlock = findViewById(R.id.unlock);
        root = findViewById(R.id.root_layout);
        scaling = findViewById(R.id.scaling);
        recyclerViewIcons = findViewById(R.id.recyclerview_icons);
        nightMode = findViewById(R.id.night_mode);
        videoList = findViewById(R.id.video_list);
        vol_text = findViewById(R.id.vol_text);
        brt_text = findViewById(R.id.brt_text);
        vol_progress = findViewById(R.id.vol_progress);
        brt_progress = findViewById(R.id.brt_progress);
        vol_progress_container = findViewById(R.id.vol_progress_container);
        brt_progress_container = findViewById(R.id.brt_progress_container);
        vol_text_container = findViewById(R.id.vol_text_container);
        brt_text_container = findViewById(R.id.brt_text_container);
        vol_icon = findViewById(R.id.vol_icon);
        brt_icon = findViewById(R.id.brt_icon);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        position = getIntent().getIntExtra("position", 1);
        video_title = getIntent().getStringExtra("video_title");
        mVideoFiles = getIntent().getExtras().getParcelableArrayList("videoArrayList");
        screenOrientation();
        title.setText(video_title);
        nextButton.setOnClickListener(this);
        previousButton.setOnClickListener(this);
        double milliseconds = Double.parseDouble(mVideoFiles.get(position).getDuration());
        totalDuration.setText(Utility.convertTime((long) milliseconds));
        videoBack.setOnClickListener(this);
        lock.setOnClickListener(this);
        unlock.setOnClickListener(this);
        videoList.setOnClickListener(this);
        scaling.setOnClickListener(firstListener);

        dialogProperties = new com.github.angads25.filepicker.model.DialogProperties();
        filePickerDialog = new FilePickerDialog(VideoPlayerActivity.this);
        filePickerDialog.setTitle("Select a Subtitle File");
        filePickerDialog.setPositiveBtnName("OK");
        filePickerDialog.setNegativeBtnName("Cancel");

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            pictureInPicture = new PictureInPictureParams.Builder();
        }

        iconModelArrayList.add(new IconModel(R.drawable.ic_right, ""));

        playbackIconsAdapter = new PlaybackIconsAdapter(iconModelArrayList, this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, RecyclerView.HORIZONTAL, true);
        recyclerViewIcons.setLayoutManager(layoutManager);
        recyclerViewIcons.setAdapter(playbackIconsAdapter);
        playbackIconsAdapter.notifyDataSetChanged();
        playbackIconsAdapter.setOnItemClickListener(new PlaybackIconsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                if (position == 0) {
                    if(expand) {

                        iconModelArrayList.clear();
                        iconModelArrayList.add(new IconModel(R.drawable.ic_right, ""));
//                        iconModelArrayList.add(new IconModel(R.drawable.ic_night_mode, "Night"));
//                        iconModelArrayList.add(new IconModel(R.drawable.ic_volume_off, "Mute"));
//                        iconModelArrayList.add(new IconModel(R.drawable.ic_rotate, "Rotate"));
                        playbackIconsAdapter.notifyDataSetChanged();
                        expand = false;
                    } else {
                        if(iconModelArrayList.size() == 1) {
                            iconModelArrayList.add(new IconModel(R.drawable.ic_night_mode, "Night"));
                            iconModelArrayList.add(new IconModel(R.drawable.ic_volume_off, "Mute"));
                            iconModelArrayList.add(new IconModel(R.drawable.ic_rotate, "Rotate"));
                            iconModelArrayList.add(new IconModel(R.drawable.ic_speed, "Speed"));
                            iconModelArrayList.add(new IconModel(R.drawable.ic_subtitle, "Subtitle"));
                            iconModelArrayList.add(new IconModel(R.drawable.ic_pip_mode, "Popup"));
                        }
                        iconModelArrayList.set(position, new IconModel(R.drawable.ic_left, ""));
                        playbackIconsAdapter.notifyDataSetChanged();
                        expand = true;
                    }
                }
                if (position == 1) {
                    if(dark) {
                        nightMode.setVisibility(View.GONE);
                        iconModelArrayList.set(position, new IconModel(R.drawable.ic_night_mode, "Night"));
                        playbackIconsAdapter.notifyDataSetChanged();
                        dark = false;
                    } else {
                        nightMode.setVisibility(View.VISIBLE);

                        iconModelArrayList.set(position, new IconModel(R.drawable.ic_night_mode, "Day"));
                        playbackIconsAdapter.notifyDataSetChanged();
                        dark = true;
                    }
                }
                if (position == 2) {
                    if(mute) {
                        player.setVolume(100);
                        iconModelArrayList.set(position, new IconModel(R.drawable.ic_volume_off, "Mute"));
                        playbackIconsAdapter.notifyDataSetChanged();
                        mute = false;
                    } else {
                        player.setVolume(0);
                        iconModelArrayList.set(position, new IconModel(R.drawable.ic_volume, "Unmute"));
                        playbackIconsAdapter.notifyDataSetChanged();
                        mute = true;
                    }
                }
                if (position == 3) {
                    if (getResources().getConfiguration().orientation ==  Configuration.ORIENTATION_PORTRAIT) {
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                        playbackIconsAdapter.notifyDataSetChanged();
                    } else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                        playbackIconsAdapter.notifyDataSetChanged();
                    }
                }
                if (position == 4) {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(VideoPlayerActivity.this);
                    alertDialog.setTitle("Select Playback Speed").setPositiveButton("OK", null);
                    String[] items = {"0.5x", "1x normal speed", "1.25x", "1.5x", "2x"};
                    int checkedItem = -1;
                    alertDialog.setSingleChoiceItems(items, checkedItem, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case 0:
                                    speed = 0.5f;
                                    parameters = new PlaybackParameters(speed);
                                    player.setPlaybackParameters(parameters);
                                    break;
                                case 1:
                                    speed = 1f;
                                    parameters = new PlaybackParameters(speed);
                                    player.setPlaybackParameters(parameters);
                                    break;
                                case 2:
                                    speed = 1.25f;
                                    parameters = new PlaybackParameters(speed);
                                    player.setPlaybackParameters(parameters);
                                    break;
                                case 3:
                                    speed = 1.5f;
                                    parameters = new PlaybackParameters(speed);
                                    player.setPlaybackParameters(parameters);
                                    break;
                                case 4:
                                    speed = 2f;
                                    parameters = new PlaybackParameters(speed);
                                    player.setPlaybackParameters(parameters);
                                    break;
                                default:
                                    break;
                            }
                        }
                    });

                    AlertDialog alert = alertDialog.create();
                    alert.show();
                }

                if (position == 5) {
                    dialogProperties.selection_mode = DialogConfigs.SINGLE_MODE;
                    dialogProperties.extensions = new String[]{".srt"};
                    dialogProperties.root = new File("/storage/emulated/0");
                    filePickerDialog.setProperties(dialogProperties);
                    filePickerDialog.show();
                    filePickerDialog.setDialogSelectionListener(new DialogSelectionListener() {
                        @Override
                        public void onSelectedFilePaths(String[] files) {
                            for (String path: files) {
                                File file = new File(path);
                                uriSubtitle = Uri.parse(file.getAbsolutePath().toString());

                            }
                            playVideoSubtitle(uriSubtitle);
                        }
                    });
                }

                if(position == 6) {
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        Rational aspectRatio = new Rational(16,9);
                        pictureInPicture.setAspectRatio(aspectRatio);
                        enterPictureInPictureMode(pictureInPicture.build());
                    } else {
                        Log.wtf("Not Oreo", "yes");
                    }
                }
            }
        });

        AudioManager.OnAudioFocusChangeListener listener = new AudioManager.OnAudioFocusChangeListener() {
            @Override
            public void onAudioFocusChange(int focusChange) {
                if(focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT)
                {
                    // Pause
                }
                else if(focusChange == AudioManager.AUDIOFOCUS_GAIN)
                {
                    // Resume
                }
                else if(focusChange == AudioManager.AUDIOFOCUS_LOSS)
                {
                    // Stop or pause depending on your need
                }
            }
        };
        int result = audioManager.requestAudioFocus(listener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            playVideo();
        }
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        device_width = displayMetrics.widthPixels;
        device_height = displayMetrics.heightPixels;

        playerView.setOnTouchListener(new OnSwipeTouchListener(this) {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
//                        playerView.showController();
                        start = true;
                        if(event.getX() < (device_width / 2)) {
                            left = true;
                            right = false;
                        } else if (event.getX() > (device_width / 2)) {
                            left = false;
                            right = true;
                        }
                        baseX = event.getX();
                        baseY =  event.getY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        swipe_move = true;
                        diffX = (long) Math.ceil(event.getX() - baseX);
                        diffY = (long) Math.ceil(event.getY() - baseY);
                        double brightnessSpeed = 0.01;
                        if (Math.abs(diffY) > MINIMUM_DISTANCE) {
                            start = true;
                            if(Math.abs(diffY) > Math.abs(diffX)) {
                                boolean value;
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    value = Settings.System.canWrite(getApplicationContext());
                                    if(value) {
                                        if (left) {
                                            contentResolver = getContentResolver();
                                            window = getWindow();
                                            try {
                                                Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE,
                                                        Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
                                                brightness = Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS);

                                            } catch (Settings.SettingNotFoundException e) {
                                                e.printStackTrace();
                                            }
                                            int new_brightness = (int) (brightness - (diffY*brightnessSpeed));
                                            if (new_brightness > 250) {
                                                new_brightness = 250;
                                            } else if (new_brightness < 1){
                                                new_brightness = 1;
                                            }

                                            double brt_percentage = Math.ceil((((double) new_brightness/(double) 250) * (double) 100));
                                            brt_progress_container.setVisibility(View.VISIBLE);
                                            brt_text_container.setVisibility(View.VISIBLE);
                                            brt_progress.setProgress((int) brt_percentage);

                                            if (brt_percentage < 30) {
                                                brt_icon.setImageResource(R.drawable.ic_brightness_low);
                                            } else if (brt_percentage > 30 && brt_percentage < 80) {
                                                brt_icon.setImageResource(R.drawable.ic_brightness_moderate);
                                            } else if (brt_percentage > 80) {
                                                brt_icon.setImageResource(R.drawable.ic_brightness);
                                            }

                                            brt_text.setText(" " + (int) brt_percentage + "%");
                                            Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS,
                                                    (new_brightness));
                                            WindowManager.LayoutParams layoutParams = window.getAttributes();
                                            layoutParams.screenBrightness = brightness/(float) 255;
                                            window.setAttributes(layoutParams);

                                        } else if (right) {
                                            vol_text_container.setVisibility(View.VISIBLE);
                                            media_volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                                            int maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                                            double cal = (double) diffY*((double) maxVol/ ((double) (device_height*1)-brightnessSpeed));
                                            int newMediaVolume = media_volume - (int) cal;
                                            if (newMediaVolume > maxVol) {
                                                newMediaVolume = maxVol;
                                            } else if (newMediaVolume < 1) {
                                                newMediaVolume = 0;
                                            }
                                            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                                                    newMediaVolume, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                                            double volPer = Math.ceil((((double) newMediaVolume / (double) maxVol)* (double) 100));
                                            vol_text.setText(" " + (int) volPer + "%");
                                            if(volPer < 1) {
                                                vol_icon.setImageResource(R.drawable.ic_volume_off);
                                                vol_text.setVisibility(View.VISIBLE);
                                                vol_text.setText("Off");
                                            } else if (volPer >= 1) {
                                                vol_icon.setImageResource(R.drawable.ic_volume);
                                                vol_text.setVisibility(View.VISIBLE);
                                            }
                                            vol_progress_container.setVisibility(View.VISIBLE);
                                            vol_progress.setProgress((int) volPer);
                                        }
                                        success = true;
                                    } else {
                                        Toast.makeText(VideoPlayerActivity.this, "Allow write settings for swipe controls", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                                        intent.setData(Uri.parse("package:" + getPackageName()));
                                        activityResultLaunch.launch(intent);
//                                        startActivityForResult(intent, 111);
                                    }
                                }
                            }
                        }

                        break;
                    case MotionEvent.ACTION_UP:
                        swipe_move = false;
                        start = false;
                        vol_progress_container.setVisibility(View.GONE);
                        brt_progress_container.setVisibility(View.GONE);
                        vol_text_container.setVisibility(View.GONE);
                        brt_text_container.setVisibility(View.GONE);
                        break;
                }
                return super.onTouch(v, event);
            }

            @Override
            public void onDoubleTouch() {
                super.onDoubleTouch();
            }

            @Override
            public void onSingleTouch() {
                super.onSingleTouch();
                if (singleTap) {
                    playerView.showController();
                    singleTap = false;
                } else {
                    playerView.hideController();
                    singleTap = true;
                }
            }
        });
    }
    ActivityResultLauncher<Intent> activityResultLaunch = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        boolean value;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            value = Settings.System.canWrite(getApplicationContext());
                            if(value) {
                                success = true;
                            } else {
                                Toast.makeText(getApplicationContext(), "Not granted", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
            });

    private void startBackgroundThread(SimpleExoPlayer player) {
        backgroundThread = new HandlerThread("Camera");
        backgroundThread.start();
        backgroundHandler = new Handler();
        backgroundHandler.post(new Runnable() {
            @Override
            public void run() {
                getSupportFragmentManager()
                        .beginTransaction()
                        .add(R.id.RLcamera, CameraFragment.newInstance(player), "Current_fragment")
                        .commit();
            }
        });
    }

    private void stopBackgroundThread() {
        backgroundThread.quitSafely();
        try {
            backgroundThread.join();
            backgroundThread = null;
            backgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        getSupportFragmentManager()
                .beginTransaction()
                .remove(getSupportFragmentManager().findFragmentByTag("Current_fragment"))
                .commit();
    }


    private void playVideo() {

        String path = mVideoFiles.get(position).getPath();
        Uri uri = Uri.parse(path);
        player = new SimpleExoPlayer.Builder(this).build();
        DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(this, Util.getUserAgent(this, "app"));
        concatenatingMediaSource = new ConcatenatingMediaSource();
        for(int i=0; i<mVideoFiles.size(); i++) {
            new File(String.valueOf(mVideoFiles.get(i)));
            MediaSource mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(Uri.parse(String.valueOf(uri)));
            concatenatingMediaSource.addMediaSource(mediaSource);
        }
        playerView.setPlayer(player);
        playerView.setKeepScreenOn(true);
        player.setPlaybackParameters(parameters);
        player.prepare(concatenatingMediaSource);
        player.seekTo(position, C.TIME_UNSET);
        playError();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION);
        } else {
                startBackgroundThread(player);
        }
    }

    private void playVideoSubtitle(Uri subtitle) {
        long oldPosition =  player.getCurrentPosition();
        player.stop();
        stopBackgroundThread();
        String path = mVideoFiles.get(position).getPath();
        Uri uri = Uri.parse(path);
        player = new SimpleExoPlayer.Builder(this).build();
        DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(this, Util.getUserAgent(this, "app"));
        concatenatingMediaSource = new ConcatenatingMediaSource();
        for(int i=0; i<mVideoFiles.size(); i++) {
            new File(String.valueOf(mVideoFiles.get(i)));
            MediaSource mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(Uri.parse(String.valueOf(uri)));
            Format textFormat = Format.createTextSampleFormat(null, MimeTypes.APPLICATION_SUBRIP,Format.NO_VALUE, "app");
            MediaSource subtitleSource = new SingleSampleMediaSource.Factory(dataSourceFactory).setTreatLoadErrorsAsEndOfStream(true)
                    .createMediaSource(Uri.parse(String.valueOf(subtitle)), textFormat, C.TIME_UNSET);
            MergingMediaSource mergingMediaSource = new MergingMediaSource(mediaSource, subtitleSource);
            concatenatingMediaSource.addMediaSource(mergingMediaSource);
            concatenatingMediaSource.addMediaSource(mediaSource);
        }
        playerView.setPlayer(player);
        playerView.setKeepScreenOn(true);
        player.setPlaybackParameters(parameters);
        player.prepare(concatenatingMediaSource);
        player.seekTo(position, oldPosition);
        playError();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION);
        } else {
            startBackgroundThread(player);
        }
    }
    private void screenOrientation() {
        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            Bitmap bitmap;
            String path = mVideoFiles.get(position).getPath();
            Uri uri = Uri.parse(path);
            retriever.setDataSource(this, uri);
            bitmap = retriever.getFrameAtTime();

            int videoWidth = bitmap.getWidth();
            int videoHeight = bitmap.getHeight();

            if(videoHeight < videoWidth) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            } else {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        } catch (Exception e) {
            Log.e("MediaMetaDataRetriever", "screenOrientation: ");
        }
    }

    private void playError() {
        player.addListener(new Player.EventListener() {
            @Override
            public void onPlayerError(ExoPlaybackException error) {
                Toast.makeText(VideoPlayerActivity.this, "Video Playing Error", Toast.LENGTH_SHORT).show();
            }
        });
        player.setPlayWhenReady(true);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(player.isPlaying()) {
            player.stop();
        }
        stopBackgroundThread();

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onPause() {
        super.onPause();
        player.setPlayWhenReady(false);
        player.getPlaybackState();
        if(isInPictureInPictureMode()) {
            player.setPlayWhenReady(true);
        } else {
            player.setPlayWhenReady(false);
            player.getPlaybackState();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        player.setPlayWhenReady(true);
        player.getPlaybackState();
        setFullScreen();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @SuppressLint("SetTextI18n")
    @Override
    protected void onResume() {
        super.onResume();
        player.setPlayWhenReady(true);
        player.getPlaybackState();
    }


    private void setFullScreen() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == CAMERA_PERMISSION) {
            if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startBackgroundThread(player);
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(this, "Hand Gestures won't work without camera, please change camera permission from settings", Toast.LENGTH_LONG).show();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.video_back:
                if (player != null) {
                    player.release();
                }
                finish();
                break;
            case R.id.video_list:
                PlaylistDialog playlistDialog = new PlaylistDialog(mVideoFiles, videoFilesAdapter);
                playlistDialog.show(getSupportFragmentManager(),playlistDialog.getTag());
                break;
            case R.id.lock:
                controlsMode = ControlsMode.FULLSCREEN;
                root.setVisibility(View.VISIBLE);
                lock.setVisibility(View.INVISIBLE);
                Toast.makeText(this, "Unlocked", Toast.LENGTH_SHORT).show();
                startBackgroundThread(player);
                break;
            case R.id.unlock:
                controlsMode = ControlsMode.LOCK;
                root.setVisibility(View.INVISIBLE);
                lock.setVisibility(View.VISIBLE);
                Toast.makeText(this, "Locked", Toast.LENGTH_SHORT).show();
                stopBackgroundThread();
                break;
            case R.id.exo_next:
                try {
                    player.stop();
                    stopBackgroundThread();
                    position++;
                    playVideo();
                    title.setText(mVideoFiles.get(position).getDisplayName());
                } catch (Exception e) {
                    Toast.makeText(this, "No next video", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            case R.id.exo_prev:
                try {
                    player.stop();
                    stopBackgroundThread();
                    position--;
                    playVideo();
                    title.setText(mVideoFiles.get(position).getDisplayName());
                } catch (Exception e) {
                    Toast.makeText(this, "No previous video", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
        }
    }

    View.OnClickListener firstListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
            player.setVideoScalingMode(C.VIDEO_SCALING_MODE_DEFAULT);
            scaling.setImageResource(R.drawable.fullscreen);

            Toast.makeText(VideoPlayerActivity.this, "Full Screen", Toast.LENGTH_SHORT).show();
            scaling.setOnClickListener(secondListener);
        }
    };

    View.OnClickListener secondListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM);
            player.setVideoScalingMode(C.VIDEO_SCALING_MODE_DEFAULT);
            scaling.setImageResource(R.drawable.zoom);

            Toast.makeText(VideoPlayerActivity.this, "Zoom", Toast.LENGTH_SHORT).show();
            scaling.setOnClickListener(thirdListener);
        }
    };

    View.OnClickListener thirdListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
            player.setVideoScalingMode(C.VIDEO_SCALING_MODE_DEFAULT);
            scaling.setImageResource(R.drawable.fit);

            Toast.makeText(VideoPlayerActivity.this, "Fit", Toast.LENGTH_SHORT).show();
            scaling.setOnClickListener(firstListener);
        }
    };

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (!isInPictureInPictureMode()) {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Rational aspectRatio = new Rational(16,9);
                    pictureInPicture.setAspectRatio(aspectRatio);
                    enterPictureInPictureMode(pictureInPicture.build());
                } else {
                    Log.wtf("Not Oreo", "yes");
                }
            } else {
                Log.d("PIP_TAG", "onUserLeaveHint: Already in PIP");
            }
        }
    }

    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode, Configuration newConfig) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig);
        isCrossChecked = isInPictureInPictureMode;
        if (isInPictureInPictureMode) {
            playerView.hideController();
        } else {
            playerView.showController();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(isCrossChecked) {
            player.release();
            finish();
        }
    }


}
//    TODO
//    private static final int CAMERA_PERMISSION = 1122;
//    private TextView videoNameTV, videoTimeTV;
//    private ImageButton backIB, forwardIB, playPauseIB;
//    private VideoView videoView;
//    private RelativeLayout controlsRL, videoRL;
//
//    private SeekBar videoSeekBar;
//    private String videoName, videoPath;
////    private Camera predict;
////    private AutoFitTextureView textureView;
////    private TextView result;
////    private Size preViewSize;
////    private String cameraId;
////    private static final int CAMERA_PERMISSION = 1122;
////    private static final int STATE_PREVIEW =0;
////    private static final int STATE_WAIT_LOCK = 1;
////    private int state;
////    private int imageSize = 224;
////    private boolean runClassifier = false;
////    private final Object lock = new Object();
////    private Semaphore cameraOpenCloseLock = new Semaphore(1);
////    private TextureView.SurfaceTextureListener surfaceTextureListener =
////            new TextureView.SurfaceTextureListener() {
////                @Override
////                public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
////                    setUpCamera(width, height);
////                    openCamera();
////                }
////
////                @Override
////                public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {
////                    configureTransform(width, height);
////                }
////
////                @Override
////                public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
////                    return false;
////                }
////
////                @Override
////                public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {
////
////                }
////            };
////    private CameraDevice cameraDevice;
////    private CameraDevice.StateCallback cameraDeviceStateCallBack =
////            new CameraDevice.StateCallback() {
////                @Override
////                public void onOpened(@NonNull CameraDevice camera) {
////                    cameraOpenCloseLock.release();
////                    cameraDevice = camera;
////                    createCameraPreviewSession();
////                }
////
////                @Override
////                public void onDisconnected(@NonNull CameraDevice camera) {
////                    cameraOpenCloseLock.release();
////                    camera.close();
////                    cameraDevice = null;
////                }
////
////                @Override
////                public void onError(@NonNull CameraDevice camera, int error) {
////                    cameraOpenCloseLock.release();
////                    camera.close();
////                    cameraDevice = null;
////                    Fragment fragment = new Fragment();
////                    Activity activity = fragment.getActivity();
////                    if (null != activity) {
////                        activity.finish();
////                    }
////                }
////            };
////
////    private CaptureRequest previewCaptureRequest;
////    private CaptureRequest.Builder previewCaptureRequestBuilder;
////    private CameraCaptureSession cameraCaptureSession;
////    private CameraCaptureSession.CaptureCallback sessionCaptureCallBack =
////            new CameraCaptureSession.CaptureCallback() {
//////                private void  process(CaptureResult result) {
//////                    switch (state) {
//////                        case STATE_PREVIEW:
//////                            // Do nothing
//////                            break;
//////                        case STATE_WAIT_LOCK:
//////                            Integer afState = result.get(CaptureResult.CONTROL_AF_STATE);
//////                            if (afState == CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED) {
//////                                unLockFocus();
//////                                Toast.makeText(getApplicationContext(), "Focus Lock Successful", Toast.LENGTH_SHORT).show();
//////                            }
//////                            break;
//////                    }
//////                }
////
////                @Override
////                public void onCaptureStarted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, long timestamp, long frameNumber) {
////                    super.onCaptureStarted(session, request, timestamp, frameNumber);
////                }
////
////                @Override
////                public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
////                    super.onCaptureCompleted(session, request, result);
////                }
////
////                @Override
////                public void onCaptureFailed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureFailure failure) {
////                    super.onCaptureFailed(session, request, failure);
////                }
////            };
////    private HandlerThread backgroundThread;
////    private Handler backgroundHandler;
////    private ImageReader imageReader;
////    /** Max preview width that is guaranteed by Camera2 API */
////    private static final int MAX_PREVIEW_WIDTH = 640;
////    /** Max preview height that is guaranteed by Camera2 API */
////    private static final int MAX_PREVIEW_HEIGHT = 480;
////
////
////
////
////
////    /** Starts a background thread and its Handler. */
////    private void startBackgroundThread() {
////        backgroundThread = new HandlerThread("Camera 2 background thread");
////        backgroundThread.start();
////        backgroundHandler = new Handler(backgroundThread.getLooper());
//////        synchronized (lock) {
//////            runClassifier = true;
//////        }
//////        backgroundHandler.post(periodicClassify);
////    }
////
////    /** Stops the background thread and its Handler. */
////    private void stopBackgroundThread() {
////        backgroundThread.quitSafely();
////        try {
////            backgroundThread.join();
////            backgroundThread = null;
////            backgroundHandler = null;
//////            synchronized (lock) {
//////                runClassifier = false;
//////            }
////        } catch (InterruptedException e) {
////            e.printStackTrace();
////        }
////    }
////
//////    private void lockFocus() {
//////        try {
//////            state = STATE_WAIT_LOCK;
//////            previewCaptureRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER,
//////                    CaptureRequest.CONTROL_AF_TRIGGER_START);
//////            cameraCaptureSession.capture(previewCaptureRequestBuilder.build(),
//////                    sessionCaptureCallBack, backgroundHandler);
//////        }catch (CameraAccessException e) {
//////            e.printStackTrace();
//////        }
//////    }
//////    private void unLockFocus() {
//////        try {
//////            state = STATE_PREVIEW;
//////            previewCaptureRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER,
//////                    CaptureRequest.CONTROL_AF_TRIGGER_CANCEL);
//////            cameraCaptureSession.capture(previewCaptureRequestBuilder.build(),
//////                    sessionCaptureCallBack, backgroundHandler);
//////        }catch (CameraAccessException e) {
//////            e.printStackTrace();
//////        }
//////    }
////
////
//////    private Size getPreferredPreviewSize(Size[] mapSizes, int width, int height) {
//////        List<Size> collectorSizes = new ArrayList<>();
//////        for(Size option: mapSizes) {
//////            if(width > height) {
//////                if(option.getWidth() > width && option.getHeight() > height) {
//////                    collectorSizes.add(option);
//////                }
//////            }else {
//////                if(option.getWidth() > height && option.getHeight() > width) {
//////                    collectorSizes.add(option);
//////                }
//////            }
//////        }
//////        if(collectorSizes.size() > 0) return Collections.min(collectorSizes, new Comparator<Size>() {
//////            @Override
//////            public int compare(Size o1, Size o2) {
//////                return Long.signum(o1.getWidth() * o1.getHeight() - o2.getWidth() * o2.getHeight());
//////            }
//////        });
//////        return mapSizes[0];
//////    }
////
////    private static Size chooseOptimalSize(
////            Size[] choices,
////            int textureViewWidth,
////            int textureViewHeight,
////            int maxWidth,
////            int maxHeight,
////            Size aspectRatio) {
////
////        // Collect the supported resolutions that are at least as big as the preview Surface
////        List<Size> bigEnough = new ArrayList<>();
////        // Collect the supported resolutions that are smaller than the preview Surface
////        List<Size> notBigEnough = new ArrayList<>();
////        int w = aspectRatio.getWidth();
////        int h = aspectRatio.getHeight();
////        for (Size option : choices) {
////            if (option.getWidth() <= maxWidth
////                    && option.getHeight() <= maxHeight
////                    && option.getHeight() == option.getWidth() * h / w) {
////                if (option.getWidth() >= textureViewWidth && option.getHeight() >= textureViewHeight) {
////                    bigEnough.add(option);
////                } else {
////                    notBigEnough.add(option);
////                }
////            }
////        }
////
////        // Pick the smallest of those big enough. If there is no one big enough, pick the
////        // largest of those not big enough.
////        if (bigEnough.size() > 0) {
////            return Collections.min(bigEnough, new Comparator<Size>() {
////                @Override
////                public int compare(Size o1, Size o2) {
////                    return Long.signum(
////                            (long) o1.getWidth() * o1.getHeight() - (long) o2.getWidth() * o2.getHeight());
////                }
////            });
////        } else if (notBigEnough.size() > 0) {
////            return Collections.max(notBigEnough, new Comparator<Size>() {
////                @Override
////                public int compare(Size o1, Size o2) {
////                    return Long.signum(
////                            (long) o1.getWidth() * o1.getHeight() - (long) o2.getWidth() * o2.getHeight());
////                }
////            });
////        } else {
////            Log.e("Video Player Camera", "Couldn't find any suitable preview size");
////            return choices[0];
////        }
////    }
////
////    private String chooseCamera(CameraManager manager) throws CameraAccessException {
////        String frontCameraId = null;
////        String backCameraId = null;
////        if (manager != null && manager.getCameraIdList().length > 0) {
////            for (String camId : manager.getCameraIdList()) {
////                CameraCharacteristics characteristics = manager.getCameraCharacteristics(camId);
////                StreamConfigurationMap map =
////                        characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
////                Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
////                if (facing != null && map != null) {
////                    if (facing == CameraCharacteristics.LENS_FACING_FRONT) {
////                        frontCameraId = camId;
////                        break;
////                    } else if (facing == CameraCharacteristics.LENS_FACING_BACK) {
////                        backCameraId = camId;
////                    }
////                }
////            }
////
////            return frontCameraId != null ? frontCameraId : backCameraId;
////        }
////        return null;
////    }
////
////    private void setUpCamera(int width, int height) {
////        Fragment fragment = new Fragment();
////        Activity activity = fragment.getActivity();
////        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
////        try {
////            String cameraID = chooseCamera(cameraManager);
////            // Front and back camera is not present or not accessible
////            if (cameraID == null) {
////                throw new IllegalStateException("Camera Not Found");
////            }
////            CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraID);
////            StreamConfigurationMap map = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
////            Size largest =
////                    Collections.max(
////                            Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)), new Comparator<Size>() {
////                                @Override
////                                public int compare(Size o1, Size o2) {
////                                    return Long.signum(
////                                            (long) o1.getWidth() * o1.getHeight() - (long) o2.getWidth() * o2.getHeight());
////                                }
////                            });
////            imageReader = ImageReader.newInstance(
////                    largest.getWidth(), largest.getHeight(), ImageFormat.JPEG, 2);
////            // Find out if we need to swap dimension to get the preview size relative to sensor
////            // coordinate.
////            int displayRotation = getWindowManager().getDefaultDisplay().getRotation();
////            // noinspection ConstantConditions
////            /* Orientation of the camera sensor */
////            int sensorOrientation = cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
////            boolean swappedDimensions = false;
////            switch (displayRotation) {
////                case Surface.ROTATION_0:
////                case Surface.ROTATION_180:
////                    if (sensorOrientation == 90 || sensorOrientation == 270) {
////                        swappedDimensions = true;
////                    }
////                    break;
////                case Surface.ROTATION_90:
////                case Surface.ROTATION_270:
////                    if (sensorOrientation == 0 || sensorOrientation == 180) {
////                        swappedDimensions = true;
////                    }
////                    break;
////                default:
////                    Log.e("Video Player Camera", "Display rotation is invalid: " + displayRotation);
////            }
////
////            Point displaySize = new Point();
////            getWindowManager().getDefaultDisplay().getSize(displaySize);
////            int rotatedPreviewWidth = width;
////            int rotatedPreviewHeight = height;
////            int maxPreviewWidth = displaySize.x;
////            int maxPreviewHeight = displaySize.y;
////
////            if (swappedDimensions) {
////                rotatedPreviewWidth = height;
////                rotatedPreviewHeight = width;
////                maxPreviewWidth = displaySize.y;
////                maxPreviewHeight = displaySize.x;
////            }
////
////            if (maxPreviewWidth > MAX_PREVIEW_WIDTH) {
////                maxPreviewWidth = MAX_PREVIEW_WIDTH;
////            }
////
////            if (maxPreviewHeight > MAX_PREVIEW_HEIGHT) {
////                maxPreviewHeight = MAX_PREVIEW_HEIGHT;
////            }
////
////            preViewSize = chooseOptimalSize(
////                    map.getOutputSizes(SurfaceTexture.class),
////                    rotatedPreviewWidth,
////                    rotatedPreviewHeight,
////                    maxPreviewWidth,
////                    maxPreviewHeight,
////                    largest);
////
////            // We fit the aspect ratio of TextureView to the size of preview we picked.
////            int orientation = getResources().getConfiguration().orientation;
////            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
////                textureView.setAspectRatio(preViewSize.getWidth(), preViewSize.getHeight());
////            } else {
////                textureView.setAspectRatio(preViewSize.getHeight(), preViewSize.getWidth());
////            }
////
////            cameraId = cameraID;
//////            preViewSize = getPreferredPreviewSize(map.getOutputSizes(SurfaceTexture.class), width, height);
////            return;
////        } catch (CameraAccessException e) {
////            Log.e("Video Player Camera", "Failed to access Camera", e);
////        }
////    }
////
//////    private void adjustAspectRatio(int videoWidth, int videoHeight) {
//////        int viewWidth = textureView.getWidth();
//////        int viewHeight = textureView.getHeight();
//////        double aspectRatio = (double) videoHeight / videoWidth;
//////
//////        int newWidth, newHeight;
//////        if (viewHeight > (int) (viewWidth * aspectRatio)) {
//////            // limited by narrow width; restrict height
//////            newWidth = viewWidth;
//////            newHeight = (int) (viewWidth * aspectRatio);
//////        } else {
//////            // limited by short height; restrict width
//////            newWidth = (int) (viewHeight / aspectRatio);
//////            newHeight = viewHeight;
//////        }
//////        int xoff = (viewWidth - newWidth) / 2;
//////        int yoff = (viewHeight - newHeight) / 2;
//////        Log.v("Video Player Camera", "video=" + videoWidth + "x" + videoHeight +
//////                " view=" + viewWidth + "x" + viewHeight +
//////                " newView=" + newWidth + "x" + newHeight +
//////                " off=" + xoff + "," + yoff);
//////
//////        Matrix txform = new Matrix();
//////        textureView.getTransform(txform);
//////        txform.setScale((float) newWidth / viewWidth, (float) newHeight / viewHeight);
//////        //txform.postRotate(10);          // just for fun
//////        txform.postTranslate(xoff, yoff);
//////        textureView.setTransform(txform);
//////    }
////
////    private void configureTransform(int viewWidth, int viewHeight) {
////        if (null == textureView || null == preViewSize) {
////            return;
////        }
////        int rotation = getWindowManager().getDefaultDisplay().getRotation();
////        Matrix matrix = new Matrix();
////        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
////        RectF bufferRect = new RectF(0, 0, preViewSize.getHeight(), preViewSize.getWidth());
////        float centerX = viewRect.centerX();
////        float centerY = viewRect.centerY();
////        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
////            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
////            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
////            float scale =
////                    Math.max(
////                            (float) viewHeight / preViewSize.getHeight(),
////                            (float) viewWidth / preViewSize.getWidth());
////            matrix.postScale(scale, scale, centerX, centerY);
////            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
////        } else if (Surface.ROTATION_180 == rotation) {
////            matrix.postRotate(180, centerX, centerY);
////        }
////        textureView.setTransform(matrix);
////    }
////
////    private void closeCamera() {
////        try {
////            cameraOpenCloseLock.acquire();
////            if (null != cameraCaptureSession) {
////                cameraCaptureSession.close();
////                cameraCaptureSession = null;
////            }
////            if (null != cameraDevice) {
////                cameraDevice.close();
////                cameraDevice = null;
////            }
////            if (null != imageReader) {
////                imageReader.close();
////                imageReader = null;
////            }
////        } catch (InterruptedException e) {
////            throw new RuntimeException("Interrupted while trying to lock camera closing.", e);
////        } finally {
////            cameraOpenCloseLock.release();
////        }
////    }
////
////    private void openCamera() {
////        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
////        try {
////            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
////                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION);
////            } else {
////                cameraManager.openCamera(cameraId, cameraDeviceStateCallBack, backgroundHandler);
////                Toast.makeText(this, "Camera is open", Toast.LENGTH_LONG).show();
////            }
////
////        } catch(CameraAccessException e) {
////            e.printStackTrace();
////        }
////    }
////
////    private Runnable periodicClassify =
////            new Runnable() {
////                @Override
////                public void run() {
////                    synchronized (lock) {
////                        if (runClassifier) {
////                            startBackgroundThread();
////                            classifyFrame();
////                        }
////                    }
//////                    lockFocus();
//////                    classifyFrame();
////                    backgroundHandler.post(periodicClassify);
//////                    unLockFocus();
////                }
////            };
////
////    private void classifyFrame() {
////        Bitmap originalBitmap = textureView.getBitmap();
//////        int dimension = Math.min(originalBitmap.getWidth(), originalBitmap.getHeight());
////        Bitmap image = ThumbnailUtils.extractThumbnail(originalBitmap, 224, 224);
//////        image = Bitmap.createScaledBitmap(image, imageSize, imageSize, false);
////        classifyImage(image);
////    }
////
////    private void classifyImage(Bitmap image) {
////        try {
////            Model model = Model.newInstance(getApplicationContext());
////
////            // Creates inputs for reference.
////            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 224, 224, 3}, DataType.FLOAT32);
////            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * imageSize * imageSize * 3);
////            byteBuffer.order(ByteOrder.nativeOrder());
////
////            int[] intValues = new int[imageSize*imageSize];
////            image.getPixels(intValues, 0, image.getWidth(), 0,0, image.getWidth(), image.getHeight());
////            int pixel = 0;
////            for(int i=0; i<imageSize; i++){
////                for(int j=0; j<imageSize; j++){
////                    int val = intValues[pixel++];
////                    byteBuffer.putFloat(((val >> 16) & 0xFF) * (1.f / 255.f));
////                    byteBuffer.putFloat(((val >> 8) & 0xFF) * (1.f / 255.f));
////                    byteBuffer.putFloat((val & 0xFF) * (1.f / 255.f));
////                }
////            }
////            inputFeature0.loadBuffer(byteBuffer);
////
////            // Runs model inference and gets result.
////            Model.Outputs outputs = model.process(inputFeature0);
////            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();
////
////            float[] confidences = outputFeature0.getFloatArray();
////            int maxPos = 0;
////            float maxCon = 0;
////            for(int i=0; i<confidences.length; i++){
////                if(confidences[i] > maxCon){
////                    maxCon = confidences[i];
////                    maxPos = i;
////                }
////            }
////
////            String[] classes = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};
////            String result = String.format("%s: %.1f%", classes[maxPos], maxCon*100);
////            this.result.setText(result);
////            // Releases model resources if no longer used.
////            model.close();
////        } catch (IOException e) {
////            e.printStackTrace();
////        }
////    }
////
////
////    @Override
////    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
////        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
////        if(requestCode == CAMERA_PERMISSION) {
////            if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
////                openCamera();
////                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
////            }else{
////                Toast.makeText(this, "This App will not work without permissions..", Toast.LENGTH_SHORT).show();
////                finish();
////            }
////        }
////    }
////
////    private void createCameraPreviewSession() {
////        try {
////            SurfaceTexture surfaceTexture = textureView.getSurfaceTexture();
////            surfaceTexture.setDefaultBufferSize(preViewSize.getWidth(), preViewSize.getHeight());
////            Surface previewSurface = new Surface(surfaceTexture);
////            previewCaptureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
////            previewCaptureRequestBuilder.addTarget(previewSurface);
////            cameraDevice.createCaptureSession(
////                    Arrays.asList(previewSurface, imageReader.getSurface()),
////                    new CameraCaptureSession.StateCallback() {
////                        @Override
////                        public void onConfigured(@NonNull CameraCaptureSession session) {
////                            // The camera is already closed
////                            if (null == cameraDevice) {
////                                return;
////                            }
////
////                            // When the session is ready, we start displaying the preview.
////                            cameraCaptureSession = session;
////                            try {
////                                // Auto focus should be continuous for camera preview.
////                                previewCaptureRequestBuilder.set(
////                                        CaptureRequest.CONTROL_AF_MODE,
////                                        CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
////
////                                // Finally, we start displaying the camera preview.
////                                previewCaptureRequest = previewCaptureRequestBuilder.build();
////                                cameraCaptureSession.setRepeatingRequest(
////                                        previewCaptureRequest, sessionCaptureCallBack, backgroundHandler);
////                            } catch (CameraAccessException e) {
////                                e.printStackTrace();
////                            }
////                        }
////
////                        @Override
////                        public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
////                            Toast.makeText(getApplicationContext(), "Create camera session failed!", Toast.LENGTH_SHORT).show();
////                        }
////                    },
////                    null);
////        } catch(CameraAccessException e) {
////            e.printStackTrace();
////        }
////    }
////
////    @Override
////    public void onResume() {
////        super.onResume();
////        startBackgroundThread();
////        if(textureView.isAvailable()) {
////            setUpCamera(textureView.getWidth(), textureView.getHeight());
////            openCamera();
////        }else{
////            textureView.setSurfaceTextureListener(surfaceTextureListener);
////        }
////    }
////    @Override
////    public void onPause() {
////        closeCamera();
////        stopBackgroundThread();
////        super.onPause();
////    }
//    TODO
//    public class Camera extends Thread {
//        ImageButton pp;
//        VideoView vv;
//        public Camera(ImageButton playPauseIB, VideoView videoView) {
//            this.pp = playPauseIB;
//            this.vv = videoView;
//        }
//        public void run() {
//            try {
//                getSupportFragmentManager()
//                        .beginTransaction()
//                        .add(R.id.RLcamera, CameraFragment.newInstance(pp, vv))
//                        .commit();
//            }catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//}
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_video_player);
//        videoName = getIntent().getStringExtra("videoName");
//        videoPath = getIntent().getStringExtra("videoPath");
//        videoNameTV = findViewById(R.id.idTVVideoTitle);
//        videoTimeTV = findViewById(R.id.idTVTime);
//        backIB = findViewById(R.id.idIBBack);
//        playPauseIB = findViewById(R.id.idIBPlay);
//        forwardIB = findViewById(R.id.idIBForward);
//        videoSeekBar = findViewById(R.id.idSeekBarProgress);
//        videoView = findViewById(R.id.idVideoView);
//        controlsRL = findViewById(R.id.idRLControls);
//        videoRL = findViewById(R.id.idRLVideo);
////        textureView = (AutoFitTextureView) findViewById(R.id.cameraDisplay);
////        result = findViewById(R.id.result);
//        videoView.setVideoURI(Uri.parse(videoPath));
////        if (null == savedInstanceState) {
////            Camera camera = new Camera(playPauseIB, videoView);
////            camera.run();
////        }
//        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
//            @Override
//            public void onPrepared(MediaPlayer mediaPlayer) {
//                videoSeekBar.setMax(videoView.getDuration());
//                videoView.start();
//            }
//        });
//
//        videoNameTV.setText(videoName);
//
//
//        backIB.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                videoView.seekTo(videoView.getCurrentPosition()-10000);
//
//            }
//        });
//        forwardIB.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                videoView.seekTo(videoView.getCurrentPosition()+10000);
//            }
//        });
//        playPauseIB.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if(videoView.isPlaying()) {
//                    videoView.pause();
//                    playPauseIB.setImageDrawable(getResources().getDrawable(R.drawable.ic_play));
//                }else{
//                    videoView.start();
//                    playPauseIB.setImageDrawable(getResources().getDrawable(R.drawable.ic_pause));
//                }
//            }
//        });
//        videoRL.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if(isOpen) {
//                    hideControls();
//                    isOpen = false;
//                }else{
//                    showControls();
//                    isOpen = true;
//                }
//            }
//        });
//
//
//        setHandler();
//        initializeSeekBar();
//
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
//                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION);
//        } else {
//            if (null == savedInstanceState) {
//                Camera camera = new Camera(playPauseIB, videoView);
//                camera.run();
//            }
//        }
//
//    }
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if(requestCode == CAMERA_PERMISSION) {
//            if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                Camera camera = new Camera(playPauseIB, videoView);
//                camera.run();
//                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
//            }else{
//                Toast.makeText(this, "Hand Gestures won't work without camera, please change camera permission from settings", Toast.LENGTH_LONG).show();
//            }
//        }
//    }
//
//
//    private void setHandler(){
//        Handler handler = new Handler();
//        Runnable runnable = new Runnable() {
//            @Override
//            public void run() {
//                if(videoView.getDuration()>0){
//                    int curPos = videoView.getCurrentPosition();
//                    videoSeekBar.setProgress(curPos);
//                    videoTimeTV.setText("" + convertTime(videoView.getDuration()-curPos));
//                }
//                handler.postDelayed(this,0);
//            }
//        };
//        handler.postDelayed(runnable, 500);
//    }
//
//    private String convertTime(int ms) {
//        String time;
//        int x, seconds, minutes, hours;
//        x = ms/1000;
//        seconds = x%60;
//        x /= 60;
//        minutes = x%60;
//        x /= 60;
//        hours = x%24;
//        if(hours != 0) {
//                time = String.format("%02d", hours) + ":" + String.format("%02d", minutes) + ":" + String.format("%02d", seconds);
//        }else{
//            time = String.format("%02d", minutes) + ":" + String.format("%02d", seconds);
//        }
//        return time;
//    }
//
//    private void initializeSeekBar(){
//        videoSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//            @Override
//            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
//                if(videoSeekBar.getId() == R.id.idSeekBarProgress) {
//                    if(b){
//                          videoView.seekTo(i);
//                          videoView.start();
//                          int curPos = videoView.getCurrentPosition();
//                          videoTimeTV.setText("" + convertTime(videoView.getDuration()-curPos));
//                    }
//                }
//            }
//
//            @Override
//            public void onStartTrackingTouch(SeekBar seekBar) {
//
//            }
//
//            @Override
//            public void onStopTrackingTouch(SeekBar seekBar) {
//
//            }
//        });
//    }
//
//    private void showControls() {
//        controlsRL.setVisibility(View.VISIBLE);
//        final Window window = this.getWindow();
//        if(window == null) {
//            return;
//        }
//        window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        window.addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
//        View decorView = window.getDecorView();
//        if(decorView != null) {
//                int uiOption = decorView.getSystemUiVisibility();
//                if(Build.VERSION.SDK_INT>=14) {
//                    uiOption&= ~View.SYSTEM_UI_FLAG_LOW_PROFILE;
//                }
//                if(Build.VERSION.SDK_INT>=16) {
//                    uiOption&= ~View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
//                }
//            if(Build.VERSION.SDK_INT>=19) {
//                uiOption&= ~View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
//            }
//
//            decorView.setSystemUiVisibility(uiOption);
//        }
//    }
//
//    private void hideControls() {
//        controlsRL.setVisibility(View.GONE);
//        final Window window = this.getWindow();
//        if(window == null) {
//            return;
//        }
//        window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        window.addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
//        View decorView = window.getDecorView();
//        if(decorView != null) {
//            int uiOption = decorView.getSystemUiVisibility();
//            if(Build.VERSION.SDK_INT>=14) {
//                uiOption |= View.SYSTEM_UI_FLAG_LOW_PROFILE;
//            }
//            if(Build.VERSION.SDK_INT>=16) {
//                uiOption |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
//            }
//            if(Build.VERSION.SDK_INT>=19) {
//                uiOption |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
//            }
//
//            decorView.setSystemUiVisibility(uiOption);
//        }
//    }
