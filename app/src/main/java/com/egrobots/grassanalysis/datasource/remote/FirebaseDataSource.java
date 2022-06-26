package com.egrobots.grassanalysis.datasource.remote;

import android.net.Uri;
import android.util.Log;

import com.egrobots.grassanalysis.data.model.AudioAnswer;
import com.egrobots.grassanalysis.data.model.QuestionItem;
import com.egrobots.grassanalysis.data.model.Reactions;
import com.egrobots.grassanalysis.utils.Constants;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.StreamSupport;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.Single;

public class FirebaseDataSource {

    private static final String TAG = "FirebaseDataSource";
    private static final int LIMIT_ITEM_COUNT = 3;

    private StorageReference storageReference;
    private FirebaseDatabase firebaseDatabase;
    private int sentItemsCount;
    private int count;

    @Inject
    public FirebaseDataSource(StorageReference storageReference, FirebaseDatabase firebaseDatabase) {
        this.storageReference = storageReference;
        this.firebaseDatabase = firebaseDatabase;
    }

    private void saveVideoInfo(FlowableEmitter emitter, String videoUri, String questionUri, String deviceToken, String fileType, String username) {
        DatabaseReference videosRef = firebaseDatabase.getReference(Constants.QUESTIONS_NODE);
        QuestionItem questionItem = new QuestionItem();
        questionItem.setQuestionMediaUri(videoUri);
        questionItem.setUsername(username);
        questionItem.setTimestamp(-System.currentTimeMillis());
        questionItem.setDeviceToken(deviceToken);
        questionItem.setIsJustUploaded(true);
        questionItem.setType(fileType);
        questionItem.setQuestionAudioUri(questionUri);
        videosRef.push().setValue(questionItem).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                emitter.onComplete();
            } else {
                emitter.onError(task.getException());
            }
        });

        DatabaseReference devicesRef = firebaseDatabase.getReference("devices");
        devicesRef.child(deviceToken).setValue(true);
    }

    public Flowable<Double> uploadVideo(Uri videoUri, String fileType, String deviceToken, String username) {
        return Flowable.create(emitter -> {
            final StorageReference reference = storageReference.child(Constants.STORAGE_REF + System.currentTimeMillis() + "." + fileType);
            UploadTask uploadTask = reference.putFile(videoUri);
            uploadTask.addOnProgressListener(snapshot -> {
                double progress = (100.0 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                emitter.onNext(progress);
            }).continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    emitter.onError(task.getException());
                }

                // Continue with the task to get the download URL
                reference.getDownloadUrl().addOnCompleteListener(task1 -> saveVideoInfo(emitter, task1.getResult().toString(), null, deviceToken, username, username));
                return reference.getDownloadUrl();
            });
        }, BackpressureStrategy.BUFFER);
    }

    public Flowable<UploadTask.TaskSnapshot> uploadVideoAsService(Uri fileUri, String fileType, String questionAudioUri, String deviceToken, String username) {
        return Flowable.create(emitter -> {
            final StorageReference reference = storageReference.child(Constants.STORAGE_REF + System.currentTimeMillis() + "." + fileType);
            UploadTask uploadFileTask = reference.putFile(fileUri);
            uploadFileTask.addOnProgressListener(snapshot -> {
                double progress = (100.0 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                emitter.onNext(snapshot);
            }).continueWithTask(fileTask -> {
                if (!fileTask.isSuccessful()) {
                    emitter.onError(fileTask.getException());
                    Log.e(TAG, "then: " + fileTask.getException());
                }
                // Continue with the fileTask to get the download URL
                reference.getDownloadUrl().addOnCompleteListener(fileCompletedTask -> {
                    if (questionAudioUri != null) {
                        //so it's an image with audio, upload audio firstly before saving in the database
                        StorageReference audioQuestionsRef = storageReference.child(Constants.STORAGE_REF + "Audio Questions/" + System.currentTimeMillis() + Constants.AUDIO_FILE_TYPE);
                        Uri audioFile = Uri.fromFile(new File(questionAudioUri));
                        UploadTask uploadAudioTask = audioQuestionsRef.putFile(audioFile);
                        uploadAudioTask.continueWithTask(audioTask -> {
                            if (!audioTask.isSuccessful()) {
                                emitter.onError(audioTask.getException());
                                Log.e(TAG, "then: " + fileTask.getException());
                            }
                            audioQuestionsRef.getDownloadUrl().addOnCompleteListener(audioCompletedTask -> {
                                saveVideoInfo(emitter,
                                        fileCompletedTask.getResult().toString(),
                                        audioCompletedTask.getResult().toString(),
                                        deviceToken, fileType, username);
                            });
                            return audioQuestionsRef.getDownloadUrl();
                        });
                    } else {
                        saveVideoInfo(emitter, fileCompletedTask.getResult().toString(), null, deviceToken, fileType, username);
                    }
                });
                return reference.getDownloadUrl();
            }).addOnFailureListener(e -> {
                Log.i(TAG, "onFailure: " + e);
                emitter.onError(e);
            });
        }, BackpressureStrategy.BUFFER);
    }

    public Single<Boolean> isCurrentUserVideosFound(String deviceToken) {
        return Single.create(emitter -> {
            final DatabaseReference devicesRef = firebaseDatabase
                    .getReference("devices")
                    .child(deviceToken);
            devicesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    boolean exist = snapshot.exists() && snapshot.getValue() != null;
                    emitter.onSuccess(exist);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    emitter.onError(error.toException());
                }
            });
        });
    }

    public Flowable<QuestionItem> getCurrentUserVideos(String deviceToken) {
        return Flowable.create(emitter -> {
            final Query videosQuery = firebaseDatabase
                    .getReference(Constants.QUESTIONS_NODE)
                    .orderByChild("deviceToken")
                    .equalTo(deviceToken)
                    .limitToLast(LIMIT_ITEM_COUNT);

            videosQuery.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot questionSnapshot, @Nullable String previousChildName) {
                    QuestionItem questionItem = questionSnapshot.getValue(QuestionItem.class);
                    questionItem.setId(questionSnapshot.getKey());
                    questionItem.setDeviceToken(deviceToken);
                    emitter.onNext(questionItem);
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                    Log.e(TAG, "onChildRemoved: " + snapshot.getValue());
                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }, BackpressureStrategy.BUFFER);
    }

    public Single<Boolean> isOtherVideosFound(String deviceToken) {
        return Single.create(emitter -> {
            final DatabaseReference devicesRef = firebaseDatabase.getReference("devices");
            devicesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    boolean found = false;
                    if (snapshot.hasChildren()) {
                        for (DataSnapshot deviceSnapshot : snapshot.getChildren()) {
                            if (!deviceSnapshot.getKey().equals(deviceToken)) {
                                found = true;
                                break;
                            }
                        }
                        emitter.onSuccess(found);
                        devicesRef.removeEventListener(this);
                    } else {
                        emitter.onSuccess(false);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    emitter.onError(error.toException());
                }
            });
        });
    }

    public Flowable<QuestionItem> getVideos3(String deviceToken, Long lastTimeStamp, boolean isCurrentUser, boolean newUploadedVideo) {
        return Flowable.create(emitter -> {

        }, BackpressureStrategy.BUFFER);
    }

    private List<QuestionItem> videoItems;

    public Flowable<List<QuestionItem>> getOtherUsersVideos(String deviceToken, Long lastTimeStamp, boolean isCurrentUser, boolean newUploadedVideo) {
        return Flowable.create(emitter -> {
            count = 0;
            sentItemsCount = 0;
            videoItems = new ArrayList<>();
            Query videoQuery;
            if (newUploadedVideo) {
                videoQuery = firebaseDatabase
                        .getReference(Constants.QUESTIONS_NODE)
                        .orderByChild("timestamp")
                        .limitToFirst(1);
            } else if (lastTimeStamp != null) {
                videoQuery = firebaseDatabase
                        .getReference(Constants.QUESTIONS_NODE)
                        .orderByChild("timestamp")
                        .startAt(lastTimeStamp)
                        .limitToFirst(LIMIT_ITEM_COUNT);
            } else {
                videoQuery = firebaseDatabase
                        .getReference(Constants.QUESTIONS_NODE)
                        .orderByChild("timestamp")
                        .limitToFirst(LIMIT_ITEM_COUNT);
            }
            //get count of retrieved videos firstly to check if there size smaller than the limit
            videoQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    int size = Long.valueOf(StreamSupport.stream(snapshot.getChildren().spliterator(), false).count()).intValue();
                    if (size == 0) {
                        emitter.onNext(videoItems);
//                        return;
                    }
                    videoQuery.addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(@NonNull DataSnapshot questionSnapshot, @Nullable String previousChildName) {
                            QuestionItem questionItem = questionSnapshot.getValue(QuestionItem.class);
                            questionItem.setId(questionSnapshot.getKey());
                            if (questionItem.getLIKES() != null
                                    && questionItem.getLIKES().getUsers() != null
                                    && questionItem.getLIKES().getUsers().containsKey(deviceToken)) {
                                questionItem.setLikedByCurrentUser(true);
                            }
                            if (questionItem.getDISLIKES() != null
                                    && questionItem.getDISLIKES().getUsers() != null
                                    && questionItem.getDISLIKES().getUsers().containsKey(deviceToken)) {
                                questionItem.setDislikedByCurrentUser(true);
                            }
                            if (isCurrentUser) {
                                if (questionItem.getDeviceToken().equals(deviceToken)) {
                                    videoItems.add(questionItem);
                                    sentItemsCount++;
                                }
                            } else {
                                if (!questionItem.getDeviceToken().equals(deviceToken)) {
                                    videoItems.add(questionItem);
                                    sentItemsCount++;
                                }
                            }
                            count++;

                            //if video is just uploaded, sent it
                            if (isCurrentUser && questionItem.isJustUploaded() && questionItem.getDeviceToken().equals(deviceToken)) {
                                //set value of just uploaded to false, and save to database
                                questionSnapshot.child("justUploaded")
                                        .getRef()
                                        .setValue(false)
                                        .addOnCompleteListener(task -> {
                                            //sent the item to the user
                                            questionItem.setIsJustUploaded(false);
                                            questionItem.setFlag(Constants.UPLOADED);
                                            List<QuestionItem> uploadedItemList = new ArrayList<>();
                                            uploadedItemList.add(questionItem);
                                            emitter.onNext(uploadedItemList);
                                        });
                            } else {

                                if (count == size) {
                                    if (size < LIMIT_ITEM_COUNT && sentItemsCount == 0) {
                                        //no more data
                                        emitter.onNext(videoItems);
                                    } else if (size < LIMIT_ITEM_COUNT && sentItemsCount > 0) {
                                        emitter.onNext(videoItems);
                                    } else if (size == LIMIT_ITEM_COUNT && sentItemsCount == 0) {
                                        //retrieve data again
                                        QuestionItem latestItem = new QuestionItem();
                                        latestItem.setFlag(Constants.LATEST);
                                        latestItem.setTimestamp(questionItem.getTimestamp());
                                        videoItems.add(latestItem);
                                        emitter.onNext(videoItems);
                                    } else if (size == LIMIT_ITEM_COUNT && sentItemsCount > 0) {
                                        //send retrieved items
                                        emitter.onNext(videoItems);
                                    }
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

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }, BackpressureStrategy.BUFFER);
    }

    public Completable uploadRecordedAudio(AudioAnswer audioAnswer, QuestionItem questionItem, String username) {
        return Completable.create(emitter -> {
            StorageReference storageReference = FirebaseStorage.getInstance().getReference(Constants.AUDIO_PATH + System.currentTimeMillis() + Constants.AUDIO_FILE_TYPE);
            StorageMetadata metadata = new StorageMetadata.Builder()
                    .setContentType("audio/mpeg")
                    .build();
            Uri audioFile = Uri.fromFile(new File(audioAnswer.getAudioUri()));
            storageReference.putFile(audioFile, metadata).addOnSuccessListener(success -> {
                Task<Uri> audioUrl = success.getStorage().getDownloadUrl();
                audioUrl.addOnCompleteListener(path -> {
                    if (path.isSuccessful()) {
                        String url = path.getResult().toString();
                        DatabaseReference audioRef = FirebaseDatabase.getInstance()
                                .getReference(Constants.QUESTIONS_NODE)
                                .child(questionItem.getId())
                                .child(Constants.ANSWERS_NODE);

//                            String pushId = audioRef.push().getKey();
                        HashMap<String, Object> updates = new HashMap<>();
                        updates.put("audioUri", url);
                        updates.put("recordedUser", username);
                        updates.put("audioLength", audioAnswer.getAudioLength());
                        audioRef.push().updateChildren(updates).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                emitter.onComplete();
                            } else {
                                emitter.onError(new Throwable("خطأ اثناء تحميل التسجيل"));

                            }
                        });
                    }
                });
            }).addOnFailureListener(e -> {
                emitter.onError(new Throwable("خطأ اثناء تحميل التسجيل"));
                Log.e("Error", "onFailure: " + e.getMessage());
            });

        });
    }

    public Flowable<AudioAnswer> getRecordedAudiosForQuestion(QuestionItem questionItem) {
        return Flowable.create(emitter -> {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                    .getReference(Constants.QUESTIONS_NODE)
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
        }, BackpressureStrategy.BUFFER);
    }

    /*
     ** old
     */
    public Flowable<QuestionItem> getOtherUsersVideosOld(String deviceToken) {
        return Flowable.create(emitter -> {

            final DatabaseReference videosRef = firebaseDatabase
                    .getReference(Constants.QUESTIONS_NODE);
            //check firstly if there is data exists
            videosRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.hasChildren()) {
                        boolean otherDataExists = false;
                        for (DataSnapshot deviceSnapshot : snapshot.getChildren()) {
                            if (!deviceSnapshot.getKey().equals(deviceToken)) {
                                otherDataExists = true;
//                                    childCount++;
//                                    break;
                            }
                        }
                        if (!otherDataExists) {
                            //send item object with empty data to trigger no data screen
                            emitter.onNext(new QuestionItem());
                        } else {
//                                videosRef.addChildEventListener(new RetrieveOtherVideosChildEventListener(deviceToken, null, emitter));
                        }
                    } else {
                        emitter.onNext(new QuestionItem());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }, BackpressureStrategy.BUFFER);
    }

    public Flowable<QuestionItem> updateReactions(Reactions.ReactType type, String questionId, String userId, int newCount, boolean increase, String deviceToken) {
        return Flowable.create(emitter -> {
            //update question item, retrieved
            DatabaseReference questionReactRef = firebaseDatabase
                    .getReference(Constants.QUESTIONS_NODE)
                    .child(questionId)
                    .child(type.toString());

            DatabaseReference questionReactCountRef = questionReactRef.child("count");

            //update count to the new value
            questionReactRef.child("count").setValue(newCount).addOnCompleteListener(task -> {
                //update users list
                if (increase) {
                    questionReactRef.child("users").child(userId).setValue(true);
                } else {
                    questionReactRef.child("users").child(userId).removeValue();
                }
            });
        }, BackpressureStrategy.BUFFER);
    }

    public void addQuestionReactionListeners(String questionId) {
        DatabaseReference questionLikesRef = firebaseDatabase
                .getReference(Constants.QUESTIONS_NODE)
                .child(questionId)
                .child("LIKES");
        questionLikesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                snapshot.getValue();
                Log.i(TAG, "onChildAdded: " + snapshot.getKey());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        DatabaseReference questionDisLikesRef = firebaseDatabase
                .getReference(Constants.QUESTIONS_NODE)
                .child(questionId)
                .child("DISLIKES");
        questionDisLikesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                snapshot.getValue();
                Log.i(TAG, "onChildAdded: " + snapshot.getKey());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
