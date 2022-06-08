package com.egrobots.grassanalysis.managers;

import android.util.Log;

import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.common.VideoSize;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.AspectRatioFrameLayout;
import androidx.media3.ui.PlayerView;

import static androidx.media3.common.Player.STATE_ENDED;
import static androidx.media3.common.Player.STATE_READY;

public class ExoPlayerVideoManager {

    private ExoPlayer exoPlayer;
    private PlayerView playerView;
    private boolean playWhenReady = true;
    private int currentItem = 0;
    private long playbackPosition = 0L;
    private boolean isRepeatEnabled = false;
    private VideoManagerCallback videoManagerCallback;

    public void setExoPlayer(ExoPlayer exoPlayer, String videoUri) {
        this.exoPlayer = exoPlayer;
        initializeExoPlayer(videoUri);
    }

    public void setExoPlayerCallback(VideoManagerCallback videoManagerCallback) {
        this.videoManagerCallback = videoManagerCallback;
    }

    public void stopPlayer() {
        if (this.exoPlayer != null) {
            this.exoPlayer.stop();
        }
    }

    public void pausePlayer() {
        if (this.exoPlayer != null) {
            this.exoPlayer.pause();
        }
    }

    public void initializeExoPlayer(String videoUri) {
        MediaItem mediaItem = MediaItem.fromUri(videoUri);
        exoPlayer.setMediaItem(mediaItem);
        exoPlayer.setPlayWhenReady(playWhenReady);
        exoPlayer.seekTo(currentItem, playbackPosition);
        exoPlayer.setRepeatMode(Player.REPEAT_MODE_ONE);
        exoPlayer.prepare();
        exoPlayer.pause();
        exoPlayer.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int playbackState) {
                switch (playbackState) {
                    case STATE_READY:
//                        videoManagerCallback.onPrepare();
                        Log.i("VIDEO READY", "onPlaybackStateChanged: STATE_READY");
                        break;
                    case STATE_ENDED:
                        Log.i("VIDEO ENDED", "onPlaybackStateChanged: STATE_ENDED");
                        break;
                    case Player.STATE_BUFFERING:
                        Log.i("VIDEO BUFFERING", "onPlaybackStateChanged: STATE_BUFFERING");
                        break;
                    case Player.STATE_IDLE:
                        Log.i("VIDEO IDLE", "onPlaybackStateChanged: STATE_IDLE");
                        break;
                }
            }
            @Override
            public void onVideoSizeChanged(VideoSize videoSize) {
                int ratio = videoSize.width / videoSize.height;
                //vertical video
                if (ratio < 1) {
                    playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
                }
                //horizontal video
                else {
                    playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
                }
            }
        });
    }

    public void initializePlayer(PlayerView playerView) {
        playerView.setPlayer(exoPlayer);
        playerView.hideController();
        this.playerView = playerView;
    }

    public PlayerView getPlayerView() {
        return playerView;
    }

    public boolean isPlaying() {
        if (exoPlayer != null) {
            return exoPlayer.isPlaying();
        } else {
            return false;
        }
    }

    public void play() {
        if (exoPlayer != null) {
            exoPlayer.prepare();
            exoPlayer.play();
        }
    }

    public void resumePlaying() {
        if (exoPlayer != null && !isPlaying()) {
            exoPlayer.setPlayWhenReady(true);
            exoPlayer.getPlaybackState();
        }
    }

    public void releasePlayer() {
        if (exoPlayer != null) {
            playbackPosition = exoPlayer.getCurrentPosition();
            currentItem = exoPlayer.getCurrentMediaItemIndex();
            playWhenReady = exoPlayer.getPlayWhenReady();
            exoPlayer.release();
        }
        exoPlayer = null;
    }

    public interface VideoManagerCallback {
        void onPrepare();

        void onError(String msg);

        void onEnd();
    }
}
