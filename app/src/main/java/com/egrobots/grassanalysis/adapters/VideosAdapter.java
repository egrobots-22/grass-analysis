package com.egrobots.grassanalysis.adapters;

import android.app.Activity;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.VideoView;

import com.devlomi.record_view.RecordButton;
import com.devlomi.record_view.RecordView;
import com.egrobots.grassanalysis.R;
import com.egrobots.grassanalysis.data.model.VideoQuestionItem;
import com.egrobots.grassanalysis.utils.Constants;
import com.egrobots.grassanalysis.utils.RecordAudioImplementation;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

public class VideosAdapter extends RecyclerView.Adapter<VideosAdapter.VideoViewHolder> {
    private List<VideoQuestionItem> videoQuestionItems;
    private Activity activity;

    public VideosAdapter(Activity activity, List<VideoQuestionItem> videoQuestionItems) {
        this.activity = activity;
        this.videoQuestionItems = videoQuestionItems;
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.video_screen_layout, parent, false);
        return new VideoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        VideoQuestionItem questionItem = videoQuestionItems.get(position);
        holder.setVideoData(questionItem.getVideoQuestionUri());
        RecordAudioImplementation recordAudioImplementation =
                new RecordAudioImplementation(holder.recordView, holder.recordButton, questionItem);
        recordAudioImplementation.attachActivity(activity);
        recordAudioImplementation.setupRecordAudio();

        //get audio files for current video question
        holder.setupAudioFilesRecyclerView(questionItem);
    }

    @Override
    public int getItemCount() {
        return videoQuestionItems.size();
    }

    class VideoViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.videoView)
        VideoView videoView;
        @BindView(R.id.record_view)
        RecordView recordView;
        @BindView(R.id.record_button)
        RecordButton recordButton;
        @BindView(R.id.progressBar)
        ProgressBar progressBar;
        @BindView(R.id.audioFilesRecyclerView)
        RecyclerView audioFilesRecyclerView;

        public VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        private void setupAudioFilesRecyclerView(VideoQuestionItem questionItem) {
            List<String> audioUris = new ArrayList<>();
//            audioUris.add("https://actions.google.com/sounds/v1/alarms/alarm_clock.ogg");
//            audioUris.add("https://firebasestorage.googleapis.com/v0/b/grass-analysis.appspot.com/o/Answers%2FRecording%2F1654123501556.mp3?alt=media&token=6f78df64-488d-4451-8617-a7a1c4801b32");
//            audioUris.add("https://firebasestorage.googleapis.com/v0/b/grass-analysis.appspot.com/o/Answers%2FRecording%2F1654122490262.mp3?alt=media&token=1699a6bd-1e41-4c27-aa43-05888e3f1a10");
            AudioAdapters audioAdapters = new AudioAdapters(audioUris);
            audioFilesRecyclerView.setAdapter(audioAdapters);
            audioFilesRecyclerView.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
//            VideoQuestionItem questionItem = videoQuestionItems.get(getAdapterPosition());
            DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                    .getReference(Constants.QUESTIONS_NODE)
                    .child(questionItem.getDeviceToken())
                    .child(questionItem.getId())
                    .child(Constants.ANSWERS_NODE);
            databaseReference.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    if (snapshot.exists()) {
//                        for (DataSnapshot answerSnapshot : snapshot.getChildren()) {
                        String audioAnswerUri = (String) snapshot.getValue();
                        audioUris.add(audioAnswerUri);
                        audioAdapters.notifyDataSetChanged();
//                        }
                    }
                    if (audioUris.isEmpty()) {
                        audioFilesRecyclerView.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

        void setVideoData(String videoUri) {
            videoView.setVideoPath(videoUri);
            videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    progressBar.setVisibility(View.GONE);
                    mp.setVolume(0f, 0f);
                    mp.start();

                    float videoRatio = mp.getVideoWidth() / (float) mp.getVideoHeight();
                    float screenRatio = videoView.getWidth() / (float) videoView.getHeight();
                    float scale = videoRatio / screenRatio;
                    if (scale >= 1f) {
                        videoView.setScaleX(scale);
                    } else {
                        videoView.setScaleY(1f / scale);
                    }
                }
            });
            videoView.setOnCompletionListener(mp -> mp.start());
            videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    Log.e("Error", "onError: ");
                    return false;
                }
            });
        }

    }

    public interface AttachActivityListener {
        void attachActivity(Activity activity);
    }

}
