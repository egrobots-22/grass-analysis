package com.egrobots.grassanalysis.di;

import com.egrobots.grassanalysis.utils.LoadingDialog;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class AppModule {

    @Singleton
    @Provides
    static StorageReference provideStorageReference() {
        return FirebaseStorage.getInstance().getReference();
    }

    @Singleton
    @Provides
    static FirebaseDatabase provideFirebaseDatabase() {
        return FirebaseDatabase.getInstance();
    }

    @Singleton
    @Provides
    static FirebaseMessaging provideFirebaseMessaging() {
        return FirebaseMessaging.getInstance();
    }

    @Singleton
    @Provides
    static LoadingDialog provideLoadingDialog() {
        return new LoadingDialog();
    }

}
