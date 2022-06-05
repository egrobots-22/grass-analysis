package com.egrobots.grassanalysis.di;

import com.egrobots.grassanalysis.di.recordscreen.RecordScreenViewModelModule;
import com.egrobots.grassanalysis.di.tabview.VideosTabActivityViewModelModule;
import com.egrobots.grassanalysis.presentation.start.SplashActivity;
import com.egrobots.grassanalysis.presentation.recordscreen.RecordScreenActivity;
import com.egrobots.grassanalysis.presentation.recordscreen.RecordScreenActivity2;
import com.egrobots.grassanalysis.presentation.videos.VideosTabActivity;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class ActivityBuildersModule {

    @ContributesAndroidInjector
    abstract SplashActivity contributeSplashActivity();

    @ContributesAndroidInjector(modules = RecordScreenViewModelModule.class)
    abstract RecordScreenActivity contributeRecordScreenActivity();

    @ContributesAndroidInjector(modules = RecordScreenViewModelModule.class)
    abstract RecordScreenActivity2 contributeRecordScreenActivity2();

    @ContributesAndroidInjector(modules = {MainFragmentBuildersModule.class
            , VideosTabActivityViewModelModule.class
    })
    abstract VideosTabActivity contributeVideosTabActivity();

}
