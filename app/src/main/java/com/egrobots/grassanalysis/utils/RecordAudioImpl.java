package com.egrobots.grassanalysis.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;

import com.devlomi.record_view.OnRecordListener;
import com.devlomi.record_view.RecordButton;
import com.devlomi.record_view.RecordView;
import com.egrobots.grassanalysis.R;
import com.egrobots.grassanalysis.adapters.VideosAdapter;
import com.egrobots.grassanalysis.data.model.VideoQuestionItem;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

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

    @SuppressLint("DefaultLocale")
    private String getHumanTimeText(long milliseconds) {
        return String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(milliseconds),
                TimeUnit.MILLISECONDS.toSeconds(milliseconds) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliseconds)));
    }

//    private void sendRecordedFile() {
//        StorageReference storageReference = FirebaseStorage.getInstance().getReference("Answers/Recording/" + System.currentTimeMillis() + FILE_TYPE);
//        StorageMetadata metadata = new StorageMetadata.Builder()
//                .setContentType("audio/mpeg")
//                .build();
//        Uri audioFile = Uri.fromFile(recordFile);
//        storageReference.putFile(audioFile, metadata).addOnSuccessListener(success -> {
//            Task<Uri> audioUrl = success.getStorage().getDownloadUrl();
//            audioUrl.addOnCompleteListener(path -> {
//                if (path.isSuccessful()) {
//                    String url = path.getResult().toString();
//                    DatabaseReference audioRef = FirebaseDatabase.getInstance()
//                            .getReference(Constants.QUESTIONS_NODE)
//                            .child(getDeviceToken())
//                            .child(questionItem.getId())
//                            .child(Constants.ANSWERS_NODE);
//                    String pushId = audioRef.push().getKey();
//                    HashMap<String, Object> updates = new HashMap<>();
//                    updates.put(pushId, url);
//                    audioRef.updateChildren(updates);
//                }
//            });
//        }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//                Toast.makeText(activity, "حدث خطأ اثناء التحميل", Toast.LENGTH_SHORT).show();
//                Log.e("Error", "onFailure: " + e.getMessage());
//            }
//        });
//    }
//
//    private String getDeviceToken() {
//        SharedPreferences sharedPreferences = activity.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
//        return sharedPreferences.getString(Constants.DEVICE_TOKEN, null);
//    }

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
//            sendRecordedFile();
//            String time = getHumanTimeText(recordTime);
//            String path = recordFile.getPath();
//            Log.d("Recorded File Path", path);
//            Toast.makeText(activity, "تم تسجيل اجابتك بنجاح..", Toast.LENGTH_SHORT).show();
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
