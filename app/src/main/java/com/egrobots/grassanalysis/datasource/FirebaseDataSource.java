package com.egrobots.grassanalysis.datasource;

import android.net.Uri;

import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.StorageReference;

import javax.inject.Inject;

import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableOnSubscribe;

public class FirebaseDataSource {

    private static final String TAG = "FirebaseDataSource";
    private StorageReference storageReference;
    private FirebaseDatabase firebaseDatabase;

    @Inject
    public FirebaseDataSource(StorageReference storageReference, FirebaseDatabase firebaseDatabase) {
        this.storageReference = storageReference;
        this.firebaseDatabase = firebaseDatabase;
    }

    public Completable uploadVideo(Uri videoUri, boolean test) {
        return Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(CompletableEmitter emitter) throws Exception {
                if (test) {
                    emitter.onComplete();
                } else {
                    emitter.onError(new Throwable("test error message"));
                }
            }
        });
    }

    public Completable saveVideoInfo(boolean test) {
        return Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(CompletableEmitter emitter) throws Exception {
                if (test) {
                    emitter.onComplete();
                } else {
                    emitter.onError(new Throwable("test error message"));
                }
            }
        });
    }
}
