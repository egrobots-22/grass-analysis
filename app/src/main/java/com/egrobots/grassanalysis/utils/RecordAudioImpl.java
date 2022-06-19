package com.egrobots.grassanalysis.utils;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;

import com.devlomi.record_view.OnRecordListener;
import com.devlomi.record_view.RecordButton;
import com.devlomi.record_view.RecordView;
import com.egrobots.grassanalysis.R;
import com.egrobots.grassanalysis.data.model.AudioAnswer;
import com.egrobots.grassanalysis.data.model.QuestionItem;
import com.egrobots.grassanalysis.managers.AudioRecorder;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class RecordAudioImpl {
    private static final String FILE_TYPE = ".mp3";

    private Context context;
    private RecordView recordView;
    private RecordButton recordButton;
    private QuestionItem questionItem;
    private File recordFile;
    private AudioRecorder audioRecorder;
    private RecordAudioCallback recordAudioCallback;
    private boolean isRecordStarted;

    public RecordAudioImpl(Context context,
                           RecordView recordView,
                           RecordButton recordButton,
                           QuestionItem questionItem,
                           RecordAudioCallback recordAudioCallback) {
        this.context = context;
        this.recordView = recordView;
        this.recordButton = recordButton;
        this.questionItem = questionItem;
        this.recordAudioCallback = recordAudioCallback;
    }

    public void setupRecordAudio() {
        audioRecorder = new AudioRecorder();
        setupRecordButton();
        setupRecordView();
    }

    private void setupRecordButton() {
        recordButton.setRecordView(recordView);
//        recordButton.setListenForRecord(false);
        recordButton.setListenForRecord(true);
    }

    private void setupRecordView() {
        requestPermissions();
        recordView.setOnRecordListener(new OnRecordListenerInstance());
        recordView.setCancelBounds(8);
        recordView.setSmallMicColor(Color.parseColor("#c2185b"));
        recordView.setLessThanSecondAllowed(false);
        recordView.setSlideToCancelText("Slide To Cancel");
        recordView.setCustomSounds(R.raw.record_start, R.raw.record_finished, 0);
    }

    private void requestPermissions() {
        recordAudioCallback.requestAudioPermission(recordView);
    }

    class OnRecordListenerInstance implements OnRecordListener {

        @Override
        public void onStart() {
            try {
                if (!isRecordStarted) {
                    recordFile = new File(context.getExternalFilesDir(null), UUID.randomUUID().toString() + Constants.AUDIO_FILE_TYPE);
                    Log.i("RecordAudioImpl:74", "recordFile: " + recordFile);
                    audioRecorder.start(context, recordFile.getPath());
                    isRecordStarted = true;
                    recordAudioCallback.onStartRecording();
                } else {
                    stopRecording(true);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onCancel() {
            isRecordStarted = false;
            stopRecording(true);
        }

        @Override
        public void onFinish(long recordTime, boolean limitReached) {
            isRecordStarted = false;
            stopRecording(false);
            AudioAnswer audioAnswer = new AudioAnswer();
            audioAnswer.setAudioUri(recordFile.getPath());
            audioAnswer.setAudioLength(recordTime);
            recordAudioCallback.uploadRecordedAudio(audioAnswer, questionItem);
        }

        @Override
        public void onLessThanSecond() {
            isRecordStarted = false;
            Log.i("RecordAudioImpl:74", "onLessThanSecond: " + recordFile);
            audioRecorder.destroyMediaRecorder();
        }

        private void stopRecording(boolean deleteFile) {
            isRecordStarted = false;
            audioRecorder.stop();
            if (recordFile != null && deleteFile) {
                recordFile.delete();
            }
        }
    }



    public interface RecordAudioCallback {
        void onStartRecording();
        void requestAudioPermission(RecordView view);
        void uploadRecordedAudio(AudioAnswer recordFile, QuestionItem questionItem);
    }
}
