package com.egrobots.grassanalysis.di.recordscreen;

import com.egrobots.grassanalysis.di.ViewModelKey;
import com.egrobots.grassanalysis.presentation.recordscreen.RecordScreenViewModel;

import androidx.lifecycle.ViewModel;
import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;

@Module
public abstract class RecordScreenViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(RecordScreenViewModel.class)
    public abstract ViewModel binViewModel(RecordScreenViewModel viewModel);
}
