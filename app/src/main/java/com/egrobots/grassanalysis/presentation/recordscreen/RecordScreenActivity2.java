package com.egrobots.grassanalysis.presentation.recordscreen;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.egrobots.grassanalysis.R;
import com.egrobots.grassanalysis.managers.CameraXRecorder;
import com.egrobots.grassanalysis.services.MyUploadService;
import com.egrobots.grassanalysis.utils.LoadingDialog;
import com.egrobots.grassanalysis.utils.Utils;
import com.egrobots.grassanalysis.utils.ViewModelProviderFactory;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dagger.android.support.DaggerAppCompatActivity;

public class RecordScreenActivity2 extends DaggerAppCompatActivity implements CameraXRecorder.CameraXCallback {

    private static final int REQUEST_CODE_PERMISSIONS = 1;
    private static final int MAX_VID_DURATION = 30;
    private static final String[] REQUIRED_PERMISSIONS = {Manifest.permission.CAMERA
                    , Manifest.permission.RECORD_AUDIO
                    , Manifest.permission.WRITE_EXTERNAL_STORAGE
                    , Manifest.permission.READ_EXTERNAL_STORAGE};

    @Inject
    ViewModelProviderFactory providerFactory;
    @Inject
    LoadingDialog loadingDialog;
    @Inject
    Utils utils;
    @BindView(R.id.viewFinder)
    PreviewView previewView;
    @BindView(R.id.video_capture_button)
    ImageButton videoCaptureButton;
    @BindView(R.id.recorded_time_tv)
    TextView recordedSecondsTV;
    private CameraXRecorder cameraXRecorder;
    private int recordedSeconds;
    private Handler handler = new Handler();
    private Runnable updateEverySecRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_screen2);
        ButterKnife.bind(this);

        // Request camera permissions
        if (allPermissionsGranted()) {
            cameraXRecorder = new CameraXRecorder(this, previewView, this);
            cameraXRecorder.setupCameraX();
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }
    }


    @OnClick(R.id.video_capture_button)
    public void onVideoRecordClicked() {
        cameraXRecorder.recordVideo();
    }

    @Override
    public void onPreparingRecording() {
        videoCaptureButton.setEnabled(false);
    }

    @Override
    public void onStartRecording() {
        videoCaptureButton.setEnabled(true);
        videoCaptureButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.stop_record));
        updateEverySecRunnable = new Runnable() {
            @Override
            public void run() {
                recordedSeconds++;
                String seconds = recordedSeconds < 10 ? "0" + recordedSeconds : recordedSeconds + "";
                recordedSecondsTV.setText(String.format("00:%s", seconds));
                if (recordedSeconds == MAX_VID_DURATION) {
                    cameraXRecorder.stopRecording();
                    handler.removeCallbacks(this);
                    Toast.makeText(RecordScreenActivity2.this, "Max length duration is 30 seconds", Toast.LENGTH_SHORT).show();
                    return;
                }
                handler.postDelayed(this, 1000);
            }
        };
        handler.postDelayed(updateEverySecRunnable, 1000);
    }

    @Override
    public void onStopRecording(Uri videoUri) {
        recordedSeconds = 0;
        videoCaptureButton.setEnabled(true);
        videoCaptureButton.setImageDrawable(ContextCompat.getDrawable(RecordScreenActivity2.this, R.drawable.start_record));
        handler.removeCallbacks(updateEverySecRunnable);
        String fileType = utils.getFieType(videoUri);
        startUploadingVideoService(videoUri, fileType);
    }

    private void startUploadingVideoService(Uri videoUri, String fileType) {
        Intent uploadServiceIntent = new Intent(this, MyUploadService.class);
        uploadServiceIntent.putExtra(MyUploadService.EXTRA_FILE_URI, videoUri);
        uploadServiceIntent.putExtra(MyUploadService.FILE_TYPE, fileType);
        uploadServiceIntent.setAction(MyUploadService.ACTION_UPLOAD);
        startService(uploadServiceIntent);
        Toast.makeText(this, R.string.uploading_in_progress, Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public void onError(String error) {
        Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(updateEverySecRunnable);
    }

    @Override
    protected void onStop() {
        super.onStop();
        handler.removeCallbacks(updateEverySecRunnable);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                cameraXRecorder = new CameraXRecorder(this, previewView, this);
                cameraXRecorder.setupCameraX();
            } else {
                Toast.makeText(this, "Permissions not granted by the user..", Toast.LENGTH_SHORT).show();
                finish();
            }
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
}