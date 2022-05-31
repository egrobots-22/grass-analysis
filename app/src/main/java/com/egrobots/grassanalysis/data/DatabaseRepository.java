package com.egrobots.grassanalysis.data;

import android.net.Uri;

import com.egrobots.grassanalysis.datasource.remote.FirebaseDataSource;

import javax.inject.Inject;

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

    public Flowable<String> getAllVideos() {
        return firebaseDataSource.getAllVideos();
    }

}
