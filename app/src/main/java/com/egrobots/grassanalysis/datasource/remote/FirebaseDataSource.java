package com.egrobots.grassanalysis.datasource.remote;

import android.net.Uri;

import com.egrobots.grassanalysis.data.model.VideoQuestionItem;
import com.egrobots.grassanalysis.utils.Constants;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.reactivex.BackpressureStrategy;
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

    private void saveVideoInfo(FlowableEmitter<Double> emitter, String videoUri, String deviceToken) {
        DatabaseReference reference1 = firebaseDatabase.getReference(Constants.QUESTIONS_NODE);
        VideoQuestionItem videoQuestionItem = new VideoQuestionItem();
        videoQuestionItem.setVideoQuestionUri(videoUri);
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

    public Flowable<Double> uploadVideo(Uri videoUri, String fileType, String deviceToken) {
        return Flowable.create(new FlowableOnSubscribe<Double>() {
            @Override
            public void subscribe(FlowableEmitter<Double> emitter) throws Exception {
                final StorageReference reference = storageReference.child(Constants.STORAGE_REF + "/" + System.currentTimeMillis() + "." + fileType);
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
                                saveVideoInfo(emitter, task.getResult().toString(), deviceToken);
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
                final DatabaseReference videosRef = firebaseDatabase.getReference(Constants.QUESTIONS_NODE);
                videosRef.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        if (deviceToken.equals(snapshot.getKey())) {
                            for (DataSnapshot questionSnapshot : snapshot.getChildren()) {
                                VideoQuestionItem videoQuestionItem = questionSnapshot.getValue(VideoQuestionItem.class);
                                videoQuestionItem.setId(questionSnapshot.getKey());
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

    public Flowable<VideoQuestionItem> getOtherUsersVideos(String deviceToken) {
        return Flowable.create(new FlowableOnSubscribe<VideoQuestionItem>() {
            @Override
            public void subscribe(FlowableEmitter<VideoQuestionItem> emitter) throws Exception {
                final DatabaseReference videosRef = firebaseDatabase.getReference(Constants.QUESTIONS_NODE);
                videosRef.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        if (!deviceToken.equals(snapshot.getKey())) {
                            for (DataSnapshot questionSnapshot : snapshot.getChildren()) {
                                VideoQuestionItem videoQuestionItem = questionSnapshot.getValue(VideoQuestionItem.class);
                                videoQuestionItem.setId(questionSnapshot.getKey());
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
}
