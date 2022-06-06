package com.egrobots.grassanalysis.presentation.recordscreen;

import android.net.Uri;
import android.util.Log;

import com.egrobots.grassanalysis.data.DatabaseRepository;
import com.egrobots.grassanalysis.data.LocalDataRepository;
import com.egrobots.grassanalysis.utils.StateResource;

import javax.inject.Inject;

import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;
import io.reactivex.CompletableObserver;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class RecordScreenViewModel extends ViewModel {

    private static final String TAG = "RecordScreenViewModel";
    private DatabaseRepository databaseRepository;
    private LocalDataRepository localDataRepository;
    private CompositeDisposable disposable = new CompositeDisposable();
    private MediatorLiveData<StateResource> onStatusChange = new MediatorLiveData<>();
    private MediatorLiveData<Double> uploadingProgress = new MediatorLiveData<>();

    @Inject
    public RecordScreenViewModel(DatabaseRepository databaseRepository, LocalDataRepository localDataRepository) {
        Log.d(TAG, "RecordScreenViewModel: working...");
        this.databaseRepository = databaseRepository;
        this.localDataRepository = localDataRepository;
    }

    public void uploadVideo(Uri videoUri, String fileType) {
        databaseRepository.uploadVideo(videoUri, fileType, localDataRepository.getDeviceToken(), localDataRepository.getUsername())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .toObservable()
                .subscribe(new Observer<Double>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        disposable.add(d);
                        onStatusChange.setValue(StateResource.loading());
                    }

                    @Override
                    public void onNext(Double progress) {
                        uploadingProgress.setValue(progress);
                    }

                    @Override
                    public void onError(Throwable e) {
                        onStatusChange.setValue(StateResource.error(e.getMessage()));
                        Log.d(TAG, "onError: " + e);
                    }

                    @Override
                    public void onComplete() {
                        onStatusChange.setValue(StateResource.success());
                    }
                });
    }

    public MediatorLiveData<StateResource> observeStatusChange() {
        return onStatusChange;
    }

    public MediatorLiveData<Double> observeUploadingProgress() {
        return uploadingProgress;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposable.clear();
    }
}
