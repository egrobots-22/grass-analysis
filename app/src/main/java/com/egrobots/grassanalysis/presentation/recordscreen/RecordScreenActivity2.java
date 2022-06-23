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
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageSwitcher;
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
import com.egrobots.grassanalysis.utils.FFMpegHelper;
import com.egrobots.grassanalysis.utils.LoadingDialog;
import com.egrobots.grassanalysis.utils.OpenGalleryActivityResultCallback;
import com.egrobots.grassanalysis.utils.Utils;
import com.egrobots.grassanalysis.utils.ViewModelProviderFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
    @BindView(R.id.multiple_images_view)
    View multipleImagesView;
    @BindView(R.id.image_switcher)
    ImageSwitcher imageSwitcher;
    @BindView(R.id.prevImageButton)
    ImageButton prevImageButton;
    @BindView(R.id.nextImageButton)
    ImageButton nextImageButton;
    @BindView(R.id.add_image_button)
    ImageButton addImageButton;
    @BindView(R.id.delete_image_button)
    ImageButton deleteImageButton;

    private CameraXRecorder cameraXRecorder;
    private int recordedSeconds;
    private Handler handler = new Handler();
    private Runnable updateEverySecRunnable;
    private Uri fileUri;
    private List<Uri> imagesUris;
    private ExoPlayerVideoManager exoPlayerManager;
    private QuestionItem.RecordType recordType;
    private AudioRecorder audioRecorder = new AudioRecorder();
    private File audioRecordedFile;
    private boolean isAudioRecordingStarted;
    private int selectedImagePosition;
    private ActivityResultLauncher openGalleryLauncher;
    private boolean isUsingCamera;
    private boolean isAddingNewImage = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_screen2);
        ButterKnife.bind(this);
        registerForActivityResult();
        registerClickListenersForPrevNextButtons();
        initializeImageSwitcher();
        //get type of camera
        if (getIntent() != null) {
            recordType = (QuestionItem.RecordType) getIntent().getExtras().get(Constants.RECORD_TYPE);
            fileUri = getIntent().getParcelableExtra(Constants.SELECTED_IMAGE_VIDEO);
            imagesUris = getIntent().getParcelableArrayListExtra(Constants.SELECTED_MULTIPLE_IMAGES);
            if (imagesUris == null)
                imagesUris = new ArrayList<>();

            if (fileUri == null && imagesUris.size() == 0) {
                isUsingCamera = true;
                addImageButton.setVisibility(View.GONE);
                deleteImageButton.setVisibility(View.GONE);
                //no selected files, so we'll record images or videos
                initializeCameraX();
            } else {
                //there are selected files
                isUsingCamera = false;
                if (recordType == QuestionItem.RecordType.VIDEO) {
//                    fileUri = Uri.fromFile(new File(fileUri.getPath()));
                    initializeSelectedVideoType();
                } else if (recordType == QuestionItem.RecordType.IMAGE) {
                    initializeSelectedImageType();
                } else if (recordType == QuestionItem.RecordType.MUTLIPLE_IMAGES) {
                    initializeSelectedMultipleImagesType();
                }
            }
        }
    }

    private void initializeSelectedVideoType() {
        addImageButton.setVisibility(View.GONE);
        deleteImageButton.setVisibility(View.GONE);
        showRecordedVideo(fileUri);
        //show length of selected video
        long videoLength = getIntent().getLongExtra(Constants.VIDEO_LENGTH, 0);
        recordedSecondsTV.setVisibility(View.VISIBLE);
        recordedSecondsTV.setText(Utils.formatMilliSeconds(videoLength));
    }

    private void initializeSelectedImageType() {
        //add selected image to the image switcher view
        imagesUris = new ArrayList<>();
        imagesUris.add(fileUri);
        initializeSelectedMultipleImagesType();
        addImageButton.setVisibility(View.VISIBLE);
    }

    private void initializeSelectedMultipleImagesType() {
        //show multiple images view
        multipleImagesView.setVisibility(View.VISIBLE);
        // showing all images in image switcher
        selectedImagePosition = 0;
        imageSwitcher.setImageURI(imagesUris.get(selectedImagePosition));
        //showing buttons
        addImageButton.setVisibility(View.VISIBLE);
        if (imagesUris.size() == 1) {
            deleteImageButton.setVisibility(View.GONE);
        } else if (imagesUris.size() > 1) {
            nextImageButton.setVisibility(View.VISIBLE);
            deleteImageButton.setVisibility(View.VISIBLE);
        }
        //show record audio button to enable user recording audio
        videoCaptureButton.setEnabled(true);
        videoCaptureButton.setImageDrawable(ContextCompat.getDrawable(RecordScreenActivity2.this, R.drawable.recording_audio));
        //register listeners for previous & next button
//        registerClickListenersForPrevNextButtons();
    }

    private void initializeImageSwitcher() {
        imageSwitcher.setFactory(() -> {
            FrameLayout.LayoutParams layoutParams
                    = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
            ImageView imgView = new ImageView(this);
            imgView.setLayoutParams(layoutParams);
            imgView.setScaleType(ImageView.ScaleType.FIT_XY);
            return imgView;
        });
    }

    private void registerClickListenersForPrevNextButtons() {
        nextImageButton.setOnClickListener(v -> {
            ++selectedImagePosition;
            prevImageButton.setVisibility(View.VISIBLE);
            if (selectedImagePosition == imagesUris.size() - 1) {
                nextImageButton.setVisibility(View.GONE);
            }
            imageSwitcher.setImageURI(imagesUris.get(selectedImagePosition));
        });

        prevImageButton.setOnClickListener(v -> {
            --selectedImagePosition;
            nextImageButton.setVisibility(View.VISIBLE);
            if (selectedImagePosition == 0) {
                prevImageButton.setVisibility(View.GONE);
            }
            imageSwitcher.setImageURI(imagesUris.get(selectedImagePosition));
        });
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
                if (isAddingNewImage) {
                    //no image captured yet
                    cameraXRecorder.captureImage();
                } else {
                    if (!isAudioRecordingStarted) {
                        //recording audio is not started yet, so start it
                        onStartRecordingAudio();
                    } else {
                        onStopRecordingAudio();
                    }
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
        recordedSecondsTV.setVisibility(View.VISIBLE);
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
        if (imagesUris != null && imagesUris.size() > 1) {
            //set record type to multiple images, to upload them as video not single image
            recordType = QuestionItem.RecordType.MUTLIPLE_IMAGES;
        }
        if (fileUri != null) {
            String fileType = utils.getFieType(fileUri);
            if (fileType != null) {
                if (recordType == QuestionItem.RecordType.VIDEO) {
                    startUploadingVideoService(fileUri, null, fileType, true);
                } else if (recordType == QuestionItem.RecordType.MUTLIPLE_IMAGES) {
                    startUploadingVideoService(fileUri, null, fileType, false);
                } else if (recordType == QuestionItem.RecordType.IMAGE) {
//                Toast.makeText(this, "start uploading", Toast.LENGTH_LONG).show();
                    startUploadingVideoService(fileUri, audioRecordedFile.getPath(), fileType, false);
                }
            } else {
                Toast.makeText(this, "خطأ غير معروف", Toast.LENGTH_SHORT).show();
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
        recordedSecondsTV.setVisibility(View.GONE);
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

    @OnClick(R.id.add_image_button)
    public void onAddImageClicked() {
        if (isUsingCamera) {
            initializeCameraX();
            fileUri = null; //to capture new image
            previewView.setVisibility(View.VISIBLE);
            multipleImagesView.setVisibility(View.GONE);
            videoCaptureButton.setEnabled(true);
            videoCaptureButton.setImageDrawable(ContextCompat.getDrawable(RecordScreenActivity2.this, R.drawable.start_record));
            isAddingNewImage = true;
//            cameraXRecorder.captureImage(false);
        } else {
            Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
            photoPickerIntent.setType("image/*");
            photoPickerIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            photoPickerIntent.setAction(Intent.ACTION_GET_CONTENT);
            openGalleryLauncher.launch(photoPickerIntent);
        }
    }

    @OnClick(R.id.delete_image_button)
    public void onDeleteImageClicked() {
        imagesUris.remove(selectedImagePosition);
        if (selectedImagePosition != 0) {
            --selectedImagePosition;
        }
        imageSwitcher.setImageURI(imagesUris.get(selectedImagePosition));
        if (imagesUris.size() > 1) {
            deleteImageButton.setVisibility(View.VISIBLE);
        } else if (imagesUris.size() == 1) {
            deleteImageButton.setVisibility(View.GONE);
            prevImageButton.setVisibility(View.GONE);
            nextImageButton.setVisibility(View.GONE);
            return;
        }
        if (selectedImagePosition == 0) {
            nextImageButton.setVisibility(View.VISIBLE);
            prevImageButton.setVisibility(View.GONE);
        } else if (selectedImagePosition == imagesUris.size() - 1) {
            nextImageButton.setVisibility(View.GONE);
            prevImageButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onCaptureImage(Uri imageUri) {
//        fileUri = imageUri;
        //show image view
//            imageView.setVisibility(View.VISIBLE);
//            imageView.setImageURI(imageUri);
        isAddingNewImage = false;
        previewView.setVisibility(View.GONE);
        multipleImagesView.setVisibility(View.VISIBLE);
        addImageButton.setVisibility(View.VISIBLE);
        videoCaptureButton.setEnabled(true);
        videoCaptureButton.setImageDrawable(ContextCompat.getDrawable(RecordScreenActivity2.this, R.drawable.recording_audio));

        imagesUris.add(imageUri);
        selectedImagePosition = imagesUris.size() - 1;
        imageSwitcher.setImageURI(imagesUris.get(selectedImagePosition));
        if (imagesUris.size() > 1) {
            deleteImageButton.setVisibility(View.VISIBLE);
        } else {
            deleteImageButton.setVisibility(View.GONE);
            prevImageButton.setVisibility(View.GONE);
            return;
        }

        if (selectedImagePosition == 0) {
            nextImageButton.setVisibility(View.VISIBLE);
            prevImageButton.setVisibility(View.GONE);
        } else if (selectedImagePosition == imagesUris.size() - 1) {
            nextImageButton.setVisibility(View.GONE);
            prevImageButton.setVisibility(View.VISIBLE);
        }
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
        recordedSecondsTV.setVisibility(View.VISIBLE);
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
        addImageButton.setVisibility(View.GONE);
        deleteImageButton.setVisibility(View.GONE);
        //stop recorded audio
        audioRecorder.stop();
        if (imagesUris != null && imagesUris.size() > 1) {
            //more than one image is selected, so combine them with the recorded audio in video and show it
            LoadingDialog loadingDialog = new LoadingDialog();
            loadingDialog.show(getSupportFragmentManager(), null);
            Toast.makeText(this, "Images will be combined in video with the recorded audio and show it here", Toast.LENGTH_SHORT).show();
            String output = getExternalFilesDir(null) + "/" + UUID.randomUUID().toString() + Constants.VIDEO_FILE_TYPE;
            FFMpegHelper ffMpegHelper = new FFMpegHelper();
            ffMpegHelper.convertImagesWithAudioToVideo(imagesUris, audioRecordedFile.getPath(), output,
                    new FFMpegHelper.FFMpegCallback() {
                        @Override
                        public void onFFMpegExecSuccess(String output) {
                            loadingDialog.dismiss();
                            fileUri = Uri.fromFile(new File(output));
                            showRecordedVideo(fileUri);
                            multipleImagesView.setVisibility(View.GONE);
                        }

                        @Override
                        public void onFFMpegExecError(String error) {
                            Toast.makeText(RecordScreenActivity2.this, error, Toast.LENGTH_SHORT).show();
                            loadingDialog.dismiss();
                        }

                        @Override
                        public void onFFMpegExecCancel() {
                            Toast.makeText(RecordScreenActivity2.this, "cancelled", Toast.LENGTH_SHORT).show();
                            loadingDialog.dismiss();
                        }

                        @Override
                        public void onFFmMpegExecProgress(long progress, long total) {

                        }
                    }, this);
        } else {
            //only one image is selected with recorded audio
            //show review view to accept or cancel video
            reviewView.setVisibility(View.VISIBLE);
            //hide record button
            videoCaptureButton.setVisibility(View.GONE);
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
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), fileUri);
                Drawable drawable = new BitmapDrawable(getResources(), bitmap);
                exoPlayerManager.setCapturedImageToPlayer(drawable);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void deleteRecordedAudio() {
        if (audioRecordedFile != null && audioRecordedFile.exists()) {
            audioRecordedFile.delete();
        }
    }

    private void startUploadingVideoService(Uri fileUri, String questionAudioUri, String fileType, boolean compressVideo) {
        Intent uploadServiceIntent = new Intent(this, MyUploadService.class);
        uploadServiceIntent.putExtra(MyUploadService.EXTRA_FILE_URI, fileUri);
        uploadServiceIntent.putExtra(MyUploadService.EXTRA_AUDIO_URI, questionAudioUri);
        uploadServiceIntent.putExtra(MyUploadService.FILE_TYPE, fileType);
        uploadServiceIntent.putExtra(MyUploadService.RECORD_TYPE, recordType);
        uploadServiceIntent.putExtra(MyUploadService.COMPRESS_VIDEO_EXTRA, compressVideo);
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

    private void registerForActivityResult() {
        openGalleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new OpenGalleryActivityResultCallback(new OpenGalleryActivityResultCallback.OpenGalleryCallback() {

                    @Override
                    public void onSingleImageOrVideoSelected(Uri fileUri) {
                        imagesUris.add(fileUri);
                        imageSwitcher.setImageURI(fileUri);
                        ++selectedImagePosition;
                        if (imagesUris.size() > 1) {
                            deleteImageButton.setVisibility(View.VISIBLE);
                        } else {
                            deleteImageButton.setVisibility(View.GONE);
                        }
                        if (selectedImagePosition == 0) {
                            nextImageButton.setVisibility(View.VISIBLE);
                            prevImageButton.setVisibility(View.GONE);
                        } else if (selectedImagePosition == imagesUris.size() - 1) {
                            nextImageButton.setVisibility(View.GONE);
                            prevImageButton.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onMultipleImagesSelected(ArrayList<Uri> uris) {
                        imagesUris.addAll(uris);
                        imageSwitcher.setImageURI(imagesUris.get(imagesUris.size() - 1));
                        selectedImagePosition = imagesUris.size() - 1;
                        if (imagesUris.size() > 1) {
                            deleteImageButton.setVisibility(View.VISIBLE);
                        } else {
                            deleteImageButton.setVisibility(View.GONE);
                        }
                        if (selectedImagePosition == 0) {
                            nextImageButton.setVisibility(View.VISIBLE);
                            prevImageButton.setVisibility(View.GONE);
                        } else if (selectedImagePosition == imagesUris.size() - 1) {
                            nextImageButton.setVisibility(View.GONE);
                            prevImageButton.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onError(String error) {
                        Toast.makeText(RecordScreenActivity2.this, error, Toast.LENGTH_SHORT).show();
                    }
                }));
    }

}