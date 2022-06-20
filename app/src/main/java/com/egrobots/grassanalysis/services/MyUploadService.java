package com.egrobots.grassanalysis.services;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;

import com.arthenica.mobileffmpeg.Config;
import com.arthenica.mobileffmpeg.ExecuteCallback;
import com.arthenica.mobileffmpeg.FFmpeg;
import com.egrobots.grassanalysis.R;
import com.egrobots.grassanalysis.data.model.QuestionItem;
import com.egrobots.grassanalysis.datasource.locale.SharedPreferencesDataSource;
import com.egrobots.grassanalysis.datasource.remote.FirebaseDataSource;
import com.egrobots.grassanalysis.presentation.videos.VideosTabActivity;
import com.egrobots.grassanalysis.utils.Constants;
import com.egrobots.grassanalysis.utils.Utils;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;

import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_CANCEL;
import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS;

/**
 * Service to handle uploading files to Firebase Storage.
 */
public class MyUploadService extends MyBaseTaskService {

    private static final String TAG = "MyUploadService";
    private static final boolean COMPRESS_VIDEO = true;

    /**
     * Intent Actions
     **/
    public static final String ACTION_UPLOAD = "action_upload";
    public static final String UPLOAD_COMPLETED = "upload_completed";
    public static final String UPLOAD_ERROR = "upload_error";
    public static final String FILE_TYPE = "file_type";
    public static final String RECORD_TYPE = Constants.RECORD_TYPE;

    /**
     * Intent Extras
     **/
    public static final String EXTRA_FILE_URI = "extra_file_uri";
    public static final String EXTRA_AUDIO_URI = "extra_audio_uri";
    public static final String EXTRA_DOWNLOAD_URL = "extra_download_url";

    // [START declare_ref]
    private StorageReference mStorageRef;
    private FirebaseDataSource firebaseDataSource;
    private SharedPreferencesDataSource sharedPreferencesDataSource;
    // [END declare_ref]

    private static long TOTAL_UNITS = 1000;

    private CompositeDisposable disposable = new CompositeDisposable();
    private Utils utils = new Utils(this);

    @Override
    public void onCreate() {
        super.onCreate();

        // [START get_storage_ref]
        firebaseDataSource = new FirebaseDataSource(FirebaseStorage.getInstance().getReference(), FirebaseDatabase.getInstance());
        sharedPreferencesDataSource = new SharedPreferencesDataSource(getSharedPreferences(Constants.PREF_NAME, MODE_PRIVATE));
        mStorageRef = FirebaseStorage.getInstance().getReference();
        // [END get_storage_ref]
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand:" + intent + ":" + startId);
        if (ACTION_UPLOAD.equals(intent.getAction())) {
            Uri fileUri = intent.getParcelableExtra(EXTRA_FILE_URI);
            String questionAudioUri = intent.getStringExtra(EXTRA_AUDIO_URI);
            String fileType = intent.getStringExtra(FILE_TYPE);
            QuestionItem.RecordType recordType = (QuestionItem.RecordType) intent.getExtras().get(RECORD_TYPE);
            uploadFromUri(fileUri, fileType, questionAudioUri, recordType);
        }
        return START_REDELIVER_INTENT;
    }

    // [START upload_from_uri]
    private void uploadFromUri(final Uri fileUri, String fileType, String questionAudioUri, QuestionItem.RecordType recordType) {
        Log.d(TAG, "uploadFromUri:src:" + fileUri.toString());

        // [START_EXCLUDE]
        taskStarted();
        // [END_EXCLUDE]

        if (recordType == QuestionItem.RecordType.IMAGE) {
            showProgressNotification(getString(R.string.progress_uploading), 0, 0);
            uploadToFirebaseStorage(fileUri, fileType, questionAudioUri);
        } else {
            // Upload file to Firebase Storage
            if (!COMPRESS_VIDEO) {
                showProgressNotification(getString(R.string.progress_uploading), 0, 0);
                uploadToFirebaseStorage(fileUri, fileType, null);
            } else {
                showProgressNotification(getString(R.string.compressing), 0, 0);
                String input = Utils.getPathFromUri(this, fileUri);
                String output = Utils.getCompressedPath(this, fileUri);
                execFFmpegBinary(input, output);
            }
        }
    }

