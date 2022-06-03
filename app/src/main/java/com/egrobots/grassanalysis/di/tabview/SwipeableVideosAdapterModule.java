package com.egrobots.grassanalysis.di.tabview;

import com.egrobots.grassanalysis.adapters.VideosAdapter;
import com.egrobots.grassanalysis.data.DatabaseRepository;

import dagger.Module;
import dagger.Provides;

@Module
public class SwipeableVideosAdapterModule {

    @Provides
    static VideosAdapter provideVideosAdapter(DatabaseRepository databaseRepository) {
        return new VideosAdapter(databaseRepository);
    }
}
