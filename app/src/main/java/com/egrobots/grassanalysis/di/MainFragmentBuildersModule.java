package com.egrobots.grassanalysis.di;

import com.egrobots.grassanalysis.di.tabview.SwipeableVideosAdapterModule;
import com.egrobots.grassanalysis.presentation.videos.swipeablevideos.SwipeableVideosFragment;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class MainFragmentBuildersModule {

    @ContributesAndroidInjector(modules = SwipeableVideosAdapterModule.class)
    abstract SwipeableVideosFragment contributeChatFragment();
}
