package com.egrobots.grassanalysis.di.tabview;

import com.egrobots.grassanalysis.adapters.VideosAdapter;
import com.egrobots.grassanalysis.data.DatabaseRepository;
import com.egrobots.grassanalysis.data.LocalDataRepository;

import dagger.Module;
import dagger.Provides;

@Module
public class SwipeableVideosAdapterModule {

    @Provides
    static VideosAdapter provideVideosAdapter(DatabaseRepository databaseRepository, LocalDataRepository localDataRepository) {
        return new VideosAdapter(databaseRepository, localDataRepository);
    }
}
