package com.egrobots.grassanalysis.presentation;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import com.egrobots.grassanalysis.R;
import com.egrobots.grassanalysis.presentation.recordscreen.RecordScreenActivity;

import androidx.appcompat.app.AppCompatActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class StartActivity extends AppCompatActivity {

    @BindView(R.id.openRecordScreenButton)
    Button openRecordScreenButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.openRecordScreenButton)
    public void onOpenRecordScreenClicked() {
        startActivity(new Intent(this, RecordScreenActivity.class));
    }
}