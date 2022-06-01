package com.egrobots.grassanalysis.presentation.videos;

import com.egrobots.grassanalysis.data.DatabaseRepository;
import com.egrobots.grassanalysis.data.model.VideoQuestionItem;

import javax.inject.Inject;

import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class SwipeableVideosViewModel extends ViewModel {
    private DatabaseRepository databaseRepository;
    private CompositeDisposable disposable = new CompositeDisposable();
    private MediatorLiveData<VideoQuestionItem> videoUris = new MediatorLiveData<>();

    @Inject
    public SwipeableVideosViewModel(DatabaseRepository databaseRepository) {
        this.databaseRepository = databaseRepository;
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

    public MediatorLiveData<VideoQuestionItem> observeVideoUris() {
        return videoUris;
    }
}
