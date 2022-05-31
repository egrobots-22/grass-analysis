package com.egrobots.grassanalysis.di;

import android.app.Application;

import com.egrobots.grassanalysis.BaseActivity;

import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.Component;
import dagger.android.AndroidInjector;
import dagger.android.support.AndroidSupportInjectionModule;


@Singleton
@Component(modules = {ActivityBuildersModule.class,
        AndroidSupportInjectionModule.class,
        AppModule.class,
        ViewModelFactoryModule.class,
        UtilsModule.class})
public interface AppComponent extends AndroidInjector<BaseActivity> {

    @Component.Builder
    interface Builder {

        @BindsInstance
        Builder application(Application application);

        AppComponent build();
    }
}
