package com.egrobots.grassanalysis.presentation.recordscreen;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.webkit.MimeTypeMap;
import android.widget.Toast;
import android.widget.VideoView;

import com.egrobots.grassanalysis.R;
import com.egrobots.grassanalysis.utils.Utils;
import com.egrobots.grassanalysis.utils.ViewModelProviderFactory;
import com.egrobots.grassanalysis.utils.LoadingDialog;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import butterknife.BindView;
import butterknife.ButterKnife;
import dagger.android.support.DaggerAppCompatActivity;

public class RecordScreenActivity extends DaggerAppCompatActivity {
    private static final int REQUEST_VIDEO_CAPTURE = 1;

    @BindView(R.id.videoView)
    VideoView videoView;
    @Inject
    ViewModelProviderFactory providerFactory;
    @Inject
    LoadingDialog loadingDialog;
    @Inject
    Utils utils;
    private RecordScreenViewModel recordScreenViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_screen);
        ButterKnife.bind(this);

        recordScreenViewModel = new ViewModelProvider(getViewModelStore(), providerFactory).get(RecordScreenViewModel.class);
        observeStatusChange();
        observerUploadingProgress();
        dispatchTakeVideoIntent();
    }

    private void observerUploadingProgress() {
        recordScreenViewModel.observeUploadingProgress().observe(this, progress -> {
            loadingDialog.setTitle(getString(R.string.uploaded) + (int) progress.doubleValue() + "%");
        });
    }

    private void observeStatusChange() {
        recordScreenViewModel.observeStatusChange().observe(this, stateResource -> {
            if (stateResource != null) {
                switch (stateResource.status) {
                    case LOADING:
                        loadingDialog.show(getSupportFragmentManager(), "loading_dialog");
                        break;
                    case SUCCESS:
                        loadingDialog.dismiss();
                        finish();
                        Toast.makeText(this, "Video uploaded Successfully", Toast.LENGTH_SHORT).show();
                        break;
                    case ERROR:
                        loadingDialog.dismiss();
                        Toast.makeText(this, stateResource.message, Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });
    }

    private void dispatchTakeVideoIntent() {
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK) {
            Uri videoUri = intent.getData();
            videoView.setVideoURI(videoUri);
            String fileType = utils.getFieType(videoUri);
            recordScreenViewModel.uploadVideo(videoUri, fileType);
        }
    }
}