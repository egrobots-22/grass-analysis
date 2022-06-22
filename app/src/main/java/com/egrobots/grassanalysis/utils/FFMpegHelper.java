package com.egrobots.grassanalysis.utils;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.util.Log;

import com.arthenica.mobileffmpeg.Config;
import com.arthenica.mobileffmpeg.FFmpeg;
import com.arthenica.mobileffmpeg.Statistics;
import com.arthenica.mobileffmpeg.StatisticsCallback;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_CANCEL;
import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS;

public class FFMpegHelper {

    public void compressVideo(String input, String output, FFMpegCallback ffMpegCallback, Context context) {
        try {
            Log.i(Config.TAG, "input file path : " + input);
            Log.i(Config.TAG, "output file path : " + output);
//            String exe = "-i " + input + " -vf scale=1280:720 " + output;
//            String exe = "-i " + input + " -vcodec libx265 -crf 18 " + output;
            String exe = "-i \""+ input +"\" -c:v libx265 -vtag hvc1 -c:a copy \"" + output + "\"";
            int videoLength = getMediaLengthInSecs(context, Uri.parse(input)) * 1000;
            Config.enableStatisticsCallback(new StatisticsCallback() {
                @Override
                public void apply(Statistics newStatistics) {
                    float time = newStatistics.getTime();
                    ffMpegCallback.onFFmMpegExecProgress((long) time, videoLength);
                }
            });
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

    public void convertImagesWithAudioToVideo(List<Uri> imagesUris, String audioUri, String output, FFMpegCallback ffMpegCallback, Context context) {
//        String output = getFilesDir().getPath() +  UUID.randomUUID().toString() + Constants.VIDEO_FILE_TYPE;
//        String output = getExternalFilesDir(null) + UUID.randomUUID().toString() + Constants.VIDEO_FILE_TYPE;
        //get audio length
        int audioLength = getMediaLengthInSecs(context, Uri.parse(audioUri));
        double imageDuration = (double) audioLength / imagesUris.size();
        imageDuration = imageDuration == 0 ? 1 : imageDuration;
        //create file with images paths
        File inputFile = new File(context.getExternalFilesDir(null), UUID.randomUUID().toString() + ".txt");
        BufferedWriter bufferedWriter = null;
        try {
            bufferedWriter = new BufferedWriter(new FileWriter(inputFile));
            int i=0;
            for (Uri imageUri : imagesUris) {
                bufferedWriter.write("file '" + Utils.getPathFromUri(context, imageUri) + "'\n");
                bufferedWriter.write("duration " + imageDuration + "\n");
//                if (i == imagesUris.size() - 1){
//                    bufferedWriter.write("file '" + Utils.getPathFromUri(context, imageUri) + "'\n");
//                }
                i++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bufferedWriter != null) {
                try {
                    bufferedWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
//        String exe = imagesPathsCmd + " -i " + audioUri + " -c:v libx264 -r 30 " + output;
        String exe = "-f concat -safe 0 -i " + inputFile.getPath() + " -i " + audioUri + " -c:v libx264 -r 30 " + output;
        Log.i("IMAGES TO VIDEO", "execFFmpegBinary: " + exe);
        try {
            FFmpeg.executeAsync(exe, (executionId, returnCode) -> {
                if (returnCode == RETURN_CODE_SUCCESS) {
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

    private int getMediaLengthInSecs(Context context, Uri fileUri) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(context, fileUri);
        String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        long timeInMilliSec = Long.parseLong(time);
        retriever.release();
        return (int) timeInMilliSec / 1000;
    }


    public interface FFMpegCallback {
        void onFFMpegExecSuccess(String output);

        void onFFMpegExecError(String error);

        void onFFMpegExecCancel();

        void onFFmMpegExecProgress(long progress, long total);
    }
}
