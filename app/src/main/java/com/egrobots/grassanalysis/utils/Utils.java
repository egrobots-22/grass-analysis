package com.egrobots.grassanalysis.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.webkit.MimeTypeMap;

import javax.inject.Inject;

public class Utils {

    private Context context;

    public Utils(Context context) {
        this.context = context;
    }

    public String getFieType(Uri videoUri) {
        ContentResolver r = context.getContentResolver();
        // get the file type ,in this case its mp4
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(r.getType(videoUri));
    }
}
