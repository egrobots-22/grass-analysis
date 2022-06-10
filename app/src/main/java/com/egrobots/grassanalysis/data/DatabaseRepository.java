package com.egrobots.grassanalysis.data;

import android.net.Uri;

import com.egrobots.grassanalysis.data.model.AudioAnswer;
import com.egrobots.grassanalysis.data.model.VideoQuestionItem;
import com.egrobots.grassanalysis.datasource.remote.FirebaseDataSource;
import com.google.firebase.storage.UploadTask;

import java.io.File;

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

    public Flowable<UploadTask.TaskSnapshot> uploadVideoAsService(Uri videoUri, String fileType, String deviceToken, String username) {
        return firebaseDataSource.uploadVideoAsService(videoUri, fileType, deviceToken, username);
    }

    public Flowable<VideoQuestionItem> getCurrentUserVideos(String deviceToken) {
        return firebaseDataSource.getCurrentUserVideos(deviceToken);
    }

    public Flowable<VideoQuestionItem> getOtherUsersVideos(String deviceToken, Long lastTimestamp, boolean isCurrentUser, boolean newVideoUploaded) {
        return firebaseDataSource.getOtherUsersVideos(deviceToken, lastTimestamp, isCurrentUser, newVideoUploaded);
    }

    public Completable uploadRecordedAudio(File recordFile, VideoQuestionItem questionItem, String username) {
        return firebaseDataSource.uploadRecordedAudio(recordFile, questionItem, username);
    }

    public Flowable<AudioAnswer> getRecordedAudiosForQuestion(VideoQuestionItem questionItem) {
        return firebaseDataSource.getRecordedAudiosForQuestion(questionItem);
    }

    public Single<Boolean> isOtherVideosFound(String deviceToken) {
        return firebaseDataSource.isOtherVideosFound(deviceToken);
    }

    public Single<Boolean> isCurrentUserVideosFound(String deviceToken) {
        return firebaseDataSource.isCurrentUserVideosFound(deviceToken);
    }
}