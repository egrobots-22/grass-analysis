package com.egrobots.grassanalysis;

import android.content.Context;
import android.content.SharedPreferences;

import com.egrobots.grassanalysis.di.DaggerAppComponent;
import com.egrobots.grassanalysis.network.NetworkMonitoringUtil;
import com.egrobots.grassanalysis.utils.Constants;

import java.util.Random;
import java.util.UUID;

import androidx.multidex.MultiDex;
import dagger.android.AndroidInjector;
import dagger.android.support.DaggerApplication;

public class BaseActivity extends DaggerApplication {

    private NetworkMonitoringUtil mNetworkMonitoringUtil;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        mNetworkMonitoringUtil = new NetworkMonitoringUtil(base);
        mNetworkMonitoringUtil.checkNetworkState();
        mNetworkMonitoringUtil.registerNetworkCallbackEvents();

        SharedPreferences sharedPreferences = getSharedPreferences(Constants.PREF_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String deviceToken = sharedPreferences.getString(Constants.DEVICE_TOKEN, null);
        if (deviceToken == null) {
            String deviceUniqueId = UUID.randomUUID().toString();
            String username = givenUsingJava8_whenGeneratingRandomAlphabeticString_thenCorrect();
            editor.putString(Constants.USER_NAME, "User: " + username);
            editor.putString(Constants.DEVICE_TOKEN, deviceUniqueId);
            editor.apply();
        }
        MultiDex.install(this);
    }

    @Override
    protected AndroidInjector<? extends DaggerApplication> applicationInjector() {
        return DaggerAppComponent.builder().application(this).build();
    }

    public String givenUsingJava8_whenGeneratingRandomAlphabeticString_thenCorrect() {
        int leftLimit = 97; // letter 'a'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = 10;
        Random random = new Random();

        return random.ints(leftLimit, rightLimit + 1)
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();

    }
}
