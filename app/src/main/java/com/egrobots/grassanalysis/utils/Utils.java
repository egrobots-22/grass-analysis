package com.egrobots.grassanalysis.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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
                try {
                    int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);//Instead of "MediaStore.Images.Media.DATA" can be used "_data"
                    filePathUri = Uri.parse(cursor.getString(column_index));
                    filePath = filePathUri.getLastPathSegment().toString();
                    filePath = filePathUri.getPath();
                } catch (IllegalArgumentException e) {
                    filePath = RealPathUtil.getRealPath(context, uri);
                } finally {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
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
            try {
                Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
                if (cursor.moveToFirst()) {
                    int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);//Instead of "MediaStore.Images.Media.DATA" can be used "_data"
                    filePathUri = Uri.parse(cursor.getString(column_index));
                    String fileName = filePathUri.getLastPathSegment().replace(".mp4", "");
                    filePath = filePathUri.getPath().replace(fileName, fileName + "_converted");
                }
            } catch (IllegalArgumentException e) {
                filePath = RealPathUtil.getRealPath(context, uri);
                filePathUri = Uri.parse(filePath);
                String fileName = filePathUri.getLastPathSegment().replace(".mp4", "");
                filePath = filePath.replace(fileName, System.currentTimeMillis() + "_converted");
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


    public static File getFile(Context context, Uri uri) throws IOException {
        File destinationFilename = new File(context.getFilesDir().getPath() + File.separatorChar + queryName(context, uri));
        try (InputStream ins = context.getContentResolver().openInputStream(uri)) {
            createFileFromStream(ins, destinationFilename);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return destinationFilename;
    }

    public static void createFileFromStream(InputStream ins, File destination) {
        try (OutputStream os = new FileOutputStream(destination)) {
            byte[] buffer = new byte[4096];
            int length;
            while ((length = ins.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
            os.close();
            os.flush();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static String queryName(Context context, Uri uri) {
        Cursor returnCursor =
                context.getContentResolver().query(uri, null, null, null, null);
        assert returnCursor != null;
        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        returnCursor.moveToFirst();
        String name = returnCursor.getString(nameIndex);
        returnCursor.close();
        return name;
    }

}
