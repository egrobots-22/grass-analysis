package com.egrobots.grassanalysis.presentation.start;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import com.egrobots.grassanalysis.R;
import com.egrobots.grassanalysis.presentation.videos.VideosTabActivity;
import com.egrobots.grassanalysis.utils.Constants;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.UUID;

public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_TIME_OUT = 500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(() -> startActivity(new Intent(SplashActivity.this, VideosTabActivity.class)), SPLASH_TIME_OUT);
    }
}