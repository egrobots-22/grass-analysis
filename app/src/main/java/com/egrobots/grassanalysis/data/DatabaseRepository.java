package com.egrobots.grassanalysis.data;

import android.net.Uri;

import com.egrobots.grassanalysis.data.model.VideoQuestionItem;
import com.egrobots.grassanalysis.datasource.remote.FirebaseDataSource;

import java.io.File;

import javax.inject.Inject;

import io.reactivex.Completable;
import io.reactivex.Flowable;

public class DatabaseRepository {

    FirebaseDataSource firebaseDataSource;

    @Inject
    public DatabaseRepository(FirebaseDataSource firebaseDataSource) {
        this.firebaseDataSource = firebaseDataSource;
    }

    public Flowable<Double> uploadVideo(Uri videoUri, String fileType, String deviceToken) {
        return firebaseDataSource.uploadVideo(videoUri, fileType, deviceToken);
    }

    public Flowable<VideoQuestionItem> getAllVideos() {
        return firebaseDataSource.getAllVideos();
    }

    public Flowable<VideoQuestionItem> getCurrentUserVideos(String deviceToken) {
        return firebaseDataSource.getCurrentUserVideos(deviceToken);
    }

    public Flowable<VideoQuestionItem> getOtherUsersVideos(String deviceToken) {
        return firebaseDataSource.getOtherUsersVideos(deviceToken);
    }

    public Completable saveAudio(File recordFile, String questionId, String deviceToken) {
        return firebaseDataSource.saveAudio(recordFile, questionId, deviceToken);
    }

}
