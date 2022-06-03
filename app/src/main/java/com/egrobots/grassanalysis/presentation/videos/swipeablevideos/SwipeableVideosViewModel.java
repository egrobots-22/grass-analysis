package com.egrobots.grassanalysis.presentation.videos.swipeablevideos;

import android.net.Uri;
import android.util.Log;

import com.egrobots.grassanalysis.data.DatabaseRepository;
import com.egrobots.grassanalysis.data.LocalDataRepository;
import com.egrobots.grassanalysis.data.model.VideoQuestionItem;
import com.egrobots.grassanalysis.utils.Constants;
import com.egrobots.grassanalysis.utils.StateResource;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.HashMap;
import java.util.Objects;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;
import io.reactivex.CompletableObserver;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class SwipeableVideosViewModel extends ViewModel {
    private DatabaseRepository databaseRepository;
    private LocalDataRepository localDataRepository;
    private CompositeDisposable disposable = new CompositeDisposable();
    private MediatorLiveData<VideoQuestionItem> videoUris = new MediatorLiveData<>();
    private MediatorLiveData<StateResource> uploadAudioState = new MediatorLiveData<>();

    @Inject
    public SwipeableVideosViewModel(DatabaseRepository databaseRepository, LocalDataRepository localDataRepository) {
        this.databaseRepository = databaseRepository;
        this.localDataRepository = localDataRepository;
    }

    public void getAllVideos() {
        databaseRepository.getAllVideos()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .toObservable()
                .subscribe(new Observer<VideoQuestionItem>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        disposable.add(d);
                    }

                    @Override
                    public void onNext(VideoQuestionItem questionItem) {
                        videoUris.setValue(questionItem);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public void getCurrentUserVideos() {
        databaseRepository.getCurrentUserVideos(localDataRepository.getDeviceToken())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .toObservable()
                .subscribe(new Observer<VideoQuestionItem>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        disposable.add(d);
                    }

                    @Override
                    public void onNext(VideoQuestionItem questionItem) {
                        videoUris.setValue(questionItem);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public void getOtherUsersVideos() {
        databaseRepository.getOtherUsersVideos(localDataRepository.getDeviceToken())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .toObservable()
                .subscribe(new Observer<VideoQuestionItem>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        disposable.add(d);
                    }

                    @Override
                    public void onNext(VideoQuestionItem questionItem) {
                        videoUris.setValue(questionItem);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public void uploadRecordedAudio(File recordFile, VideoQuestionItem questionItem) {
        databaseRepository.uploadRecordedAudio(recordFile, questionItem)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        disposable.add(d);
                        uploadAudioState.setValue(StateResource.loading());
                    }

                    @Override
                    public void onComplete() {
                        uploadAudioState.setValue(StateResource.success());
                    }

                    @Override
                    public void onError(Throwable e) {
                        uploadAudioState.setValue(StateResource.error(Objects.requireNonNull(e.getMessage())));
                    }
                });
    }

    public MediatorLiveData<VideoQuestionItem> observeVideoUris() {
        return videoUris;
    }

    public MediatorLiveData<StateResource> observeUploadAudioState() {
        return uploadAudioState;
    }
}
