package com.egrobots.grassanalysis.presentation.start;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.egrobots.grassanalysis.R;
import com.egrobots.grassanalysis.presentation.recordscreen.RecordScreenActivity2;
import com.egrobots.grassanalysis.presentation.videos.VideosTabActivity;

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
        startActivity(new Intent(this, RecordScreenActivity2.class));
    }

    @OnClick(R.id.openVideosScreenButton)
    public void onOpenVideosScreenButton() {
        startActivity(new Intent(this, VideosTabActivity.class));
    }
}