package com.egrobots.grassanalysis.di;

import com.egrobots.grassanalysis.di.recordscreen.RecordScreenViewModelModule;
import com.egrobots.grassanalysis.presentation.SplashActivity;
import com.egrobots.grassanalysis.presentation.recordscreen.RecordScreenActivity;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class ActivityBuildersModule {

    @ContributesAndroidInjector
    abstract SplashActivity contributeSplashActivity();

    @ContributesAndroidInjector(modules ={
            RecordScreenViewModelModule.class
    })
    abstract RecordScreenActivity contributeRecordScreenActivity();

}
