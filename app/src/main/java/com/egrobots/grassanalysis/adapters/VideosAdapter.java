package com.egrobots.grassanalysis.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.devlomi.record_view.RecordButton;
import com.devlomi.record_view.RecordView;
import com.egrobots.grassanalysis.R;
import com.egrobots.grassanalysis.data.DatabaseRepository;
import com.egrobots.grassanalysis.data.model.VideoQuestionItem;
import com.egrobots.grassanalysis.managers.ExoPlayerVideoManager;
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
    private List<ExoPlayerVideoManager> managers = new ArrayList<>();

    public VideosAdapter(DatabaseRepository databaseRepository) {
        this.databaseRepository = databaseRepository;
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
        managers.get(position).initializePlayer(holder.playerView);
        //get audio files for current video question
        holder.setRecordAudioView(questionItem);
        holder.setupAudioFilesRecyclerView(questionItem);
    }

    @Override
    public int getItemCount() {
        return videoQuestionItems.size();
    }

    public void addNewVideo(Context context, VideoQuestionItem videoQuestionItem) {
        videoQuestionItems.add(0, videoQuestionItem);
        notifyItemInserted(0);
        ExoPlayerVideoManager exoPlayerVideoManager = new ExoPlayerVideoManager();
//        ExoPlayer exoPlayer = new ExoPlayer.Builder(context).build();
        exoPlayerVideoManager.initializeExoPlayer(context, videoQuestionItem.getVideoQuestionUri());
        managers.add(0, exoPlayerVideoManager);
    }

    public void addAll(Context context, List<VideoQuestionItem> itemsList) {
        videoQuestionItems.addAll(itemsList);
        for (VideoQuestionItem item : itemsList) {
            ExoPlayerVideoManager exoPlayerVideoManager = new ExoPlayerVideoManager();
//            ExoPlayer exoPlayer = new ExoPlayer.Builder(context).build();
            exoPlayerVideoManager.initializeExoPlayer(context, item.getVideoQuestionUri());
            managers.add(exoPlayerVideoManager);
        }
        notifyDataSetChanged();
    }

    public ExoPlayerVideoManager getCurrentExoPlayerManager(int position) {
        return managers.get(position);
    }

    public List<ExoPlayerVideoManager> getCurrentExoPlayerManagerList() {
        return managers;
    }

    public void setRecordAudioCallback(RecordAudioImpl.RecordAudioCallback recordAudioCallback) {
        this.recordAudioCallback = recordAudioCallback;
    }

    class VideoViewHolder extends RecyclerView.ViewHolder {

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

        private void setRecordAudioView(VideoQuestionItem questionItem) {
            RecordAudioImpl recordAudioImpl = new RecordAudioImpl(recordView, recordButton, questionItem, recordAudioCallback);
            recordAudioImpl.setupRecordAudio(itemView.getContext().getExternalFilesDir(null), UUID.randomUUID().toString() + Constants.AUDIO_FILE_TYPE);
        }

        private void setupAudioFilesRecyclerView(VideoQuestionItem questionItem) {
            AudioAdapters audioAdapters = new AudioAdapters(databaseRepository);
            audioFilesRecyclerView.setAdapter(audioAdapters);
            audioFilesRecyclerView.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
            audioAdapters.retrieveAudios(questionItem);
        }
    }

}
