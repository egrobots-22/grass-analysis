package com.egrobots.grassanalysis;

import androidx.appcompat.app.AppCompatActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.egrobots.grassanalysis.presentation.RecordScreenActivity;

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