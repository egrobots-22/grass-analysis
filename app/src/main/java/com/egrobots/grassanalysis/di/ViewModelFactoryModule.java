package com.egrobots.grassanalysis.di;

import com.egrobots.grassanalysis.utils.ViewModelProviderFactory;

import androidx.lifecycle.ViewModelProvider;


import dagger.Binds;
import dagger.Module;

@Module
public abstract class ViewModelFactoryModule {

    @Binds
    public abstract ViewModelProvider.Factory bindViewModelFactory(ViewModelProviderFactory factory);
}
