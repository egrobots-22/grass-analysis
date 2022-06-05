package com.egrobots.grassanalysis.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
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

    public String getPathFromUri(Uri uri) {
        String filePath = "unknown";//default fileName
        Uri filePathUri = uri;
        if (uri.getScheme().toString().compareTo("content") == 0) {
            Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
            if (cursor.moveToFirst()) {
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);//Instead of "MediaStore.Images.Media.DATA" can be used "_data"
                filePathUri = Uri.parse(cursor.getString(column_index));
                filePath = filePathUri.getLastPathSegment().toString();
                filePath = filePathUri.getPath();
            }
        } else if (uri.getScheme().compareTo("file") == 0) {
            filePath = filePathUri.getLastPathSegment().toString();
            filePath = filePathUri.getPath();
        } else {
            filePath = filePath + "_" + filePathUri.getLastPathSegment();
            filePath = filePathUri.getPath();
        }
        return filePath;
    }

    public String getCompressedPath(Uri uri) {
        String filePath = "unknown";//default fileName
        Uri filePathUri = uri;
        if (uri.getScheme().toString().compareTo("content") == 0) {
            Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
            if (cursor.moveToFirst()) {
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);//Instead of "MediaStore.Images.Media.DATA" can be used "_data"
                filePathUri = Uri.parse(cursor.getString(column_index));
                String fileName = filePathUri.getLastPathSegment().replace(".mp4", "");
                filePath = filePathUri.getPath().replace(fileName, fileName + "_converted");
            }
        } else if (uri.getScheme().compareTo("file") == 0) {
            filePath = filePathUri.getLastPathSegment().toString();
            filePath = filePathUri.getPath();
        } else {
            filePath = filePath + "_" + filePathUri.getLastPathSegment();
            filePath = filePathUri.getPath();
        }
        return filePath;
    }
}
