package com.egrobots.grassanalysis.utils;

import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;

import java.io.IOException;

public class AudioPlayer {
    private MediaPlayer mediaPlayer;
    private String audioUri;
    private AudioPlayCallback audioPlayCallback;

    public AudioPlayer(String audioUri, AudioPlayCallback audioPlayCallback) {
        this.audioUri = audioUri;
        this.audioPlayCallback = audioPlayCallback;
    }

    public void playAudio() {
        mediaPlayer = new MediaPlayer();
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setLegacyStreamType(AudioManager.STREAM_MUSIC)
                .build();
        mediaPlayer.setAudioAttributes(audioAttributes);
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                audioPlayCallback.onComplete();
            }
        });
        try {
            mediaPlayer.setDataSource(audioUri);
            mediaPlayer.prepare();
            mediaPlayer.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void pauseAudio() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.release();
        }
    }

    public interface AudioPlayCallback {
        void onComplete();
    }
}
