package com.egrobots.grassanalysis.utils;

import android.graphics.Color;

import com.devlomi.record_view.OnRecordListener;
import com.devlomi.record_view.RecordButton;
import com.devlomi.record_view.RecordView;
import com.egrobots.grassanalysis.R;
import com.egrobots.grassanalysis.data.model.VideoQuestionItem;
import com.egrobots.grassanalysis.managers.AudioRecorder;

import java.io.File;
import java.io.IOException;

public class RecordAudioImpl {
    private static final String FILE_TYPE = ".mp3";

    private RecordView recordView;
    private RecordButton recordButton;
    private VideoQuestionItem questionItem;
    private File recordFile;
    private AudioRecorder audioRecorder;
    private RecordAudioCallback recordAudioCallback;

    public RecordAudioImpl(RecordView recordView,
                           RecordButton recordButton,
                           VideoQuestionItem questionItem,
                           RecordAudioCallback recordAudioCallback) {
        this.recordView = recordView;
        this.recordButton = recordButton;
        this.questionItem = questionItem;
        this.recordAudioCallback = recordAudioCallback;
    }

    public void setupRecordAudio(File path, String fileName) {
        audioRecorder = new AudioRecorder();
        recordFile = new File(path, fileName);
        setupRecordButton();
        setupRecordView();
    }

    private void setupRecordButton() {
        recordButton.setRecordView(recordView);
        recordButton.setListenForRecord(false);
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
                audioRecorder.start(recordFile.getPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onCancel() {
            stopRecording(true);
        }

        @Override
        public void onFinish(long recordTime, boolean limitReached) {
            stopRecording(false);
            recordAudioCallback.uploadRecordedAudio(recordFile, questionItem);
        }

        @Override
        public void onLessThanSecond() {

        }

        private void stopRecording(boolean deleteFile) {
            audioRecorder.stop();
            if (recordFile != null && deleteFile) {
                recordFile.delete();
            }
        }
    }

    public interface RecordAudioCallback {
        void requestAudioPermission(RecordView view);
        void uploadRecordedAudio(File recordFile, VideoQuestionItem questionItem);
    }
}
