package com.egrobots.grassanalysis.presentation.videos;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;
import butterknife.BindView;
import butterknife.ButterKnife;
import dagger.android.support.DaggerAppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.egrobots.grassanalysis.R;
import com.egrobots.grassanalysis.adapters.VideosAdapter;
import com.egrobots.grassanalysis.data.model.VideoItem;
import com.egrobots.grassanalysis.presentation.recordscreen.RecordScreenViewModel;
import com.egrobots.grassanalysis.utils.Constants;
import com.egrobots.grassanalysis.utils.ViewModelProviderFactory;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class SwipeableVideosActivity extends DaggerAppCompatActivity implements VideosAdapter.ReplyCallback {
    private static final String TAG = SwipeableVideosActivity.class.getSimpleName();
    @BindView(R.id.viewPagerVideos)
    ViewPager2 viewPagerVideos;
    @Inject
    ViewModelProviderFactory providerFactory;
    private List<VideoItem> videoItems = new ArrayList<>();;
    private VideosAdapter videosAdapter;
    private SwipeableVideosViewModel swipeableVideosViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_swipeable_videos);
        ButterKnife.bind(this);

        swipeableVideosViewModel = new ViewModelProvider(getViewModelStore(), providerFactory).get(SwipeableVideosViewModel.class);
        swipeableVideosViewModel.getAllVideos();
        videosAdapter = new VideosAdapter(videoItems, this);
        viewPagerVideos.setAdapter(videosAdapter);
        observeVideosUris();
    }

    private void observeVideosUris() {
        swipeableVideosViewModel.observeVideoUris().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String videoUri) {
                VideoItem videoItem = new VideoItem();
                videoItem.setVideoUri(videoUri);
                videoItems.add(videoItem);
                videosAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onSendClicked(String reply) {
        Toast.makeText(this, reply, Toast.LENGTH_SHORT).show();
    }
}