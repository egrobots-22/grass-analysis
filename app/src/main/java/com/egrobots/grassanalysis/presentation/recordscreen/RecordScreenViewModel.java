package com.egrobots.grassanalysis.presentation.recordscreen;

import android.net.Uri;
import android.util.Log;

import com.egrobots.grassanalysis.data.DatabaseRepository;
import com.egrobots.grassanalysis.utils.StateResource;

import javax.inject.Inject;

import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;
import io.reactivex.CompletableObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class RecordScreenViewModel extends ViewModel {

    private static final String TAG = "RecordScreenViewModel";
    private DatabaseRepository databaseRepository;
    private CompositeDisposable disposable = new CompositeDisposable();
    private MediatorLiveData<StateResource> onStatusChange = new MediatorLiveData<>();

    @Inject
    public RecordScreenViewModel(DatabaseRepository databaseRepository) {
        Log.d(TAG, "RecordScreenViewModel: working...");
        this.databaseRepository = databaseRepository;
    }

    public void uploadVideo(Uri videoUri, boolean test) {
        databaseRepository.uploadVideo(videoUri, true)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        disposable.add(d);
                        onStatusChange.setValue(StateResource.loading());
                    }

                    @Override
                    public void onComplete() {
                        onStatusChange.setValue(StateResource.success());
                    }

                    @Override
                    public void onError(Throwable e) {
                        onStatusChange.setValue(StateResource.error(e.getMessage()));
                    }
                });
    }

    public MediatorLiveData<StateResource> observeStatusChange() {
        return onStatusChange;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposable.clear();
    }
}
