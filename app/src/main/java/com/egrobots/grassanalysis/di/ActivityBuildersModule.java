package com.egrobots.grassanalysis.di;

import com.egrobots.grassanalysis.di.recordscreen.RecordScreenViewModelModule;
import com.egrobots.grassanalysis.di.recordscreen.SwipeableScreenViewModelModule;
import com.egrobots.grassanalysis.di.tabview.VideosTabActivityViewModelModule;
import com.egrobots.grassanalysis.presentation.SplashActivity;
import com.egrobots.grassanalysis.presentation.recordscreen.RecordScreenActivity;
import com.egrobots.grassanalysis.presentation.videos.SwipeableVideosActivity;
import com.egrobots.grassanalysis.presentation.videos.tabs.VideosTabActivity;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class ActivityBuildersModule {

    @ContributesAndroidInjector
    abstract SplashActivity contributeSplashActivity();

    @ContributesAndroidInjector(modules = RecordScreenViewModelModule.class)
    abstract RecordScreenActivity contributeRecordScreenActivity();

    @ContributesAndroidInjector(modules = SwipeableScreenViewModelModule.class)
    abstract SwipeableVideosActivity contributeSwipeableVideosActivity();

    @ContributesAndroidInjector(modules = {MainFragmentBuildersModule.class
            , VideosTabActivityViewModelModule.class
    })
    abstract VideosTabActivity contributeVideosTabActivity();

}
