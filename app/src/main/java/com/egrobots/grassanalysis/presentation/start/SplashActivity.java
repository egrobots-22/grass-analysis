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

    private static final long SPLASH_TIME_OUT = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(() -> {
//            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
//                if (!task.isSuccessful()) {
//                    startActivity(new Intent(SplashActivity.this, VideosTabActivity.class));
//                } else {
//                    // Get new FCM registration token
//                    String token = task.getResult();
//                    //set user random name to shared pref
//                    SharedPreferences sharedPreferences = getSharedPreferences(Constants.PREF_NAME, MODE_PRIVATE);
//                    SharedPreferences.Editor editor = sharedPreferences.edit();
//                    String deviceName = sharedPreferences.getString(Constants.USER_NAME, null);
//                    if (deviceName == null) {
//                        deviceName = UUID.randomUUID().toString();
//                        editor.putString(Constants.USER_NAME, deviceName);
//                        editor.putString(Constants.DEVICE_TOKEN, token);
//                        editor.apply();
//                    }
//                    startActivity(new Intent(SplashActivity.this, VideosTabActivity.class));
//                }
//            });
            startActivity(new Intent(SplashActivity.this, VideosTabActivity.class));
        }, SPLASH_TIME_OUT);
    }
}