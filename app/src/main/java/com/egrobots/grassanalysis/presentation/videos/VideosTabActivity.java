package com.egrobots.grassanalysis.presentation.videos;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import com.egrobots.grassanalysis.R;
import com.egrobots.grassanalysis.adapters.ViewPagerAdapter;
import com.egrobots.grassanalysis.data.model.QuestionItem;
import com.egrobots.grassanalysis.presentation.recordscreen.RecordScreenActivity2;
import com.egrobots.grassanalysis.utils.Constants;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.viewpager2.widget.ViewPager2;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dagger.android.support.DaggerAppCompatActivity;

public class VideosTabActivity extends DaggerAppCompatActivity {

    private ActivityResultLauncher<String> openGalleryLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_videos_tab);
        ButterKnife.bind(this);
        initView();
        registerForActivityResult();
    }

    private void registerForActivityResult() {
        openGalleryLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                fileUri -> {
                    if (fileUri != null) {
                        String type = getContentResolver().getType(fileUri);
                        Intent intent = new Intent(this, RecordScreenActivity2.class);
                        if (type.contains("video")) {
                            intent.putExtra(Constants.RECORD_TYPE, QuestionItem.RecordType.VIDEO);
                        } else if (type.contains("image")) {
                            intent.putExtra(Constants.RECORD_TYPE, QuestionItem.RecordType.IMAGE);
                        } else {
                            Toast.makeText(VideosTabActivity.this, "Not supported file", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        intent.putExtra(Constants.SELECTED_IMAGE_VIDEO, fileUri);
                        startActivity(intent);
                    }
                });
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
        photoPickerIntent.setType("*/*");
        photoPickerIntent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"image/*", "video/*"});
        openGalleryLauncher.launch("*/*");
    }

}