package com.egrobots.grassanalysis.managers;

import androidx.media3.common.MediaItem;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

public class ExoPlayerVideoManager {

    private ExoPlayer exoPlayer;
    private PlayerView playerView;
    private boolean playWhenReady = true;
    private int currentItem = 0;
    private long playbackPosition = 0L;

    public void setPlayerView(PlayerView playerView) {
        this.playerView = playerView;
    }

    public void setExoPlayer(ExoPlayer exoPlayer) {
        this.exoPlayer = exoPlayer;
    }

    public ExoPlayer getExoPlayer() {
        return exoPlayer;
    }

    public void stopPlayer() {
        if (this.exoPlayer != null) {
            this.exoPlayer.stop();
        }
    }

    public void initializePlayer(String videoUri) {
        playerView.setPlayer(exoPlayer);
        MediaItem mediaItem = MediaItem.fromUri(videoUri);
        exoPlayer.setMediaItem(mediaItem);
        exoPlayer.setPlayWhenReady(playWhenReady);
        exoPlayer.seekTo(currentItem, playbackPosition);
        exoPlayer.prepare();
        exoPlayer.play();
    }

    public boolean isPlaying() {
        if (exoPlayer != null) {
            return exoPlayer.isPlaying();
        } else {
            return false;
        }
    }

    public void resumePlaying() {
        if (exoPlayer != null && !isPlaying()) {
            exoPlayer.play();
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
}
