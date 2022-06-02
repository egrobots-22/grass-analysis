package com.egrobots.grassanalysis.di.recordscreen;

import com.egrobots.grassanalysis.di.ViewModelKey;
import com.egrobots.grassanalysis.presentation.videos.swipeablevideos.SwipeableVideosViewModel;

import androidx.lifecycle.ViewModel;
import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;

@Module
public abstract class SwipeableScreenViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(SwipeableVideosViewModel.class)
    public abstract ViewModel bindViewModel(SwipeableVideosViewModel viewModel);
}
