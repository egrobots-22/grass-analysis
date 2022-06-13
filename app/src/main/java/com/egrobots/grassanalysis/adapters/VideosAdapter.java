package com.egrobots.grassanalysis.adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.devlomi.record_view.RecordButton;
import com.devlomi.record_view.RecordView;
import com.egrobots.grassanalysis.R;
import com.egrobots.grassanalysis.data.DatabaseRepository;
import com.egrobots.grassanalysis.data.model.QuestionItem;
import com.egrobots.grassanalysis.managers.ExoPlayerVideoManager;
import com.egrobots.grassanalysis.utils.RecordAudioImpl;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.media3.ui.PlayerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

public class VideosAdapter extends RecyclerView.Adapter<VideosAdapter.VideoViewHolder> {

    public List<QuestionItem> questionItems = new ArrayList<>();
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
        QuestionItem questionItem = questionItems.get(position);
        if (questionItem.getType() == null || questionItem.getType().contains("mp4")) {
            holder.playerView.setVisibility(View.VISIBLE);
            holder.imageView.setVisibility(View.GONE);
            managers.get(position).initializePlayer(holder.playerView);
        } else {
            holder.playerView.setVisibility(View.GONE);
            holder.imageView.setVisibility(View.VISIBLE);
            Glide.with(holder.itemView.getContext())
                    .load(questionItem.getVideoQuestionUri())
                    .into(holder.imageView);
        }

        //get audio files for current video question
        holder.setRecordAudioView(questionItem);
        holder.setupAudioFilesRecyclerView(questionItem);
    }

    @Override
    public int getItemCount() {
        return questionItems.size();
    }

    public void addNewVideo(Context context, QuestionItem questionItem) {
        questionItems.add(0, questionItem);
        notifyItemInserted(0);
        if (questionItem.getType()== null || questionItem.getType().contains("mp4")) {
            ExoPlayerVideoManager exoPlayerVideoManager = new ExoPlayerVideoManager();
//        ExoPlayer exoPlayer = new ExoPlayer.Builder(context).build();
            exoPlayerVideoManager.initializeExoPlayer(context, questionItem.getVideoQuestionUri());
            managers.add(0, exoPlayerVideoManager);
        } else {
            managers.add(null);
        }
    }

    public void addAll(Context context, List<QuestionItem> itemsList) {
        questionItems.addAll(itemsList);
        for (QuestionItem item : itemsList) {
            if (item.getType()== null || item.getType().contains("mp4")) {
                ExoPlayerVideoManager exoPlayerVideoManager = new ExoPlayerVideoManager();
//            ExoPlayer exoPlayer = new ExoPlayer.Builder(context).build();
                exoPlayerVideoManager.initializeExoPlayer(context, item.getVideoQuestionUri());
                managers.add(exoPlayerVideoManager);
            } else {
                managers.add(null);
            }
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
        @BindView(R.id.imageView)
        ImageView imageView;

        public VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        private void setRecordAudioView(QuestionItem questionItem) {
            RecordAudioImpl recordAudioImpl = new RecordAudioImpl(itemView.getContext(), recordView, recordButton, questionItem, recordAudioCallback);
            recordAudioImpl.setupRecordAudio();
        }

        private void setupAudioFilesRecyclerView(QuestionItem questionItem) {
            AudioAdapters audioAdapters = new AudioAdapters(databaseRepository);
            audioFilesRecyclerView.setAdapter(audioAdapters);
            audioFilesRecyclerView.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
            audioAdapters.retrieveAudios(questionItem);
        }
    }

}
