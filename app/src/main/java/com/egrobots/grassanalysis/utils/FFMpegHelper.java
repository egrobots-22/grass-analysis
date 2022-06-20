package com.egrobots.grassanalysis.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.arthenica.mobileffmpeg.Config;
import com.arthenica.mobileffmpeg.FFmpeg;
import com.egrobots.grassanalysis.R;

import java.io.File;
import java.util.List;
import java.util.UUID;

import androidx.core.content.FileProvider;

import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_CANCEL;
import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS;

public class FFMpegHelper {

    private Context context;
    private FFMpegCallback ffMpegCallback;

    public FFMpegHelper(Context context, FFMpegCallback ffMpegCallback) {
        this.context = context;
        this.ffMpegCallback = ffMpegCallback;
    }

    public void compressVideo(String input, String output) {
        try {
            Log.i(Config.TAG, "input file path : " + input);
            Log.i(Config.TAG, "output file path : " + output);
//            String exe = "-i " + input + " -vf scale=1280:720 " + output;
//            String exe = "-i " + input + " -vcodec libx265 -crf 18 " + output;
            String exe = "-i "+ input +" -c:v libx265 -vtag hvc1 -c:a copy " + output;
            FFmpeg.executeAsync(exe, (executionId, returnCode) -> {
                if (returnCode == RETURN_CODE_SUCCESS) {
                    ffMpegCallback.onFFMpegExecSuccess(output);
                } else if (returnCode == RETURN_CODE_CANCEL) {
                    ffMpegCallback.onFFMpegExecCancel();
                    Log.i(Config.TAG, "Async command execution cancelled by user.");
                } else {
                    ffMpegCallback.onFFMpegExecError("unknown error");
                    Log.i(Config.TAG, String.format("Async command execution failed with returnCode=%d.", returnCode));
                }
            });
        } catch (Exception e) {
            // Mention to user the command is currently running
            ffMpegCallback.onFFMpegExecError("unknown error: " + e.getMessage());
        }
    }

    public void convertImagesWithAudioToVideo(List<Uri> imagesUris, String audioUri, String output) {
//        String output = getFilesDir().getPath() +  UUID.randomUUID().toString() + Constants.VIDEO_FILE_TYPE;
//        String output = getExternalFilesDir(null) + UUID.randomUUID().toString() + Constants.VIDEO_FILE_TYPE;

        StringBuilder imagesPathsCmd = new StringBuilder();
        imagesPathsCmd.append("-framerate 1 ");
        for (Uri imageUri : imagesUris) {
            String imagePath = "-i " + Utils.getPathFromUri(context, imageUri) + " ";
            imagesPathsCmd.append(imagePath);
        }
        String exe = imagesPathsCmd + " -i " + audioUri + " -c:v libx264 -r 30 " +output;
        Log.i("IMAGES TO VIDEO", "execFFmpegBinary: " + exe);
        try {
            FFmpeg.executeAsync(exe, (executionId, returnCode) -> {
                if (returnCode == RETURN_CODE_SUCCESS) {
//                    showRecordedVideo(Uri.parse(outputFile.getPath()));
//                    multipleImagesView.setVisibility(View.GONE);
                    ffMpegCallback.onFFMpegExecSuccess(output);
                    Log.i(Config.TAG, "Images are converted");
                } else if (returnCode == RETURN_CODE_CANCEL) {
                    ffMpegCallback.onFFMpegExecCancel();
                    Log.i(Config.TAG, "Async command execution cancelled by user.");
                } else {
                    ffMpegCallback.onFFMpegExecError("unknown error");
                    Log.i(Config.TAG, String.format("Async command execution failed with returnCode=%d.", returnCode));
                }
            });
        } catch (Exception e) {
            // Mention to user the command is currently running
            ffMpegCallback.onFFMpegExecError("unknown error: " + e.getMessage());
        }
    }


    public interface FFMpegCallback {
        void onFFMpegExecSuccess(String output);

        void onFFMpegExecError(String error);

        void onFFMpegExecCancel();
    }
}
