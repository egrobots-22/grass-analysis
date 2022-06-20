package com.egrobots.grassanalysis.presentation.videos;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.egrobots.grassanalysis.R;
import com.egrobots.grassanalysis.adapters.ViewPagerAdapter;
import com.egrobots.grassanalysis.data.model.QuestionItem;
import com.egrobots.grassanalysis.presentation.recordscreen.RecordScreenActivity2;
import com.egrobots.grassanalysis.utils.Constants;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;
import androidx.viewpager2.widget.ViewPager2;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_videos_tab);
        ButterKnife.bind(this);
        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }
        initView();
        registerForActivityResult();
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
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK){
                        Intent questionIntent = new Intent(this, RecordScreenActivity2.class);
                        Intent data = result.getData();
                        if (data != null && data.getClipData() != null) {
                            //if multiple images are selected
                            ArrayList<Uri> selectedUris = new ArrayList<>();
                            for (int i = 0; i < data.getClipData().getItemCount(); i++) {
                                Uri imageUri = data.getClipData().getItemAt(i).getUri();
                                //check type of selected item, send error message if video is selected with image
                                if (getContentResolver().getType(imageUri).contains("video")) {
                                    Toast.makeText(VideosTabActivity.this, "لا يمكنك تحميل فيديو مع صورة أو أكثر من فيديو", Toast.LENGTH_LONG).show();
                                    return;
                                }
                                selectedUris.add(imageUri);
                            }
                            questionIntent.putExtra(Constants.RECORD_TYPE, QuestionItem.RecordType.MUTLIPLE_IMAGES);
                            questionIntent.putParcelableArrayListExtra(Constants.SELECTED_MULTIPLE_IMAGES, selectedUris);
                            startActivity(questionIntent);
                        } else if (data.getData() != null) {
                            //if single image is selected
                            Uri fileUri = data.getData();
                            if (fileUri != null) {
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
                        }
                    }
                });
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

    @OnClick(R.id.record_new_video)
    public void onRecordVideoClicked() {
        Intent intent = new Intent(this, RecordScreenActivity2.class);
        intent.putExtra(Constants.RECORD_TYPE, QuestionItem.RecordType.VIDEO);
        startActivity(intent);
    }

    @OnClick(R.id.capture_new_image)
    public void onCaptureImageClicked() {
        Intent intent = new Intent(this, RecordScreenActivity2.class);
        intent.putExtra(Constants.RECORD_TYPE, QuestionItem.RecordType.IMAGE);
        startActivity(intent);
    }

    @OnClick(R.id.open_gallery)
    public void onOpenGalleryClicked() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        photoPickerIntent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"image/*", "video/*"});
        photoPickerIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        photoPickerIntent.setAction(Intent.ACTION_GET_CONTENT);
        openGalleryLauncher.launch(photoPickerIntent);
    }

}