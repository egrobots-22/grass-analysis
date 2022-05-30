package com.egrobots.grassanalysis.presentation;

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
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import butterknife.BindView;
import butterknife.ButterKnife;

public class RecordScreenActivity extends AppCompatActivity {
    private static final int REQUEST_VIDEO_CAPTURE = 1;

    @BindView(R.id.videoView)
    VideoView videoView;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_screen);
        ButterKnife.bind(this);
        dispatchTakeVideoIntent();
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
            progressDialog = new ProgressDialog(this);
            progressDialog.setTitle(getString(R.string.uploading));
            progressDialog.show();
            uploadVideoToFirebaseStorage(videoUri);
        }
    }

    private void uploadVideoToFirebaseStorage(Uri videouri) {
        String deviceToken = getDeviceToken();
        final StorageReference reference = FirebaseStorage.getInstance()
                .getReference("Files/" + System.currentTimeMillis() + "." + getFileType(videouri));

//        reference.putFile(videouri).addOnSuccessListener(taskSnapshot -> {
//            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
//            if (uriTask.isSuccessful()) {
//                // get the link of video
//                String downloadUri = uriTask.getResult().toString();
//                DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference("videos");
//                HashMap<String, String> map = new HashMap<>();
//                map.put("video_link", downloadUri);
//                map.put("device_token", deviceToken);
//                reference1.push().setValue(map);
//                // Video uploaded successfully
//                // Dismiss dialog
//                progressDialog.dismiss();
//                Toast.makeText(this, "Video Uploaded!!", Toast.LENGTH_SHORT).show();
//                finish();
//            }
//
//        }).addOnFailureListener(e -> {
//            progressDialog.dismiss();
//            Toast.makeText(this, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
//        }).addOnProgressListener(snapshot -> {
//            // show the progress bar
//            double progress = (100.0 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
//            progressDialog.setMessage("Uploaded " + (int) progress + "%");
//        });

        UploadTask uploadTask = reference.putFile(videouri);
        Task<Uri> urlTask = uploadTask.addOnProgressListener(snapshot -> {
//            // show the progress bar
            double progress = (100.0 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
            progressDialog.setMessage(getString(R.string.uploaded) + (int) progress + "%");
        }).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }

                // Continue with the task to get the download URL
                reference.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference("videos");
                        HashMap<String, String> map = new HashMap<>();
                        map.put("video_link", task.getResult().toString());
                        map.put("device_token", deviceToken);
                        reference1.push().setValue(map);
                        // Video uploaded successfully
                        // Dismiss dialog
                        progressDialog.dismiss();
                        Toast.makeText(RecordScreenActivity.this, R.string.video_uploaded, Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
                return reference.getDownloadUrl();
            }
        });
    }

    private String getFileType(Uri videouri) {
        ContentResolver r = getContentResolver();
        // get the file type ,in this case its mp4
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(r.getType(videouri));
    }

    private String getDeviceToken() {
        SharedPreferences sharedPreferences = getSharedPreferences("data", MODE_PRIVATE);
        return sharedPreferences.getString("DEVICE_TOKEN", null);
    }

}