package com.egrobots.grassanalysis.managers;

import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaDataSource;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;

import java.io.IOException;
import java.util.HashMap;

public class AudioPlayer {
    private MediaPlayer mediaPlayer;
    private String audioUri;
    private AudioPlayCallback audioPlayCallback;

    public void setAudio(String audioUri, AudioPlayCallback audioPlayCallback) {
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

    public String getAudioDuration() {
        try {
            MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
            mediaMetadataRetriever.setDataSource(audioUri, new HashMap<String, String>());
            String durationStr = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            mediaMetadataRetriever.release();
            return formatMilliSeconds(Long.parseLong(durationStr));
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        return "0";
    }

    private String formatMilliSeconds(long milliseconds) {
        String finalTimerString = "";
        String secondsString = "";

        // Convert total duration into time
        int hours = (int) (milliseconds / (1000 * 60 * 60));
        int minutes = (int) (milliseconds % (1000 * 60 * 60)) / (1000 * 60);
        int seconds = (int) ((milliseconds % (1000 * 60 * 60)) % (1000 * 60) / 1000);

        // Add hours if there
        if (hours > 0) {
            finalTimerString = hours + ":";
        }

        // Prepending 0 to seconds if it is one digit
        if (seconds < 10) {
            secondsString = "0" + seconds;
        } else {
            secondsString = "" + seconds;
        }

        finalTimerString = finalTimerString + minutes + ":" + secondsString;
        // return timer string
        return finalTimerString;
    }

    public interface AudioPlayCallback {
        void onComplete();
    }
}
