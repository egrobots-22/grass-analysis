package com.egrobots.grassanalysis.presentation.videos.swipeablevideos;

import android.util.Log;

import com.egrobots.grassanalysis.data.DatabaseRepository;
import com.egrobots.grassanalysis.data.LocalDataRepository;
import com.egrobots.grassanalysis.data.model.VideoQuestionItem;
import com.egrobots.grassanalysis.utils.StateResource;

import java.io.File;
import java.util.Objects;

import javax.inject.Inject;

import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;
import io.reactivex.CompletableObserver;
import io.reactivex.Observer;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class SwipeableVideosViewModel extends ViewModel {
    private DatabaseRepository databaseRepository;
    private LocalDataRepository localDataRepository;
    private CompositeDisposable disposable = new CompositeDisposable();
    private MediatorLiveData<VideoQuestionItem> videoUris = new MediatorLiveData<>();
    private MediatorLiveData<Boolean> existVideosState = new MediatorLiveData<>();
    private MediatorLiveData<StateResource> uploadAudioState = new MediatorLiveData<>();

    @Inject
    public SwipeableVideosViewModel(DatabaseRepository databaseRepository, LocalDataRepository localDataRepository) {
        this.databaseRepository = databaseRepository;
        this.localDataRepository = localDataRepository;
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
                        videoUris.setValue(null);
                    }
                });
    }

    public void isOtherVideosFound() {
        SingleObserver<Boolean> singleObserver = databaseRepository.isOtherVideosFound(localDataRepository.getDeviceToken())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new SingleObserver<Boolean>() {


                    @Override
                    public void onSubscribe(Disposable d) {
                        disposable.add(d);
                    }

                    @Override
                    public void onSuccess(Boolean exist) {
                        existVideosState.setValue(exist);
                        Log.e("Single", "onSuccess: ");
                    }

                    @Override
                    public void onError(Throwable e) {

                    }
                });
    }

    public void isCurrentUserVideosFound() {
        SingleObserver<Boolean> singleObserver = databaseRepository.isCurrentUserVideosFound(localDataRepository.getDeviceToken())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new SingleObserver<Boolean>() {


                    @Override
                    public void onSubscribe(Disposable d) {
                        disposable.add(d);
                    }

                    @Override
                    public void onSuccess(Boolean exist) {
                        existVideosState.setValue(exist);
                        Log.e("Single", "onSuccess: ");
                    }

                    @Override
                    public void onError(Throwable e) {

                    }
                });
    }

    public void getNextOtherUsersVideos(Long lastTimestamp, boolean isCurrentUser, boolean newVideoUploaded) {
        databaseRepository.getOtherUsersVideos(localDataRepository.getDeviceToken(), lastTimestamp, isCurrentUser, newVideoUploaded)
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
                        if (questionItem.getVideoQuestionUri() == null) {
                            videoUris.setValue(null);
                        } else {
                            videoUris.setValue(questionItem);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onComplete() {
                        videoUris.setValue(null);
                    }
                });
    }

    public void uploadRecordedAudio(File recordFile, VideoQuestionItem questionItem) {
        databaseRepository.uploadRecordedAudio(recordFile, questionItem, localDataRepository.getUsername())
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

    public MediatorLiveData<Boolean> observeExistVideosState() {
        return existVideosState;
    }
}
