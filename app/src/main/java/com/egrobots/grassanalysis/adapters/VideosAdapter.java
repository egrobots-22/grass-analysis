package com.egrobots.grassanalysis.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.devlomi.record_view.RecordButton;
import com.devlomi.record_view.RecordView;
import com.egrobots.grassanalysis.R;
import com.egrobots.grassanalysis.data.DatabaseRepository;
import com.egrobots.grassanalysis.data.model.VideoQuestionItem;
import com.egrobots.grassanalysis.managers.ExoPlayerVideoManager;
import com.egrobots.grassanalysis.managers.VideoManager;
import com.egrobots.grassanalysis.utils.Constants;
import com.egrobots.grassanalysis.utils.RecordAudioImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

public class VideosAdapter extends RecyclerView.Adapter<VideosAdapter.VideoViewHolder> {

    private List<VideoQuestionItem> videoQuestionItems = new ArrayList<>();
    private RecordAudioImpl.RecordAudioCallback recordAudioCallback;
    private DatabaseRepository databaseRepository;
    private ExoPlayerVideoManager exoPlayerVideoManager = new ExoPlayerVideoManager();

    public VideosAdapter(DatabaseRepository databaseRepository) {
        this.databaseRepository = databaseRepository;
    }

    public void setRecordAudioCallback(RecordAudioImpl.RecordAudioCallback recordAudioCallback) {
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

    public ExoPlayerVideoManager getExoPlayerVideoManager() {
        return exoPlayerVideoManager;
    }

    class VideoViewHolder extends RecyclerView.ViewHolder implements VideoManager.VideoManagerCallback {

        @BindView(R.id.record_view)
        RecordView recordView;
        @BindView(R.id.record_button)
        RecordButton recordButton;
        @BindView(R.id.progressBar)
        ProgressBar progressBar;
        @BindView(R.id.audioFilesRecyclerView)
        RecyclerView audioFilesRecyclerView;
        @BindView(R.id.videoView)
        PlayerView playerView;

        public VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

        }

        private void setVideoData(String videoUri) {
//            exoPlayerVideoManager.stopPlayer();
            ExoPlayer exoPlayer = new ExoPlayer.Builder(itemView.getContext()).build();
            exoPlayerVideoManager.setPlayerView(playerView);
            exoPlayerVideoManager.stopPlayer();
            exoPlayerVideoManager.setExoPlayer(exoPlayer);
            exoPlayerVideoManager.initializePlayer(videoUri);
        }

        private void setRecordedAudios(VideoQuestionItem questionItem) {
            RecordAudioImpl recordAudioImpl = new RecordAudioImpl(recordView, recordButton, questionItem, recordAudioCallback);
            recordAudioImpl.setupRecordAudio(itemView.getContext().getExternalFilesDir(null), UUID.randomUUID().toString() + Constants.AUDIO_FILE_TYPE);
            setupAudioFilesRecyclerView(questionItem);
        }

        private void setupAudioFilesRecyclerView(VideoQuestionItem questionItem) {
            AudioAdapters audioAdapters = new AudioAdapters(databaseRepository);
            audioFilesRecyclerView.setAdapter(audioAdapters);
            audioFilesRecyclerView.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
            audioAdapters.retrieveAudios(questionItem);
        }

        @Override
        public void onPrepare() {
            progressBar.setVisibility(View.GONE);
        }

        @Override
        public void onError(String msg) {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(itemView.getContext(), msg, Toast.LENGTH_SHORT).show();
        }
    }

}
