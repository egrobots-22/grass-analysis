package com.egrobots.grassanalysis.presentation;

import android.Manifest;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.egrobots.grassanalysis.R;
import com.google.common.util.concurrent.ListenableFuture;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.CameraX;
import androidx.camera.core.Preview;
import androidx.camera.core.impl.PreviewConfig;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.video.MediaStoreOutputOptions;
import androidx.camera.video.PendingRecording;
import androidx.camera.video.Quality;
import androidx.camera.video.QualitySelector;
import androidx.camera.video.Recorder;
import androidx.camera.video.Recording;
import androidx.camera.video.VideoCapture;
import androidx.camera.video.VideoRecordEvent;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.PermissionChecker;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class RecordScreenActivity2 extends AppCompatActivity {

    private static final int REQUEST_CODE_PERMISSIONS = 1;
    private static final String[] REQUIRED_PERMISSIONS = {Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
    private static final String TAG = RecordScreenActivity2.class.getSimpleName();
    private static final String FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS";
    private ExecutorService cameraExecutor;
    private VideoCapture<Recorder> videoCapture = null;
    private Recording recording;

    @BindView(R.id.viewFinder)
    PreviewView viewFinder;
    @BindView(R.id.video_capture_button)
    Button videoCaptureButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_screen2);
        ButterKnife.bind(this);

        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }
        cameraExecutor = Executors.newSingleThreadExecutor();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera();
            } else {
                Toast.makeText(this, "Permissions not granted by the user..", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(viewFinder.getSurfaceProvider());

                Recorder recorder = new Recorder.Builder().setQualitySelector(QualitySelector.from(Quality.HIGHEST)).build();
                videoCapture = VideoCapture.withOutput(recorder);

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview);
            } catch (ExecutionException e) {
                Log.e(TAG, "Use case binding failed", e);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    @OnClick(R.id.video_capture_button)
    public void onCaptureVideoClicked() {
        if (videoCapture != null) {
            videoCaptureButton.setEnabled(false);
            Recording curRecording = recording;
            if (curRecording != null) {
                // Stop the current recording session.
                curRecording.stop();
                recording = null;
                return;
            }
            // create and start a new recording session
            String name = new SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis());
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4");
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                contentValues.put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/CameraX-Video");
            }

            MediaStoreOutputOptions mediaStoreOutputOptions = new MediaStoreOutputOptions
                    .Builder(getContentResolver(), MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
                    .setContentValues(contentValues)
                    .build();

            PendingRecording pendingRecording = videoCapture.getOutput().prepareRecording(this, mediaStoreOutputOptions);

            if (PermissionChecker.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PermissionChecker.PERMISSION_GRANTED) {
                pendingRecording.withAudioEnabled();
            }
            recording = pendingRecording.start(ContextCompat.getMainExecutor(this), recordEvent -> {
                if (recordEvent instanceof VideoRecordEvent.Start) {
                    videoCaptureButton.setText("Stop Recording");
                    videoCaptureButton.setEnabled(true);
                } else if (recordEvent instanceof VideoRecordEvent.Finalize) {
                    if (!((VideoRecordEvent.Finalize) recordEvent).hasError()) {
                        String msg = "Video capture succeeded: " +
                                ((VideoRecordEvent.Finalize) recordEvent).getOutputResults().getOutputUri();
                        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                        Log.d(TAG, msg);
                    } else {
                        recording.close();
                        recording = null;
                        Log.e(TAG, "Video capture ends with error: "
                                + ((VideoRecordEvent.Finalize) recordEvent).getError());
                    }
                    videoCaptureButton.setText("Start Capture");
                    videoCaptureButton.setEnabled(true);
                }
            });
        }
    }

    private boolean allPermissionsGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && REQUIRED_PERMISSIONS != null) {
            for (String permission : REQUIRED_PERMISSIONS) {
                if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
    }
}