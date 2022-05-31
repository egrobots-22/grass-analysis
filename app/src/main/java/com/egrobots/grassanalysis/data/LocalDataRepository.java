package com.egrobots.grassanalysis.data;

import android.content.SharedPreferences;

import com.egrobots.grassanalysis.datasource.locale.SharedPreferencesDataSource;

import javax.inject.Inject;

public class LocalDataRepository {

    private SharedPreferencesDataSource sharedPreferencesDataSource;

    @Inject
    public LocalDataRepository(SharedPreferencesDataSource sharedPreferencesDataSource) {
        this.sharedPreferencesDataSource = sharedPreferencesDataSource;
    }

    public String getDeviceToken() {
        return sharedPreferencesDataSource.getDeviceToken();
    }
}
