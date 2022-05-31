package com.egrobots.grassanalysis.datasource.locale;

import android.content.SharedPreferences;

import javax.inject.Inject;

public class SharedPreferencesDataSource {

    private final SharedPreferences sharedPreferences;

    @Inject
    public SharedPreferencesDataSource(SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
    }


    public String getDeviceToken() {
        return sharedPreferences.getString("DEVICE_TOKEN", null);
    }
}
