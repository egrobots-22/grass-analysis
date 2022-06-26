package com.egrobots.grassanalysis.presentation.videos;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.egrobots.grassanalysis.R;
import com.egrobots.grassanalysis.adapters.ViewPagerAdapter;
import com.egrobots.grassanalysis.data.model.QuestionItem;
import com.egrobots.grassanalysis.presentation.recordscreen.RecordScreenActivity2;
import com.egrobots.grassanalysis.utils.Constants;
import com.egrobots.grassanalysis.utils.OpenGalleryActivityResultCallback;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;
import androidx.viewpager2.widget.ViewPager2;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dagger.android.support.DaggerAppCompatActivity;

public class VideosTabActivity extends DaggerAppCompatActivity {

    private static final int REQUEST_CODE_PERMISSIONS = 1;

    private static final String[] REQUIRED_PERMISSIONS = {Manifest.permission.CAMERA
            , Manifest.permission.RECORD_AUDIO
            , Manifest.permission.WRITE_EXTERNAL_STORAGE
            , Manifest.permission.READ_EXTERNAL_STORAGE};

    private ActivityResultLauncher openGalleryLauncher;

    @BindView(R.id.add_question)
    ExtendedFloatingActionButton mAddQuestion;
    @BindView(R.id.add_from_gallery_fab)
    FloatingActionButton addFromGalleryFab;
    @BindView(R.id.capture_video_fab)
    FloatingActionButton captureVideoFab;
    @BindView(R.id.capture_image_fab)
    FloatingActionButton captureImageFab;
    private boolean isAllFabsVisible;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_videos_tab);
        ButterKnife.bind(this);

        addFromGalleryFab.setVisibility(View.GONE);
        captureImageFab.setVisibility(View.GONE);
        captureVideoFab.setVisibility(View.GONE);
        isAllFabsVisible = false;
        mAddQuestion.shrink();

        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }
        initView();
        registerForActivityResult();
    }

    @OnClick(R.id.add_question)
    public void onAddQuestionClicked() {
        if (!isAllFabsVisible) {
            addFromGalleryFab.show();
            captureImageFab.show();
            captureVideoFab.show();
            mAddQuestion.extend();
            isAllFabsVisible = true;
        } else {
            addFromGalleryFab.hide();
            captureImageFab.hide();
            captureVideoFab.hide();
            mAddQuestion.shrink();
            isAllFabsVisible = false;
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
                    Intent questionIntent = new Intent(VideosTabActivity.this, RecordScreenActivity2.class);

                    @Override
                    public void onSingleImageOrVideoSelected(Uri fileUri) {
                        String type = getContentResolver().getType(fileUri);
                        if (type.contains("video")) {
                            questionIntent.putExtra(Constants.RECORD_TYPE, QuestionItem.RecordType.VIDEO);
                            //get video length
                            long videoLength = getVideoLength(fileUri);
                            questionIntent.putExtra(Constants.VIDEO_LENGTH, videoLength);
                            if (videoLength > TimeUnit.SECONDS.toMillis(Constants.MAX_VID_DURATION_SEC)) {
                                Toast.makeText(VideosTabActivity.this, R.string.cant_upload_video_max_cause, Toast.LENGTH_LONG).show();
                                return;
                            }
                        } else if (type.contains("image")) {
                            questionIntent.putExtra(Constants.RECORD_TYPE, QuestionItem.RecordType.IMAGE);
                        } else {
                            Toast.makeText(VideosTabActivity.this, "Not supported file", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        questionIntent.putExtra(Constants.SELECTED_IMAGE_VIDEO, fileUri);
                        startActivity(questionIntent);
                    }

                    @Override
                    public void onMultipleImagesSelected(ArrayList<Uri> imagesUris) {
                        for (Uri uri : imagesUris) {
                            if (getContentResolver().getType(uri).contains("video")) {
                                Toast.makeText(VideosTabActivity.this, "لا يمكنك تحميل فيديو مع صورة أو أكثر من فيديو", Toast.LENGTH_LONG).show();
                                return;
                            }
                        }
                        questionIntent.putExtra(Constants.RECORD_TYPE, QuestionItem.RecordType.MUTLIPLE_IMAGES);
                        questionIntent.putParcelableArrayListExtra(Constants.SELECTED_MULTIPLE_IMAGES, imagesUris);
                        startActivity(questionIntent);
                    }

                    @Override
                    public void onError(String error) {
                        Toast.makeText(VideosTabActivity.this, error, Toast.LENGTH_SHORT).show();
                    }
                }));
    }

    private long getVideoLength(Uri fileUri) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(this, fileUri);
        String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        long timeInMilliSec = Long.parseLong(time);
        retriever.release();
        return timeInMilliSec;
    }

    private void initView() {
        ViewPager2 viewPager = findViewById(R.id.view_pager);
        viewPager.setUserInputEnabled(false);
        TabLayout tabLayout = findViewById(R.id.tabs);
        viewPager.setAdapter(new ViewPagerAdapter(this));
        TabLayoutMediator tabLayoutMediator = new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    if (position == 0) {
                        tab.setText(R.string.your_questions);
                    } else {
                        tab.setText(R.string.other_questions);
                    }
                });
        tabLayoutMediator.attach();
    }

    @OnClick(R.id.capture_video_fab)
    public void onRecordVideoClicked() {
        Intent intent = new Intent(this, RecordScreenActivity2.class);
        intent.putExtra(Constants.RECORD_TYPE, QuestionItem.RecordType.VIDEO);
        startActivity(intent);
    }

    @OnClick(R.id.capture_image_fab)
    public void onCaptureImageClicked() {
        Intent intent = new Intent(this, RecordScreenActivity2.class);
        intent.putExtra(Constants.RECORD_TYPE, QuestionItem.RecordType.IMAGE);
        startActivity(intent);
    }

    @OnClick(R.id.add_from_gallery_fab)
    public void onOpenGalleryClicked() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        photoPickerIntent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"image/*", "video/*"});
        photoPickerIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        photoPickerIntent.setAction(Intent.ACTION_GET_CONTENT);
        openGalleryLauncher.launch(photoPickerIntent);
    }

}