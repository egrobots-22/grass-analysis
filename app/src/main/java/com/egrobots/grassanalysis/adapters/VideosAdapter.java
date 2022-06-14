package com.egrobots.grassanalysis.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
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
import com.egrobots.grassanalysis.data.model.AudioAnswer;
import com.egrobots.grassanalysis.data.model.QuestionItem;
import com.egrobots.grassanalysis.managers.AudioPlayer;
import com.egrobots.grassanalysis.managers.ExoPlayerVideoManager;
import com.egrobots.grassanalysis.utils.RecordAudioImpl;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.annotation.NonNull;
import androidx.media3.ui.PlayerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

public class VideosAdapter extends RecyclerView.Adapter<VideosAdapter.VideoViewHolder> {

    public List<QuestionItem> questionItems = new ArrayList<>();
    private RecordAudioImpl.RecordAudioCallback recordAudioCallback;
    private AudioPlayer.AudioPlayCallback audioPlayCallback;
    private DatabaseRepository databaseRepository;
    private List<ExoPlayerVideoManager> managers = new ArrayList<>();
    private HashMap<String, AudioAdapters> audioAnswersForQuestionMap = new HashMap<>();
    ExecutorService executorService = Executors.newSingleThreadExecutor();

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
        } else {
            holder.playerView.setVisibility(View.GONE);
            holder.imageView.setVisibility(View.VISIBLE);
            Glide.with(holder.itemView.getContext())
                    .load(questionItem.getQuestionMediaUri())
                    .into(holder.imageView);
            /*
            ** another option: but low performance
             executorService.submit(() -> {
                    try {
                        InputStream url = new java.net.URL(item.getQuestionMediaUri()).openStream();
                        Bitmap bitmap = BitmapFactory.decodeStream(url);
                        Drawable drawable = new BitmapDrawable(context.getResources(), bitmap);
                        exoPlayerAudioManager.setCapturedImageToPlayer(drawable);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

             */
        }
        managers.get(position).initializePlayer(holder.playerView);

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
            exoPlayerVideoManager.initializeExoPlayer(context, questionItem.getQuestionMediaUri());
            managers.add(0, exoPlayerVideoManager);
        } else {
            ExoPlayerVideoManager exoPlayerAudioManager = new ExoPlayerVideoManager();
            exoPlayerAudioManager.initializeAudioExoPlayer(context, questionItem.getQuestionAudioUri(), false);
            managers.add(0, exoPlayerAudioManager);
        }
    }

    public void addAll(Context context, List<QuestionItem> itemsList) {
        questionItems.addAll(itemsList);
        for (QuestionItem item : itemsList) {
            if (item.getType()== null || item.getType().contains("mp4")) {
                ExoPlayerVideoManager exoPlayerVideoManager = new ExoPlayerVideoManager();
                exoPlayerVideoManager.initializeExoPlayer(context, item.getQuestionMediaUri());
                managers.add(exoPlayerVideoManager);
            } else {
                ExoPlayerVideoManager exoPlayerAudioManager = new ExoPlayerVideoManager();
                exoPlayerAudioManager.initializeAudioExoPlayer(context, item.getQuestionAudioUri(), false);
                managers.add(exoPlayerAudioManager);
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

    public void setAudioPlayCallback(AudioPlayer.AudioPlayCallback audioPlayCallback) {
        this.audioPlayCallback = audioPlayCallback;
    }

    public void loadAddingNewAudioAnswer(String id) {
        audioAnswersForQuestionMap.get(id).addNewAudio(null);
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
        AudioAdapters audioAdapters;

        public VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        private void setRecordAudioView(QuestionItem questionItem) {
            RecordAudioImpl recordAudioImpl = new RecordAudioImpl(itemView.getContext(), recordView, recordButton, questionItem, recordAudioCallback);
            recordAudioImpl.setupRecordAudio();
        }

        private void setupAudioFilesRecyclerView(QuestionItem questionItem) {
            audioAdapters = new AudioAdapters(databaseRepository, audioPlayCallback);
            audioFilesRecyclerView.setAdapter(audioAdapters);
            audioFilesRecyclerView.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
            audioAdapters.retrieveAudios(questionItem);
            audioAnswersForQuestionMap.put(questionItem.getId(), audioAdapters);
        }
    }

}
