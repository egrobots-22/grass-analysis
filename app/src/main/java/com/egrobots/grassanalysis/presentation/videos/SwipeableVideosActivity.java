package com.egrobots.grassanalysis.presentation.videos;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import com.egrobots.grassanalysis.R;
import com.egrobots.grassanalysis.adapters.VideosAdapter;
import com.egrobots.grassanalysis.data.model.VideoItem;
import com.egrobots.grassanalysis.data.model.VideoQuestionItem;
import com.egrobots.grassanalysis.utils.ViewModelProviderFactory;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;
import butterknife.BindView;
import butterknife.ButterKnife;
import dagger.android.support.DaggerAppCompatActivity;

public class SwipeableVideosActivity extends DaggerAppCompatActivity {
    private static final String TAG = SwipeableVideosActivity.class.getSimpleName();
    @BindView(R.id.viewPagerVideos)
    ViewPager2 viewPagerVideos;
    @Inject
    ViewModelProviderFactory providerFactory;
    private List<VideoQuestionItem> videoItems = new ArrayList<>();
    private VideosAdapter videosAdapter;
    private SwipeableVideosViewModel swipeableVideosViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_swipeable_videos);
        ButterKnife.bind(this);

        swipeableVideosViewModel = new ViewModelProvider(getViewModelStore(), providerFactory).get(SwipeableVideosViewModel.class);
        swipeableVideosViewModel.getAllVideos();
        videosAdapter = new VideosAdapter( this, videoItems);
        viewPagerVideos.setAdapter(videosAdapter);
        observeVideosUris();
    }

    private void observeVideosUris() {
        swipeableVideosViewModel.observeVideoUris().observe(this, new Observer<VideoQuestionItem>() {
            @Override
            public void onChanged(VideoQuestionItem videoQuestionItem) {
                videoItems.add(videoQuestionItem);
                videosAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 0) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Audio Permission Granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Audio Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}