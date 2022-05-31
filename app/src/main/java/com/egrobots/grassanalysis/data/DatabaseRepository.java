package com.egrobots.grassanalysis.data;

import android.net.Uri;

import com.egrobots.grassanalysis.datasource.FirebaseDataSource;

import javax.inject.Inject;

import io.reactivex.Completable;

public class DatabaseRepository {

    FirebaseDataSource firebaseDataSource;

    @Inject
    public DatabaseRepository(FirebaseDataSource firebaseDataSource) {
        this.firebaseDataSource = firebaseDataSource;
    }

    public Completable uploadVideo(Uri videoUri, boolean test) {
        return firebaseDataSource.uploadVideo(videoUri, test);
    }

    public Completable saveVideoInfo(boolean test) {
        return firebaseDataSource.saveVideoInfo(test);
    }
}