    private void uploadToFirebaseStorage(Uri fileUri, String fileType, String questionAudioUri) {
        firebaseDataSource.uploadVideoAsService(fileUri
                , fileType
                , questionAudioUri
                , sharedPreferencesDataSource.getDeviceToken()
                , sharedPreferencesDataSource.getUserName())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .toObservable()
                .subscribe(new Observer<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        disposable.add(d);
                    }

                    @Override
                    public void onNext(UploadTask.TaskSnapshot taskSnapshot) {
                        showProgressNotification(getString(R.string.progress_uploading),
                                taskSnapshot.getBytesTransferred(),
                                taskSnapshot.getTotalByteCount());
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "onError: " + e);
                    }

                    @Override
                    public void onComplete() {
                        // Upload succeeded
                        Log.d(TAG, "uploadFromUri: getDownloadUri success");

                        // [START_EXCLUDE]
                        broadcastUploadFinished(fileUri, fileUri);
                        showUploadFinishedNotification(fileUri, fileUri);
                        taskCompleted();
                        // [END_EXCLUDE]
                    }
                });
    }
    // [END upload_from_uri]

    /**
     * Broadcast finished upload (success or failure).
     *
     * @return true if a running receiver received the broadcast.
     */
    private boolean broadcastUploadFinished(@Nullable Uri downloadUrl, @Nullable Uri fileUri) {
        boolean success = downloadUrl != null;

        String action = success ? UPLOAD_COMPLETED : UPLOAD_ERROR;

        Intent broadcast = new Intent(action)
                .putExtra(EXTRA_DOWNLOAD_URL, downloadUrl)
                .putExtra(EXTRA_FILE_URI, fileUri);
        return LocalBroadcastManager.getInstance(getApplicationContext())
                .sendBroadcast(broadcast);
    }

    /**
     * Show a notification for a finished upload.
     */
    private void showUploadFinishedNotification(@Nullable Uri downloadUrl, @Nullable Uri fileUri) {
        // Hide the progress notification
        dismissProgressNotification();

        // Make Intent to MainActivity
        Intent intent = new Intent(this, VideosTabActivity.class)
                .putExtra(EXTRA_DOWNLOAD_URL, downloadUrl)
                .putExtra(EXTRA_FILE_URI, fileUri)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        boolean success = downloadUrl != null;
        String caption = success ? getString(R.string.upload_success) : getString(R.string.upload_failure);
        showFinishedNotification(caption, intent, success);
    }

    public static IntentFilter getIntentFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(UPLOAD_COMPLETED);
        filter.addAction(UPLOAD_ERROR);

        return filter;
    }

    private void execFFmpegBinary(String input, String output) {
        try {
            Log.i(Config.TAG, "input file path : " + input);
            Log.i(Config.TAG, "output file path : " + output);
//            String exe = "-i " + input + " -vf scale=1280:720 " + output;
//            String exe = "-i " + input + " -vcodec libx265 -crf 18 " + output;
            String exe = "-i "+ input +" -c:v libx265 -vtag hvc1 -c:a copy " + output;
            FFmpeg.executeAsync(exe, (executionId, returnCode) -> {
                if (returnCode == RETURN_CODE_SUCCESS) {
                    Uri outputUri = FileProvider.getUriForFile(getApplicationContext(),
                            getApplicationContext().getPackageName() + ".provider", new File(output));
                    uploadToFirebaseStorage(outputUri, utils.getFieType(outputUri), null);
                    showProgressNotification(getString(R.string.compressing), 10, 1000);
                } else if (returnCode == RETURN_CODE_CANCEL) {
                    Log.i(Config.TAG, "Async command execution cancelled by user.");
                } else {
                    Log.i(Config.TAG, String.format("Async command execution failed with returnCode=%d.", returnCode));
                }
            });
        } catch (Exception e) {
            // Mention to user the command is currently running
        }
    }

}
