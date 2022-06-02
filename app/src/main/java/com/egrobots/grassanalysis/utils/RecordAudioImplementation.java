package com.egrobots.grassanalysis.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import com.devlomi.record_view.OnRecordListener;
import com.devlomi.record_view.RecordButton;
import com.devlomi.record_view.RecordView;
import com.egrobots.grassanalysis.R;
import com.egrobots.grassanalysis.adapters.VideosAdapter;
import com.egrobots.grassanalysis.data.model.VideoQuestionItem;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import static androidx.core.content.PermissionChecker.PERMISSION_GRANTED;

public class RecordAudioImplementation implements VideosAdapter.AttachActivityListener {
    private static final String FILE_TYPE = ".mp3";

    private Activity activity;
    private RecordView recordView;
    private RecordButton recordButton;
    private VideoQuestionItem questionItem;
    private File recordFile;
    private AudioRecorder audioRecorder;

    public RecordAudioImplementation(RecordView recordView, RecordButton recordButton, VideoQuestionItem questionItem) {
        this.recordView = recordView;
        this.recordButton = recordButton;
        this.questionItem = questionItem;
    }

    public void setupRecordAudio() {
        audioRecorder = new AudioRecorder();
        recordFile = new File(activity.getExternalFilesDir(null), UUID.randomUUID().toString() + FILE_TYPE);
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
        recordView.setRecordPermissionHandler(() -> {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                return true;
            }
            boolean recordPermissionAvailable = ContextCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO) == PERMISSION_GRANTED;
            if (recordPermissionAvailable) {
                return true;
            }
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.RECORD_AUDIO}, 0);
            return false;
        });
    }

    @Override
    public void attachActivity(Activity activity) {
        this.activity = activity;
    }

    @SuppressLint("DefaultLocale")
    private String getHumanTimeText(long milliseconds) {
        return String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(milliseconds),
                TimeUnit.MILLISECONDS.toSeconds(milliseconds) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliseconds)));
    }

    private void sendRecordedFile() {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference("Answers/Recording/" + System.currentTimeMillis() + FILE_TYPE);
        StorageMetadata metadata = new StorageMetadata.Builder()
                .setContentType("audio/mpeg")
                .build();
        Uri audioFile = Uri.fromFile(recordFile);
        storageReference.putFile(audioFile, metadata).addOnSuccessListener(success -> {
            Task<Uri> audioUrl = success.getStorage().getDownloadUrl();
            audioUrl.addOnCompleteListener(path -> {
                if (path.isSuccessful()) {
                    String url = path.getResult().toString();
                    DatabaseReference audioRef = FirebaseDatabase.getInstance()
                            .getReference(Constants.QUESTIONS_NODE)
                            .child(getDeviceToken())
                            .child(questionItem.getId())
                            .child(Constants.ANSWERS_NODE);
                    String pushId = audioRef.push().getKey();
                    HashMap<String, Object> updates = new HashMap<>();
                    updates.put(pushId, url);
                    audioRef.updateChildren(updates);
                }
            });
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(activity, "حدث خطأ اثناء التحميل", Toast.LENGTH_SHORT).show();
                Log.e("Error", "onFailure: " + e.getMessage());
            }
        });
    }

    private String getDeviceToken() {
        SharedPreferences sharedPreferences = activity.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(Constants.DEVICE_TOKEN, null);
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
            sendRecordedFile();
            String time = getHumanTimeText(recordTime);
            String path = recordFile.getPath();
            Log.d("Recorded File Path", path);
            Toast.makeText(activity, "تم تسجيل اجابتك بنجاح..", Toast.LENGTH_SHORT).show();
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
}
