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
import com.egrobots.grassanalysis.data.model.VideoItem;
import com.egrobots.grassanalysis.data.model.VideoQuestionItem;
import com.egrobots.grassanalysis.utils.RecordAudioImplementation;

import java.util.List;

import androidx.annotation.NonNull;
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
    }

    @Override
    public int getItemCount() {
        return videoQuestionItems.size();
    }

    class VideoViewHolder extends RecyclerView.ViewHolder implements AttachActivityListener{
        @BindView(R.id.videoView)
        VideoView videoView;
        @BindView(R.id.record_view)
        RecordView recordView;
        @BindView(R.id.record_button)
        RecordButton recordButton;
        @BindView(R.id.progressBar)
        ProgressBar progressBar;

        public VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void setVideoData(String videoUri) {
            videoView.setVideoPath(videoUri);
            videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    progressBar.setVisibility(View.GONE);
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
                    Log.e("Error", "onError: "  );;
                    return false;
                }
            });
        }

        @Override
        public void attachActivity(Activity activity) {

        }
    }

    public interface AttachActivityListener {
        void attachActivity(Activity activity);
    }

}
