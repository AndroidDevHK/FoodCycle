package com.nextgen.wastefoodmanagement;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;

import java.io.File;
import java.io.IOException;

import id.zelory.compressor.Compressor;


public class ImageCompressHelper {

    private Context context;

    public ImageCompressHelper(Context context) {
        this.context = context;
    }

    public Uri compressImage(Uri imageUri) {
        File originalFile = new File(imageUri.getPath());

        try {
            File compressedFile = new Compressor(context)
                    .setMaxWidth(640)
                    .setMaxHeight(480)
                    .setQuality(50)
                    .setCompressFormat(Bitmap.CompressFormat.JPEG)
                    .setDestinationDirectoryPath(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath())
                    .compressToFile(originalFile);

            return Uri.fromFile(compressedFile);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
