package com.egrobots.grassanalysis.presentation;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.egrobots.grassanalysis.StartActivity;
import com.egrobots.grassanalysis.R;
import com.egrobots.grassanalysis.utils.Constants;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_TIME_OUT = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(() -> {
            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
                if (!task.isSuccessful()) {
                    startActivity(new Intent(SplashActivity.this, StartActivity.class));
                } else {
                    // Get new FCM registration token
                    String token = task.getResult();
                    SharedPreferences.Editor editor = getSharedPreferences("data", MODE_PRIVATE).edit();
                    editor.putString(Constants.DEVICE_TOKEN, token);
                    editor.apply();
                    startActivity(new Intent(SplashActivity.this, StartActivity.class));
                }
            });
        }, SPLASH_TIME_OUT);
    }
}