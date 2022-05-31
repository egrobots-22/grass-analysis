package com.egrobots.grassanalysis.presentation.videos;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import butterknife.BindView;
import butterknife.ButterKnife;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.egrobots.grassanalysis.R;
import com.egrobots.grassanalysis.adapters.VideosAdapter;
import com.egrobots.grassanalysis.data.model.VideoItem;
import com.egrobots.grassanalysis.utils.Constants;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SwipeableVideosActivity extends AppCompatActivity implements VideosAdapter.ReplyCallback {
    private static final String TAG = SwipeableVideosActivity.class.getSimpleName();
    @BindView(R.id.viewPagerVideos)
    ViewPager2 viewPagerVideos;
    private List<VideoItem> videoItems;
    private VideosAdapter videosAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_swipeable_videos);
        ButterKnife.bind(this);

        videoItems = new ArrayList<>();
        //get videos from firebase storage
        getVideosFromFirebase();
        videosAdapter = new VideosAdapter(videoItems, this);
        viewPagerVideos.setAdapter(videosAdapter);
    }

    private void getVideosFromFirebase() {
        DatabaseReference videosRef = FirebaseDatabase.getInstance().getReference(Constants.VIDEOS_INFO_NODE);
        videosRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot videoSnapshot : snapshot.getChildren()) {
                    String videoUri = (String) videoSnapshot.child("video_link").getValue();
                    VideoItem videoItem = new VideoItem();
                    videoItem.setVideoUri(videoUri);
                    videoItems.add(videoItem);
                    videosAdapter.notifyDataSetChanged();
                    Log.i(TAG, "onDataChange: " + videoUri);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "onCancelled: ", error.toException() );
            }
        });
    }

    @Override
    public void onSendClicked(String reply) {
        Toast.makeText(this, reply, Toast.LENGTH_SHORT).show();
    }
}