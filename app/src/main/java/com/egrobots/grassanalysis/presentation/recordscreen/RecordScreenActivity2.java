package com.egrobots.grassanalysis.presentation.recordscreen;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.arthenica.mobileffmpeg.Config;
import com.arthenica.mobileffmpeg.ExecuteCallback;
import com.arthenica.mobileffmpeg.FFmpeg;
import com.egrobots.grassanalysis.R;
import com.egrobots.grassanalysis.managers.CameraXRecorder;
import com.egrobots.grassanalysis.utils.LoadingDialog;
import com.egrobots.grassanalysis.utils.Utils;
import com.egrobots.grassanalysis.utils.ViewModelProviderFactory;

import java.io.File;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProvider;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dagger.android.support.DaggerAppCompatActivity;

import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_CANCEL;
import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS;

public class RecordScreenActivity2 extends DaggerAppCompatActivity implements CameraXRecorder.CameraXCallback {

    private static final int REQUEST_CODE_PERMISSIONS = 1;
    private static final String[] REQUIRED_PERMISSIONS =
            {Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE};

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
    private RecordScreenViewModel recordScreenViewModel;
    private CameraXRecorder cameraXRecorder;
    private int recordedSeconds;
    private int MAX_VID_DURATION = 30;
    private Handler handler = new Handler();
    private Runnable updateEverySecRunnable;
    private ProgressDialog pd;
    private boolean compressVideo = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_screen2);
        ButterKnife.bind(this);

        recordScreenViewModel = new ViewModelProvider(getViewModelStore(), providerFactory).get(RecordScreenViewModel.class);
        // Request camera permissions
        if (allPermissionsGranted()) {
            cameraXRecorder = new CameraXRecorder(this, previewView, this);
            cameraXRecorder.setupCameraX();
            observeStatusChange();
            observerUploadingProgress();
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }
    }

    private void observerUploadingProgress() {
        recordScreenViewModel.observeUploadingProgress().observe(this, progress -> {
            loadingDialog.setTitle(getString(R.string.uploaded) + (int) progress.doubleValue() + "%");
        });
    }

    private void observeStatusChange() {
        recordScreenViewModel.observeStatusChange().observe(this, stateResource -> {
            if (stateResource != null) {
                switch (stateResource.status) {
                    case LOADING:
                        loadingDialog.show(getSupportFragmentManager(), "loading_dialog");
                        break;
                    case SUCCESS:
                        loadingDialog.dismiss();
                        finish();
                        Toast.makeText(this, "Video uploaded Successfully", Toast.LENGTH_SHORT).show();
                        break;
                    case ERROR:
                        loadingDialog.dismiss();
                        Toast.makeText(this, stateResource.message, Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });
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
        if (!compressVideo) {
            String fileType = utils.getFieType(videoUri);
            recordScreenViewModel.uploadVideo(videoUri, fileType);
        } else {
            pd = new ProgressDialog(this);
            pd.setTitle("من فضلك انتظر");
            pd.show();
            String input = utils.getPathFromUri(videoUri);
            String output = utils.getCompressedPath(videoUri);
            execFFmpegBinary(input, output);
        }

    }

    private void execFFmpegBinary(String input, String output) {
        try {
            Log.i(Config.TAG, "input file path : " + input);
            Log.i(Config.TAG, "output file path : " + output);
            String exe = "-i " + input + " -vf scale=1280:720 " + output;
            FFmpeg.executeAsync(exe, new ExecuteCallback() {
                @Override
                public void apply(long executionId, int returnCode) {
                    if (returnCode == RETURN_CODE_SUCCESS) {
                        Uri outputUri = FileProvider.getUriForFile(RecordScreenActivity2.this,
                                getApplicationContext().getPackageName() + ".provider", new File(output));
                        String fileType = utils.getFieType(outputUri);
                        recordScreenViewModel.uploadVideo(outputUri, fileType);
                    } else if (returnCode == RETURN_CODE_CANCEL) {
                        Log.i(Config.TAG, "Async command execution cancelled by user.");
                    } else {
                        Log.i(Config.TAG, String.format("Async command execution failed with returnCode=%d.", returnCode));
                    }
                    pd.dismiss();
                }
            });
        } catch (Exception e) {
            // Mention to user the command is currently running
        }
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
                observeStatusChange();
                observerUploadingProgress();
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