package com.egrobots.grassanalysis.adapters;

import android.media.MediaPlayer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.VideoView;

import com.egrobots.grassanalysis.R;
import com.egrobots.grassanalysis.data.model.VideoItem;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

public class VideosAdapter extends RecyclerView.Adapter<VideosAdapter.VideoViewHolder> {
    private List<VideoItem> videoItems;
    private ReplyCallback replyCallback;

    public VideosAdapter(List<VideoItem> videoItems, ReplyCallback replyCallback) {
        this.videoItems = videoItems;
        this.replyCallback = replyCallback;
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.video_screen_layout, parent, false);
        return new VideoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        holder.setVideoData(videoItems.get(position));
        holder.sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String reply = holder.getReply();
                if (!reply.isEmpty()) {
                    replyCallback.onSendClicked(reply);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return videoItems.size();
    }

    static class VideoViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.videoView)
        VideoView videoView;
        @BindView(R.id.replyEditText)
        EditText replyEditText;
        @BindView(R.id.sendButton)
        ImageButton sendButton;
        @BindView(R.id.progressBar)
        ProgressBar progressBar;

        public VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void setVideoData(VideoItem videoItem) {
            videoView.setVideoPath(videoItem.getVideoUri());
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
                    return false;
                }
            });
        }

        public String getReply() {
            return replyEditText.getText().toString();
        }
    }

    public interface ReplyCallback {
        void onSendClicked(String reply);
    }
}
