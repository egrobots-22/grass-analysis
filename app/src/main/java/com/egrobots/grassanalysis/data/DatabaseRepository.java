package com.egrobots.grassanalysis.data;

import android.net.Uri;

import com.egrobots.grassanalysis.data.model.AudioAnswer;
import com.egrobots.grassanalysis.data.model.QuestionItem;
import com.egrobots.grassanalysis.data.model.Reactions;
import com.egrobots.grassanalysis.datasource.remote.FirebaseDataSource;
import com.google.firebase.storage.UploadTask;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;

public class DatabaseRepository {

    FirebaseDataSource firebaseDataSource;

    @Inject
    public DatabaseRepository(FirebaseDataSource firebaseDataSource) {
        this.firebaseDataSource = firebaseDataSource;
    }

    public Flowable<Double> uploadVideo(Uri videoUri, String fileType, String deviceToken, String username) {
        return firebaseDataSource.uploadVideo(videoUri, fileType, deviceToken, username);
    }

    public Flowable<UploadTask.TaskSnapshot> uploadVideoAsService(Uri videoUri, String fileType, String questionAudioUri, String deviceToken, String username) {
        return firebaseDataSource.uploadVideoAsService(videoUri, fileType, questionAudioUri, deviceToken, username);
    }

    public Flowable<QuestionItem> getCurrentUserVideos(String deviceToken) {
        return firebaseDataSource.getCurrentUserVideos(deviceToken);
    }

    public Flowable<List<QuestionItem>> getOtherUsersVideos(String deviceToken, Long lastTimestamp, boolean isCurrentUser, boolean newVideoUploaded) {
        return firebaseDataSource.getOtherUsersVideos(deviceToken, lastTimestamp, isCurrentUser, newVideoUploaded);
    }

    public Completable uploadRecordedAudio(AudioAnswer audioAnswer, QuestionItem questionItem, String username) {
        return firebaseDataSource.uploadRecordedAudio(audioAnswer, questionItem, username);
    }

    public Flowable<AudioAnswer> getRecordedAudiosForQuestion(QuestionItem questionItem, String deviceToken) {
        return firebaseDataSource.getRecordedAudiosForQuestion(questionItem, deviceToken);
    }

    public Single<Boolean> isOtherVideosFound(String deviceToken) {
        return firebaseDataSource.isOtherVideosFound(deviceToken);
    }

    public Single<Boolean> isCurrentUserVideosFound(String deviceToken) {
        return firebaseDataSource.isCurrentUserVideosFound(deviceToken);
    }

    public Flowable<QuestionItem> updateReactions(Reactions.ReactType type, String questionId, String audioAnswerId, String userId, int newCount, boolean increase, String deviceToken) {
        return firebaseDataSource.updateReactions(type, questionId, audioAnswerId, userId, newCount, increase, deviceToken);
    }
}