package com.egrobots.grassanalysis.presentation.recordscreen;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.egrobots.grassanalysis.R;
import com.egrobots.grassanalysis.data.model.QuestionItem;
import com.egrobots.grassanalysis.managers.AudioRecorder;
import com.egrobots.grassanalysis.managers.CameraXRecorder;
import com.egrobots.grassanalysis.managers.ExoPlayerVideoManager;
import com.egrobots.grassanalysis.services.MyUploadService;
import com.egrobots.grassanalysis.utils.Constants;
import com.egrobots.grassanalysis.utils.LoadingDialog;
import com.egrobots.grassanalysis.utils.Utils;
import com.egrobots.grassanalysis.utils.ViewModelProviderFactory;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.media3.ui.PlayerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dagger.android.support.DaggerAppCompatActivity;

public class RecordScreenActivity2 extends DaggerAppCompatActivity implements CameraXRecorder.CameraXCallback {

    private static final int REQUEST_CODE_PERMISSIONS = 1;
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
    @BindView(R.id.review_video_view)
    View reviewView;
    @BindView(R.id.videoView)
    PlayerView playerView;
    @BindView(R.id.imageView)
    ImageView imageView;
    private CameraXRecorder cameraXRecorder;
    private int recordedSeconds;
    private Handler handler = new Handler();
    private Runnable updateEverySecRunnable;
    private Uri fileUri;
    private ExoPlayerVideoManager exoPlayerManager;
    private QuestionItem.RecordType recordType;
    private AudioRecorder audioRecorder = new AudioRecorder();
    private File audioRecordedFile;
    private boolean isAudioRecordingStarted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_screen2);
        ButterKnife.bind(this);

        //get type of camera
        if (getIntent() != null) {
            recordType = (QuestionItem.RecordType) getIntent().getExtras().get(Constants.RECORD_TYPE);
            fileUri = getIntent().getParcelableExtra(Constants.SELECTED_IMAGE_VIDEO);
            if (fileUri == null) {
                initializeCameraX();
            } else {
                if (recordType == QuestionItem.RecordType.VIDEO) {
                    showRecordedVideo(fileUri);
                    //show length of selected video
                    long videoLength = getIntent().getLongExtra(Constants.VIDEO_LENGTH, 0);
                    recordedSecondsTV.setVisibility(View.VISIBLE);
                    recordedSecondsTV.setText(Utils.formatMilliSeconds(videoLength));
                } else if (recordType == QuestionItem.RecordType.IMAGE) {
                    onCaptureImage(fileUri);
                }
            }
        }
    }

    private void initializeCameraX() {
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
        if (cameraXRecorder == null) {
            //file is selected, in this case, recording button will be shown only when selecting image to record the audio
            //so here we'll stop recording audio
            if (!isAudioRecordingStarted) {
                onStartRecordingAudio();
            } else {
                onStopRecordingAudio();
            }
        } else {
            //file is recorded
            if (recordType == QuestionItem.RecordType.VIDEO) {
                cameraXRecorder.recordVideo();
            } else {
                if (fileUri == null) {
                    //no image captured yet
                    cameraXRecorder.captureImage();
                } else if (!isAudioRecordingStarted) {
                    //recording audio is not started yet, so start it
                    onStartRecordingAudio();
                } else {
                    //recording audio is started, so stop it by captureImage method to unbind camera provider
                    cameraXRecorder.captureImage();
                }
            }
        }
    }

    @Override
    public void onPreparingRecording() {
        videoCaptureButton.setEnabled(false);
    }

    @Override
    public void onStartRecording() {
        recordedSecondsTV.setText("00:00");
        videoCaptureButton.setEnabled(true);
        videoCaptureButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.stop_record));
        updateEverySecRunnable = new Runnable() {
            @Override
            public void run() {
                ++recordedSeconds;
                String seconds = recordedSeconds < 10 ? "0" + recordedSeconds : recordedSeconds + "";
                recordedSecondsTV.setText(String.format("00:%s", seconds));
                if (recordedSeconds == Constants.MAX_VID_DURATION_SEC) {
                    cameraXRecorder.stopRecording();
                    handler.removeCallbacks(this);
                    Toast.makeText(RecordScreenActivity2.this, R.string.max_video_duarion_exceed, Toast.LENGTH_SHORT).show();
                    return;
                }
                handler.postDelayed(this, 1000);
            }
        };
        handler.postDelayed(updateEverySecRunnable, 1000);
    }

    @Override
    public void onStopRecording(Uri videoUri) {
        fileUri = videoUri;
        recordedSeconds = 0;
        videoCaptureButton.setEnabled(true);
        videoCaptureButton.setImageDrawable(ContextCompat.getDrawable(RecordScreenActivity2.this, R.drawable.start_record));
        handler.removeCallbacks(updateEverySecRunnable);
        showRecordedVideo(videoUri);
    }

    private void showRecordedVideo(Uri videoUri) {
        //show review view to accept or cancel video
        reviewView.setVisibility(View.VISIBLE);
        //hide record button
        videoCaptureButton.setVisibility(View.GONE);
        //show recorded video
        playerView.setVisibility(View.VISIBLE);
        previewView.setVisibility(View.GONE);
        exoPlayerManager = new ExoPlayerVideoManager();
        exoPlayerManager.initializeExoPlayer(this, videoUri.toString());
        exoPlayerManager.initializePlayer(playerView);
    }

    @OnClick(R.id.done_button)
    public void onDoneClicked() {
        if (fileUri != null) {
            String fileType = utils.getFieType(fileUri);
            if (recordType == QuestionItem.RecordType.VIDEO) {
                startUploadingVideoService(fileUri, null, fileType);
            } else if (recordType == QuestionItem.RecordType.IMAGE){
//                Toast.makeText(this, "start uploading", Toast.LENGTH_LONG).show();
                startUploadingVideoService(fileUri, audioRecordedFile.getPath(), fileType);
            }
            //release exoplayer
            if (exoPlayerManager != null) {
                exoPlayerManager.releasePlayer();
            }
        }
    }

    @OnClick(R.id.cancel_button)
    public void onCancelButton() {
        if (exoPlayerManager != null) {
            //release exoplayer
            exoPlayerManager.releasePlayer();
        }
        recordedSeconds = 0;
        recordedSecondsTV.setText("");
        //show camerax view
        previewView.setVisibility(View.VISIBLE);
        playerView.setVisibility(View.GONE);
        imageView.setVisibility(View.GONE);
        //hide review view
        reviewView.setVisibility(View.GONE);
        //show record button
        videoCaptureButton.setVisibility(View.VISIBLE);
        deleteRecordedAudio();
        fileUri = null;
        if (cameraXRecorder == null) {
            //finish activity
            finish();
        } else {
            //reinitialize camera
            initializeCameraX();
        }
    }

    @Override
    public void onCaptureImage(Uri imageUri) {
        fileUri = imageUri;
        //show image view
        imageView.setVisibility(View.VISIBLE);
        imageView.setImageURI(imageUri);
        previewView.setVisibility(View.GONE);
        videoCaptureButton.setEnabled(true);
        videoCaptureButton.setImageDrawable(ContextCompat.getDrawable(RecordScreenActivity2.this, R.drawable.recording_audio));
    }

    @Override
    public void onStartRecordingAudio() {
        isAudioRecordingStarted = true;
        //set button as stop recording
        videoCaptureButton.setEnabled(true);
        videoCaptureButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.stop_record));
        //start recording audio
        audioRecordedFile = new File(getFilesDir().getPath(), UUID.randomUUID().toString() + Constants.AUDIO_FILE_TYPE);
        try {
            audioRecorder.start(this, audioRecordedFile.getPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        recordedSecondsTV.setText("00:00");
        updateEverySecRunnable = new Runnable() {
            @Override
            public void run() {
                ++recordedSeconds;
                String seconds = recordedSeconds < 10 ? "0" + recordedSeconds : recordedSeconds + "";
                recordedSecondsTV.setText(String.format("00:%s", seconds));
                //start recording question audio
                handler.postDelayed(this, 1000);
            }
        };
        handler.postDelayed(updateEverySecRunnable, 1000);
    }

    @Override
    public void onStopRecordingAudio() {
        isAudioRecordingStarted = false;
        handler.removeCallbacks(updateEverySecRunnable);
        videoCaptureButton.setEnabled(true);
        videoCaptureButton.setImageDrawable(ContextCompat.getDrawable(RecordScreenActivity2.this, R.drawable.start_record));
        //show review view to accept or cancel video
        reviewView.setVisibility(View.VISIBLE);
        //hide record button
        videoCaptureButton.setVisibility(View.GONE);
        //stop recorded audio
        audioRecorder.stop();
        //show exoplayer audio
        previewView.setVisibility(View.GONE);
        playerView.setVisibility(View.GONE);
        imageView.setVisibility(View.VISIBLE);
        //show image with audio
        exoPlayerManager = new ExoPlayerVideoManager();
        exoPlayerManager.initializeAudioExoPlayer(this, audioRecordedFile.getPath(), true);
        exoPlayerManager.initializePlayer(playerView);
        //set captured image to the exoplayer
        imageView.setVisibility(View.GONE);
        playerView.setVisibility(View.VISIBLE);
        try {
//            Bitmap bitmap = BitmapFactory.decodeFile(RealPathUtil.getRealPath(this, fileUri));
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), fileUri);
            Drawable drawable = new BitmapDrawable(getResources(), bitmap);
            exoPlayerManager.setCapturedImageToPlayer(drawable);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deleteRecordedAudio() {
        if (audioRecordedFile != null && audioRecordedFile.exists()) {
            audioRecordedFile.delete();
        }
    }

    private void startUploadingVideoService(Uri fileUri, String questionAudioUri, String fileType) {
        Intent uploadServiceIntent = new Intent(this, MyUploadService.class);
        uploadServiceIntent.putExtra(MyUploadService.EXTRA_FILE_URI, fileUri);
        uploadServiceIntent.putExtra(MyUploadService.EXTRA_AUDIO_URI, questionAudioUri);
        uploadServiceIntent.putExtra(MyUploadService.FILE_TYPE, fileType);
        uploadServiceIntent.putExtra(MyUploadService.RECORD_TYPE, recordType);
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
        if (handler != null) {
            handler.removeCallbacks(updateEverySecRunnable);
        }
        if (exoPlayerManager != null) {
            exoPlayerManager.releasePlayer();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (handler != null) {
            handler.removeCallbacks(updateEverySecRunnable);
        }
        if (exoPlayerManager != null) {
            exoPlayerManager.releasePlayer();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (handler != null) {
            handler.removeCallbacks(updateEverySecRunnable);
        }
        if (exoPlayerManager != null) {
            exoPlayerManager.releasePlayer();
        }
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