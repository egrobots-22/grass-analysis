package com.egrobots.grassanalysis.datasource.remote;

import android.net.Uri;
import android.util.Log;

import com.egrobots.grassanalysis.data.model.AudioAnswer;
import com.egrobots.grassanalysis.data.model.VideoQuestionItem;
import com.egrobots.grassanalysis.utils.Constants;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.HashMap;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableOnSubscribe;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;

public class FirebaseDataSource {

    private static final String TAG = "FirebaseDataSource";
    private StorageReference storageReference;
    private FirebaseDatabase firebaseDatabase;

    @Inject
    public FirebaseDataSource(StorageReference storageReference, FirebaseDatabase firebaseDatabase) {
        this.storageReference = storageReference;
        this.firebaseDatabase = firebaseDatabase;
    }

    private void saveVideoInfo(FlowableEmitter emitter, String videoUri, String deviceToken, String username) {
        DatabaseReference reference1 = firebaseDatabase.getReference(Constants.QUESTIONS_NODE);
        VideoQuestionItem videoQuestionItem = new VideoQuestionItem();
        videoQuestionItem.setVideoQuestionUri(videoUri);
        videoQuestionItem.setUsername(username);
        reference1.child(deviceToken).push().setValue(videoQuestionItem).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    emitter.onComplete();
                } else {
                    emitter.onError(task.getException());
                }
            }
        });
    }

    public Flowable<Double> uploadVideo(Uri videoUri, String fileType, String deviceToken, String username) {
        return Flowable.create(new FlowableOnSubscribe<Double>() {
            @Override
            public void subscribe(FlowableEmitter<Double> emitter) throws Exception {
                final StorageReference reference = storageReference.child(Constants.STORAGE_REF + System.currentTimeMillis() + "." + fileType);
                UploadTask uploadTask = reference.putFile(videoUri);
                uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                        double progress = (100.0 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                        emitter.onNext(progress);
                    }
                }).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            emitter.onError(task.getException());
                        }

                        // Continue with the task to get the download URL
                        reference.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                saveVideoInfo(emitter, task.getResult().toString(), deviceToken, username);
                            }
                        });
                        return reference.getDownloadUrl();
                    }
                });
            }
        }, BackpressureStrategy.BUFFER);
    }

    public Flowable<UploadTask.TaskSnapshot> uploadVideoAsService(Uri videoUri, String fileType, String deviceToken, String username) {
        return Flowable.create(new FlowableOnSubscribe<UploadTask.TaskSnapshot>() {
            @Override
            public void subscribe(FlowableEmitter<UploadTask.TaskSnapshot> emitter) throws Exception {
                final StorageReference reference = storageReference.child(Constants.STORAGE_REF + System.currentTimeMillis() + "." + fileType);
                UploadTask uploadTask = reference.putFile(videoUri);
                uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                        double progress = (100.0 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                        emitter.onNext(snapshot);
                    }
                }).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            emitter.onError(task.getException());
                        }

                        // Continue with the task to get the download URL
                        reference.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                saveVideoInfo(emitter, task.getResult().toString(), deviceToken, username);
                            }
                        });
                        return reference.getDownloadUrl();
                    }
                });
            }
        }, BackpressureStrategy.BUFFER);
    }

    public Flowable<VideoQuestionItem> getAllVideos() {
        return Flowable.create(new FlowableOnSubscribe<VideoQuestionItem>() {
            @Override
            public void subscribe(FlowableEmitter<VideoQuestionItem> emitter) throws Exception {
                final DatabaseReference videosRef = firebaseDatabase.getReference(Constants.QUESTIONS_NODE);
                videosRef.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        for (DataSnapshot questionSnapshot : snapshot.getChildren()) {
                            VideoQuestionItem videoQuestionItem = questionSnapshot.getValue(VideoQuestionItem.class);
                            videoQuestionItem.setId(questionSnapshot.getKey());
                            emitter.onNext(videoQuestionItem);
                        }
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        }, BackpressureStrategy.BUFFER);
    }

    public Flowable<VideoQuestionItem> getCurrentUserVideos(String deviceToken) {
        return Flowable.create(new FlowableOnSubscribe<VideoQuestionItem>() {
            @Override
            public void subscribe(FlowableEmitter<VideoQuestionItem> emitter) throws Exception {
                final DatabaseReference videosRef = firebaseDatabase
                        .getReference(Constants.QUESTIONS_NODE)
                        .child(deviceToken);
                //check firstly if there is data exists
                videosRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!snapshot.exists() || !snapshot.hasChildren()) {
                            emitter.onNext(new VideoQuestionItem());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                videosRef.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot questionSnapshot, @Nullable String previousChildName) {
                        VideoQuestionItem videoQuestionItem = questionSnapshot.getValue(VideoQuestionItem.class);
                        videoQuestionItem.setId(questionSnapshot.getKey());
                        videoQuestionItem.setDeviceToken(deviceToken);
                        emitter.onNext(videoQuestionItem);
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        }, BackpressureStrategy.BUFFER);
    }

    public Flowable<VideoQuestionItem> getOtherUsersVideos(String deviceToken) {
        return Flowable.create(new FlowableOnSubscribe<VideoQuestionItem>() {
            @Override
            public void subscribe(FlowableEmitter<VideoQuestionItem> emitter) throws Exception {
                final DatabaseReference videosRef = firebaseDatabase.getReference(Constants.QUESTIONS_NODE);
                //check firstly if there is data exists
                videosRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.hasChildren()) {
                            boolean otherDataExists = false;
                            for (DataSnapshot deviceSnapshot : snapshot.getChildren()) {
                                if (!deviceSnapshot.getKey().equals(deviceToken)) {
                                    otherDataExists = true;
                                    break;
                                }
                            }
                            if (!otherDataExists) {
                                emitter.onNext(new VideoQuestionItem());
                            }
                        } else {
                            emitter.onNext(new VideoQuestionItem());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                videosRef.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        if (!deviceToken.equals(snapshot.getKey())) {
                            for (DataSnapshot questionSnapshot : snapshot.getChildren()) {
                                VideoQuestionItem videoQuestionItem = questionSnapshot.getValue(VideoQuestionItem.class);
                                videoQuestionItem.setId(questionSnapshot.getKey());
                                videoQuestionItem.setDeviceToken(snapshot.getKey());
                                emitter.onNext(videoQuestionItem);
                            }
                        }
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        }, BackpressureStrategy.BUFFER);
    }

    public Completable uploadRecordedAudio(File recordFile, VideoQuestionItem questionItem, String username) {
        return Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(CompletableEmitter emitter) throws Exception {
                StorageReference storageReference = FirebaseStorage.getInstance().getReference(Constants.AUDIO_PATH + System.currentTimeMillis() + Constants.AUDIO_FILE_TYPE);
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
                                    .child(questionItem.getDeviceToken())
                                    .child(questionItem.getId())
                                    .child(Constants.ANSWERS_NODE);

//                            String pushId = audioRef.push().getKey();
                            HashMap<String, Object> updates = new HashMap<>();
                            updates.put("audioUri", url);
                            updates.put("recordedUser", username);
                            audioRef.push().updateChildren(updates).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        emitter.onComplete();
                                    } else {
                                        emitter.onError(new Throwable("خطأ اثناء تحميل التسجيل"));

                                    }
                                }
                            });
                        }
                    });
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        emitter.onError(new Throwable("خطأ اثناء تحميل التسجيل"));
                        Log.e("Error", "onFailure: " + e.getMessage());
                    }
                });

            }
        });
    }

    public Flowable<AudioAnswer> getRecordedAudiosForQuestion(VideoQuestionItem questionItem) {
        return Flowable.create(new FlowableOnSubscribe<AudioAnswer>() {
            @Override
            public void subscribe(FlowableEmitter<AudioAnswer> emitter) throws Exception {
                DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                        .getReference(Constants.QUESTIONS_NODE)
                        .child(questionItem.getDeviceToken())
                        .child(questionItem.getId())
                        .child(Constants.ANSWERS_NODE);
                databaseReference.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        if (snapshot.exists()) {
                            AudioAnswer audioAnswer = snapshot.getValue(AudioAnswer.class);
                            audioAnswer.setId(snapshot.getKey());
//                            audioAnswer.setAudioUri((String) snapshot.getValue());
//                            String audioAnswerUri = (String) snapshot.getValue();
                            emitter.onNext(audioAnswer);
                        }
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        }, BackpressureStrategy.BUFFER);
    }
}
