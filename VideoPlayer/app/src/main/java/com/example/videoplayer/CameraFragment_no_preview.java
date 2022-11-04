package com.example.videoplayer;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.OutputConfiguration;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.AudioManager;
import android.media.Image;
import android.media.ImageReader;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;
import android.provider.Settings;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import com.google.android.exoplayer2.SimpleExoPlayer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.function.ToDoubleBiFunction;

/** Basic fragments for the Camera. */
public class CameraFragment_no_preview extends Fragment
        implements ActivityCompat.OnRequestPermissionsResultCallback {

    /** Tag for the {@link Log}. */
    private static final String TAG = "Video Player Camera";
    private static final String FRAGMENT_DIALOG = "dialog";
    private static final String HANDLE_THREAD_NAME = "CameraBackground";
    private SimpleExoPlayer simpleExoPlayer;
    private static final int PERMISSIONS_REQUEST_CODE = 1;
    private static final int CAMERA_PERMISSION = 1122;
    private ImageClassifier classifier;
    private boolean runClassifier = false;
    private boolean checkedPermissions = false;
    private TextView result;
    private AudioManager audioManager;
    boolean success = false;
    private TextView vol_text;
    private LinearLayout vol_text_container;
    private Object lock = new Object();



    /** ID of the current {@link CameraDevice}. */
    private String cameraId;

    /** A {@link CameraCaptureSession } for camera preview. */
    private CameraCaptureSession captureSession;

    /** A reference to the opened {@link CameraDevice}. */
    private CameraDevice cameraDevice;

    /** {@link CameraDevice.StateCallback} is called when {@link CameraDevice} changes its state. */
    private final CameraDevice.StateCallback stateCallback =
            new CameraDevice.StateCallback() {

                @Override
                public void onOpened(@NonNull CameraDevice currentCameraDevice) {
                    // This method is called when the camera is opened.  We start camera preview here.
                    cameraOpenCloseLock.release();
                    cameraDevice = currentCameraDevice;
                    createCameraPreviewSession();
                }

                @Override
                public void onDisconnected(@NonNull CameraDevice currentCameraDevice) {
                    cameraOpenCloseLock.release();
                    currentCameraDevice.close();
                    cameraDevice = null;
                }

                @Override
                public void onError(@NonNull CameraDevice currentCameraDevice, int error) {
                    cameraOpenCloseLock.release();
                    currentCameraDevice.close();
                    cameraDevice = null;
                    Activity activity = getActivity();
                    if (null != activity) {
                        activity.finish();
                    }
                }
            };

    /** An additional thread for running tasks that shouldn't block the UI. */
    private HandlerThread backgroundThread;

    /** A {@link Handler} for running tasks in the background. */
    private Handler backgroundHandler;

    /** An {@link ImageReader} that handles image capture. */
    private ImageReader imageReader;
    private int jpgOrientation;
    /** A {@link Semaphore} to prevent the app from exiting before closing the camera. */
    private Semaphore cameraOpenCloseLock = new Semaphore(1);

    /** A {@link CameraCaptureSession.CaptureCallback} that handles events related to capture. */
    private CameraCaptureSession.CaptureCallback captureCallback =
            new CameraCaptureSession.CaptureCallback() {

                @Override
                public void onCaptureProgressed(
                        @NonNull CameraCaptureSession session,
                        @NonNull CaptureRequest request,
                        @NonNull CaptureResult partialResult) {
                }

                @Override
                public void onCaptureCompleted(
                        @NonNull CameraCaptureSession session,
                        @NonNull CaptureRequest request,
                        @NonNull TotalCaptureResult result) {
                }
            };
    private ImageView imageView;

    public CameraFragment_no_preview(SimpleExoPlayer player) {
        this.simpleExoPlayer = player;
    }

    /**
     * Shows a {@link Toast} on the UI thread for the classification results.
     *
     *
     */
    private void showToast(String s) {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        SpannableString str1 = new SpannableString(s);
        builder.append(str1);
        showToast(builder);
    }

    private void showToast(SpannableStringBuilder builder) {
        final Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(
                    new Runnable() {
                        @Override
                        public void run() {
                            result.setText(builder, TextView.BufferType.SPANNABLE);
                        }
                    });
        }
    }

    public static CameraFragment_no_preview newInstance(SimpleExoPlayer player) {
        return new CameraFragment_no_preview(player);
    }

    /** Layout the preview and buttons. */
    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_video_player, container, false);
    }


    /** Connect the buttons to their event handler. */
    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        result = view.findViewById(R.id.result);
        imageView = view.findViewById(R.id.imageView);

        vol_text = (TextView) view.findViewById(R.id.vol_text);
        vol_text_container = (LinearLayout) view.findViewById(R.id.vol_text_container);

        vol_text.setText("200");
        vol_text_container.setVisibility(View.VISIBLE);
        audioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
    }

    /** Load the model and labels. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            // create either a new ImageClassifierQuantizedMobileNet or an ImageClassifierFloatInception
            //      classifier = new ImageClassifierQuantizedMobileNet(getActivity());
            classifier = new ImageClassifierFloatInception(getActivity());
//            openCamera();
        } catch (IOException e) {
            Log.e(TAG, "Failed to initialize an image classifier.", e);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        startBackgroundThread();
        openCamera();
    }

    @Override
    public void onPause() {
        closeCamera();
        stopBackgroundThread();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        classifier.close();
        super.onDestroy();
    }

    /**
     * Sets up member variables related to camera.
     */
    private void setUpCameraOutputs() {
        Activity activity = getActivity();
        CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
        try {
            String camId = chooseCamera(manager);
            // Front and back camera is not present or not accessible
            if (camId == null) {
                throw new IllegalStateException("Camera Not Found");
            }
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(camId);
//
//            int orientation = getResources().getConfiguration().orientation;
//            int sensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
//            int deviceRotation = activity.getWindowManager().getDefaultDisplay().getRotation();
//            int surfaceRotation = ORIENTATIONS.get(deviceRotation);
//
////            jpgOrientation = getJpegOrientation(characteristics, orientation);
//            jpgOrientation = (surfaceRotation + sensorOrientation + 270) % 360;

            StreamConfigurationMap map =
                    characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

            // // For still image captures, we use the largest available size.
            Size largest =
                    Collections.max(
                            Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)), new CompareSizesByArea());
            imageReader =
                    ImageReader.newInstance(
                            largest.getWidth(), largest.getHeight(), ImageFormat.JPEG,50);
