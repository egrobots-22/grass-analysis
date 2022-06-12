package com.egrobots.grassanalysis.managers;

import android.content.Context;
import android.media.MediaRecorder;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;

public class AudioRecorder {
    private MediaRecorder mediaRecorder;
    private Context context;

    private void initMediaRecorder() {
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
    }


    public void start(Context context, String filePath) throws IOException {
        if (mediaRecorder == null) {
            initMediaRecorder();
        }

        try {
            Log.i("AudioRecorder:28", "filePath: " + filePath);
            mediaRecorder.setOutputFile(filePath);
            mediaRecorder.prepare();
            mediaRecorder.start();
        } catch (IllegalStateException ex) {
            Toast.makeText(context, "Can't start recording", Toast.LENGTH_SHORT).show();
            ex.printStackTrace();
        }
    }

    public void stop() {
        try {
            mediaRecorder.stop();
            destroyMediaRecorder();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void destroyMediaRecorder() {
        if (mediaRecorder != null) {
            mediaRecorder.release();
            mediaRecorder = null;
        }
    }
}