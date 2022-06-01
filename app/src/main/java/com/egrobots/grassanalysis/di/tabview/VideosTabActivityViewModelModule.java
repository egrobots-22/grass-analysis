package com.egrobots.grassanalysis.di.tabview;

import com.egrobots.grassanalysis.di.ViewModelKey;
import com.egrobots.grassanalysis.presentation.videos.SwipeableVideosViewModel;

import androidx.lifecycle.ViewModel;
import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;

@Module
public abstract class VideosTabActivityViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(SwipeableVideosViewModel.class)
    public abstract ViewModel bindViewModel(SwipeableVideosViewModel viewModel);
}
