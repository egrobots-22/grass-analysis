package com.egrobots.grassanalysis.utils;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import java.util.ArrayList;
import java.util.List;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;

public class OpenGalleryActivityResultCallback implements ActivityResultCallback<ActivityResult> {

    private OpenGalleryCallback openGalleryCallback;

    public OpenGalleryActivityResultCallback(OpenGalleryCallback openGalleryCallback) {
        this.openGalleryCallback = openGalleryCallback;
    }

    @Override
    public void onActivityResult(ActivityResult result) {
        if (result.getResultCode() == Activity.RESULT_OK) {
            Intent data = result.getData();
            if (data != null && data.getClipData() != null) {
                //if multiple images are selected
                ArrayList<Uri> selectedUris = new ArrayList<>();
                for (int i = 0; i < data.getClipData().getItemCount(); i++) {
                    Uri imageUri = data.getClipData().getItemAt(i).getUri();
                    selectedUris.add(imageUri);
                }
                openGalleryCallback.onMultipleImagesSelected(selectedUris);
            } else if (data.getData() != null) {
                //if single image is selected
                Uri fileUri = data.getData();
                if (fileUri != null) {
                    openGalleryCallback.onSingleImageOrVideoSelected(fileUri);
                }
            } else {
                openGalleryCallback.onError("حدث خطأ غير معروف");
            }
        }
    }

    public interface OpenGalleryCallback {

        void onSingleImageOrVideoSelected(Uri imageUri);

        void onMultipleImagesSelected(ArrayList<Uri> imagesUris);

        void onError(String error);
    }
}
