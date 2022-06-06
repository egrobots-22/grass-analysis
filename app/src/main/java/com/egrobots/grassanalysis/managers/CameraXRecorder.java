package com.egrobots.grassanalysis.managers;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.widget.Toast;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
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
import androidx.core.content.ContextCompat;
import androidx.core.content.PermissionChecker;
import androidx.lifecycle.LifecycleOwner;

public class CameraXRecorder {

    private Context context;
    private PreviewView previewView;
    private VideoCapture<Recorder> videoCapture;
    private Recording recording;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private CameraXCallback cameraXCallback;
    private ProcessCameraProvider cameraProvider;

    public CameraXRecorder(Context context, PreviewView previewView, CameraXCallback cameraXCallback) {
        this.context = context;
        this.previewView = previewView;
        this.cameraXCallback = cameraXCallback;
    }

    private Executor getExecutor() {
        return ContextCompat.getMainExecutor(context);
    }

    public void setupCameraX() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(context);
        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                startCameraX();
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, getExecutor());
    }

    @SuppressLint("RestrictedApi")
    private void startCameraX() {
        cameraProvider.unbindAll();
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        // Video capture use case
        Recorder recorder = new Recorder.Builder()
                .setQualitySelector(QualitySelector.from(Quality.HD))
                .build();




        videoCapture = VideoCapture.withOutput(recorder);

        //bind to lifecycle:
        cameraProvider.bindToLifecycle((LifecycleOwner) context, cameraSelector, preview, videoCapture);
    }

    @SuppressLint("RestrictedApi")
    public void recordVideo() {
        if (videoCapture != null) {
            cameraXCallback.onPreparingRecording();
            Recording curRecording = recording;
            if (curRecording != null) {
                curRecording.stop();
                recording = null;
                cameraProvider.unbindAll();
                return;
            }

            long timestamp = System.currentTimeMillis();

            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, timestamp);
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4");
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                contentValues.put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/CameraX-Video");
            }

            MediaStoreOutputOptions mediaStoreOutputOptions = new MediaStoreOutputOptions
                    .Builder(context.getContentResolver(), MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
                    .setContentValues(contentValues)
                    .build();

            PendingRecording pendingRecording = videoCapture.getOutput().prepareRecording(context, mediaStoreOutputOptions);
            if (PermissionChecker.checkSelfPermission
                    (context, Manifest.permission.RECORD_AUDIO) == PermissionChecker.PERMISSION_GRANTED) {
                pendingRecording.withAudioEnabled();
            }

            recording = pendingRecording.start(getExecutor(), videoRecordEvent -> {
                if (videoRecordEvent instanceof VideoRecordEvent.Start) {
                    cameraXCallback.onStartRecording();
                } else if (videoRecordEvent instanceof VideoRecordEvent.Finalize) {
                    if (!((VideoRecordEvent.Finalize) videoRecordEvent).hasError()) {
                        Uri videoUri = ((VideoRecordEvent.Finalize) videoRecordEvent).getOutputResults().getOutputUri();
                        cameraXCallback.onStopRecording(videoUri);
                        Toast.makeText(context, "Video is saved on device", Toast.LENGTH_SHORT).show();
                    } else {
                        if (recording != null) {
                            recording.close();
                            recording = null;
                        }
                        cameraXCallback.onError("Error");
                    }
                }
            });
        }
    }

    public void stopRecording() {
        recordVideo();
    }

    public interface CameraXCallback {
        void onPreparingRecording();
        void onStartRecording();
        void onStopRecording(Uri videoUri);
        void onError(String error);
    }
}