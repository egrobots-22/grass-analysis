package com.egrobots.grassanalysis.di;

import com.egrobots.grassanalysis.presentation.videos.swipeablevideos.SwipeableVideosFragment;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class MainFragmentBuildersModule {

    @ContributesAndroidInjector
    abstract SwipeableVideosFragment contributeChatFragment();
}