//            imageReader.setOnImageAvailableListener(mOnImageAvailableListener, backgroundHandler);

            this.cameraId = camId;
        } catch (CameraAccessException e) {
            Log.e(TAG, "Failed to access Camera", e);
        } catch (NullPointerException e) {
            // Currently an NPE is thrown when the Camera2API is used but not supported on the
            // device this code runs.
            ErrorDialog.newInstance(getString(R.string.camera_error))
                    .show(getChildFragmentManager(), FRAGMENT_DIALOG);
        }
    }

    private final ImageReader.OnImageAvailableListener mOnImageAvailableListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            Log.d(TAG, "ImageAvailable");
            backgroundHandler.post(periodicClassify);

//            try {
//                Thread.sleep(500);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }




        }

    };

    /**
     * Choose the Camera from the list of available cameras. Priority goes to front camera, if its
     * present then use the front camera, else switch to back camera.
     *
     * @param manager CameraManager
     * @return ID of the Camera
     * @throws CameraAccessException
     */
    private String chooseCamera(CameraManager manager) throws CameraAccessException {
        String frontCameraId = null;
        String backCameraId = null;
        if (manager != null && manager.getCameraIdList().length > 0) {
            for (String camId : manager.getCameraIdList()) {
                CameraCharacteristics characteristics = manager.getCameraCharacteristics(camId);
                StreamConfigurationMap map =
                        characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (facing != null && map != null) {
                    if (facing == CameraCharacteristics.LENS_FACING_FRONT) {
                        frontCameraId = camId;
                        break;
                    } else if (facing == CameraCharacteristics.LENS_FACING_BACK) {
                        backCameraId = camId;
                    }
                }
            }

            return frontCameraId != null ? frontCameraId : backCameraId;
        }
        return null;
    }

    private String[] getRequiredPermissions() {
        return new String[]{Manifest.permission.CAMERA};
    }

    /** Opens the camera specified by {@link CameraFragment_no_preview#cameraId}. */
    private void openCamera() {
        if (!checkedPermissions && !allPermissionsGranted()) {
            ActivityCompat.requestPermissions(getActivity(), getRequiredPermissions(), PERMISSIONS_REQUEST_CODE);
            return;
        } else {
            checkedPermissions = true;
        }
        setUpCameraOutputs();

        if (cameraId == null) {
            throw new IllegalStateException("No front camera available.");
        }
        Activity activity = getActivity();
        CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
        try {
            if (!cameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Time out waiting to lock camera opening.");
            }

            if (!allPermissionsGranted()) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION);
            }

            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION);
            } else {
//                startBackgroundThread();
                manager.openCamera(cameraId, stateCallback, backgroundHandler);
                Toast.makeText(getContext(), "Camera is open", Toast.LENGTH_LONG).show();
            }
        } catch (CameraAccessException e) {
            Log.e(TAG, "Failed to open Camera", e);
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera opening.", e);
        }
    }

    private boolean allPermissionsGranted() {
        for (String permission : getRequiredPermissions()) {
            if (getContext().checkPermission(permission, Process.myPid(), Process.myUid())
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }


    /** Closes the current {@link CameraDevice}. */
    private void closeCamera() {
        try {
            cameraOpenCloseLock.acquire();
            if (null != captureSession) {
                captureSession.close();
                captureSession = null;
            }
            if (null != cameraDevice) {
                cameraDevice.close();
                cameraDevice = null;
            }
            if (null != imageReader) {
                imageReader.close();
                imageReader = null;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera closing.", e);
        } finally {
            cameraOpenCloseLock.release();
        }
    }

    /** Starts a background thread and its {@link Handler}. */
    private void startBackgroundThread() {
        backgroundThread = new HandlerThread(HANDLE_THREAD_NAME);
        backgroundThread.start();
        backgroundHandler = new Handler(backgroundThread.getLooper());
        synchronized (lock) {
            runClassifier = true;
        }
//        backgroundHandler.post(periodicClassify);
    }

    /** Stops the background thread and its {@link Handler}. */
    private void stopBackgroundThread() {
        backgroundThread.quitSafely();
        try {
            backgroundThread.join();
            backgroundThread = null;
            backgroundHandler = null;
            synchronized (lock) {
                runClassifier = false;
            }
        } catch (InterruptedException e) {
            Log.e(TAG, "Interrupted when stopping background thread", e);
        }
    }

    /** Takes photos and classify them periodically. */
    private Runnable periodicClassify =
            new Runnable() {
                @Override
                public void run() {
                    synchronized (lock) {
                        if (runClassifier) {
                            classifyFrame();
                        }
                    }
//                    backgroundHandler.post(periodicClassify);
//                    setUpCameraOutputs();
//                    createCameraPreviewSession();
                }
        };

    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }
    private int getJpegOrientation(CameraCharacteristics c, int deviceOrientation) {
        if (deviceOrientation == android.view.OrientationEventListener.ORIENTATION_UNKNOWN) return 0;
        int sensorOrientation = c.get(CameraCharacteristics.SENSOR_ORIENTATION);

        // Round device orientation to a multiple of 90
        deviceOrientation = (deviceOrientation + 45) / 90 * 90;

        // Reverse device orientation for front-facing cameras
        boolean facingFront = c.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT;
        if (facingFront) deviceOrientation = -deviceOrientation;

        // Calculate desired JPEG orientation relative to camera orientation to make
        // the image upright relative to the device orientation
        int jpegOrientation = (sensorOrientation + deviceOrientation + 360) % 360;

        return jpegOrientation;
    }
    /** Creates a new {@link CameraCaptureSession} for camera preview. */
    private void createCameraPreviewSession() {
        try {
            cameraDevice.createCaptureSession(
                    Arrays.asList(imageReader.getSurface()),
                    new CameraCaptureSession.StateCallback() {

                        @RequiresApi(api = Build.VERSION_CODES.R)
                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                            // The camera is already closed
                            if (null == cameraDevice) {
                                return;
                            }

                            // When the session is ready, we start displaying the preview.
                            captureSession = cameraCaptureSession;
                            try {
                                if (null == cameraDevice) {
                                    return;
                                }
                                // Auto focus should be continuous for camera preview.
                                final CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
                                captureBuilder.addTarget(imageReader.getSurface());
                                captureBuilder.set(
                                        CaptureRequest.CONTROL_AF_MODE,
                                        CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);

                                int rotation = getActivity().getWindowManager().getDefaultDisplay().getRotation();
//                                System.out.println(" HERE IS YOUR FUCKING ROTATION: " + rotation + "!");
                                captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));
                                // Finally, we start displaying the camera preview.
//                                captureSession.stopRepeating();
                                captureSession.capture(
                                        captureBuilder.build(), captureCallback, backgroundHandler);
                            } catch (CameraAccessException e) {
                                Log.e(TAG, "Failed to set up config to capture Camera", e);
                            }

                            imageReader.setOnImageAvailableListener(mOnImageAvailableListener, backgroundHandler);
                        }
                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                            showToast("Failed");
                        }
                    },
                    null);


        } catch (CameraAccessException e) {
            Log.e(TAG, "Failed to preview Camera", e);
        }


    }


    /** Classifies a frame from the preview stream. */
    private void classifyFrame() {
        if (classifier == null || getActivity() == null || cameraDevice == null) {
            showToast("Uninitialized Classifier or invalid context.");
            return;
        }
//        if (imageReader.acquireLatestImage() == null) {
//            return;
//        }


        SpannableStringBuilder textToShow = new SpannableStringBuilder();

        ByteBuffer buffer = imageReader.acquireLatestImage().getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        Bitmap originalBitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length,null);
        Matrix matrix = new Matrix();
        matrix.postRotate(-90);
        Bitmap rotatedBitmap = Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.getWidth(), originalBitmap.getHeight(), matrix, false);
        Bitmap bitmap = ThumbnailUtils.extractThumbnail(rotatedBitmap, 224, 224);


        //        Bitmap bitmap = textureView.getBitmap(classifier.getImageSizeX(),
        // classifier.getImageSizeY());

        classifier.classifyFrame(bitmap, textToShow);
        bitmap.recycle();
        originalBitmap.recycle();
        rotatedBitmap.recycle();
        Log.e("amlan", textToShow.toString());

        if (textToShow.toString().indexOf(":") != -1) {
            String token = textToShow.toString().substring(0, textToShow.toString().indexOf(":"));
            Activity activity = getActivity();
            activity.runOnUiThread(
                    new Runnable() {
                        @Override
                        public void run() {
                            controls(token);
//                            imageView.setImageBitmap(bitmap);
                            setUpCameraOutputs();
                            createCameraPreviewSession();


                        }
                    });
        }

        showToast(textToShow);

    }

    //TODO
    private void controls(String token) {
        if (token.equalsIgnoreCase("0")) {
            simpleExoPlayer.setPlayWhenReady(false);
        }
        else if (token.equalsIgnoreCase("1")) {
            simpleExoPlayer.setPlayWhenReady(true);
        }
        else if (token.equalsIgnoreCase("2")) {
            simpleExoPlayer.seekTo(simpleExoPlayer.getCurrentPosition() + 5000);
        }

        else if (token.equalsIgnoreCase("3")) {
            simpleExoPlayer.seekTo(simpleExoPlayer.getCurrentPosition() - 5000);
        }

        else if (token.equalsIgnoreCase("4")) {
            boolean value;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                value = Settings.System.canWrite(getContext());
                if(value) {
                    int media_volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                    int maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                    int newMediaVolume = media_volume + 1;
                    if (newMediaVolume > maxVol) {
                        newMediaVolume = maxVol;
                    } else if (newMediaVolume < 1) {
                        newMediaVolume = 0;
                    }
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newMediaVolume, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                    double volPer = Math.ceil((((double) newMediaVolume / (double) maxVol)* (double) 100));
                    vol_text.setText(" " + (int) volPer + "%");
                    vol_text.setVisibility(View.VISIBLE);
                    success = true;
                } else {
                    Toast.makeText(getContext(), "Allow write settings for swipe controls", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                    intent.setData(Uri.parse("package:" + getActivity().getPackageName()));
                    activityResultLaunch.launch(intent);
                }
            }
        }

        else if (token.equalsIgnoreCase("5")) {
            boolean value;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                value = Settings.System.canWrite(getContext());
                if(value) {
                    vol_text_container.setVisibility(View.VISIBLE);
                    int media_volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                    int maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                    int newMediaVolume = media_volume - 1;
                    if (newMediaVolume > maxVol) {
                        newMediaVolume = maxVol;
                    } else if (newMediaVolume < 1) {
                        newMediaVolume = 0;
                    }
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newMediaVolume, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                    double volPer = Math.ceil((((double) newMediaVolume / (double) maxVol)* (double) 100));
                    vol_text.setText(" " + (int) volPer + "%");
                    if(volPer < 1) {
                        vol_text.setVisibility(View.VISIBLE);
                        vol_text.setText("Off");
                    }
                    success = true;
                } else {
                    Toast.makeText(getContext(), "Allow write settings for swipe controls", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                    intent.setData(Uri.parse("package:" + getActivity().getPackageName()));
                    activityResultLaunch.launch(intent);
                }
            }
        }
    }

    ActivityResultLauncher<Intent> activityResultLaunch = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        boolean value;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            value = Settings.System.canWrite(getContext());
                            if(value) {
                                success = true;
                            } else {
                                Toast.makeText(getContext(), "Not granted", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
            });


    /** Compares two {@code Size}s based on their areas. */
    private static class CompareSizesByArea implements Comparator<Size> {

        @Override
        public int compare(Size lhs, Size rhs) {
            // We cast here to ensure the multiplications won't overflow
            return Long.signum(
                    (long) lhs.getWidth() * lhs.getHeight() - (long) rhs.getWidth() * rhs.getHeight());
        }
    }

    /** Shows an error message dialog. */
    public static class ErrorDialog extends DialogFragment {

        private static final String ARG_MESSAGE = "message";

        public static ErrorDialog newInstance(String message) {
            ErrorDialog dialog = new ErrorDialog();
            Bundle args = new Bundle();
            args.putString(ARG_MESSAGE, message);
            dialog.setArguments(args);
            return dialog;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Activity activity = getActivity();
            assert getArguments() != null;
            return new AlertDialog.Builder(activity)
                    .setMessage(getArguments().getString(ARG_MESSAGE))
                    .setPositiveButton(
                            android.R.string.ok,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    assert activity != null;
                                    activity.finish();
                                }
                            })
                    .create();
        }
    }
}

