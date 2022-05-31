package com.egrobots.grassanalysis.di;

import android.content.Context;

import com.egrobots.grassanalysis.utils.Utils;

import javax.inject.Singleton;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;

@Module
public abstract class UtilsModule {

    @Binds
    abstract Utils provideUtils(Utils utils);
}
