package com.egrobots.grassanalysis.di;

import com.egrobots.grassanalysis.presentation.videos.tabs.SwipeableVideosFragment;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class MainFragmentBuildersModule {

    @ContributesAndroidInjector
    abstract SwipeableVideosFragment contributeChatFragment();
}
