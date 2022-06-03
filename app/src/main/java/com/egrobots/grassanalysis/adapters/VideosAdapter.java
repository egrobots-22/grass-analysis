package com.egrobots.grassanalysis.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.VideoView;

import com.devlomi.record_view.RecordButton;
import com.devlomi.record_view.RecordView;
import com.egrobots.grassanalysis.R;
import com.egrobots.grassanalysis.data.model.VideoQuestionItem;
import com.egrobots.grassanalysis.managers.VideoManager;
import com.egrobots.grassanalysis.utils.Constants;
import com.egrobots.grassanalysis.utils.RecordAudioImpl;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

public class VideosAdapter extends RecyclerView.Adapter<VideosAdapter.VideoViewHolder> {
    private static final String FILE_TYPE = ".mp3";

    private List<VideoQuestionItem> videoQuestionItems = new ArrayList<>();
    private RecordAudioImpl.RecordAudioCallback recordAudioCallback;

    public VideosAdapter(RecordAudioImpl.RecordAudioCallback recordAudioCallback) {
        this.recordAudioCallback = recordAudioCallback;
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
        //get audio files for current video question
        holder.setRecordedAudios(questionItem);
    }

    @Override
    public int getItemCount() {
        return videoQuestionItems.size();
    }

    public void addNewVideo(VideoQuestionItem videoQuestionItem) {
        videoQuestionItems.add(videoQuestionItem);
        notifyDataSetChanged();
    }


    class VideoViewHolder extends RecyclerView.ViewHolder implements VideoManager.VideoManagerCallback {
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

        void setVideoData(String videoUri) {
            VideoManager videoManager = new VideoManager(videoView, this);
            videoManager.setVideoData(videoUri);
        }

        public void setRecordedAudios(VideoQuestionItem questionItem) {
            RecordAudioImpl recordAudioImpl = new RecordAudioImpl(recordView, recordButton, questionItem, recordAudioCallback);
            recordAudioImpl.setupRecordAudio(itemView.getContext().getExternalFilesDir(null), UUID.randomUUID().toString() + FILE_TYPE);
            setupAudioFilesRecyclerView(questionItem);
        }

        private void setupAudioFilesRecyclerView(VideoQuestionItem questionItem) {
            AudioAdapters audioAdapters = new AudioAdapters();
            audioFilesRecyclerView.setAdapter(audioAdapters);
            audioFilesRecyclerView.setLayoutManager(new LinearLayoutManager(itemView.getContext()));

            DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                    .getReference(Constants.QUESTIONS_NODE)
                    .child(questionItem.getDeviceToken())
                    .child(questionItem.getId())
                    .child(Constants.ANSWERS_NODE);
            databaseReference.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    if (snapshot.exists()) {
                        String audioAnswerUri = (String) snapshot.getValue();
                        audioAdapters.addNewAudio(audioAnswerUri);
                    } else {
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

        @Override
        public void onPrepare() {
            progressBar.setVisibility(View.GONE);
        }

        @Override
        public void onError(String msg) {
            Toast.makeText(itemView.getContext(), msg, Toast.LENGTH_SHORT).show();
        }
    }

}
