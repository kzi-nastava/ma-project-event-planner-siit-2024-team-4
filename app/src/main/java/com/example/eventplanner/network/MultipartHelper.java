package com.example.eventplanner.network;

import android.graphics.Bitmap;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class MultipartHelper {

    public static List<MultipartBody.Part> createMultipartList(List<Bitmap> bitmaps) {
        List<MultipartBody.Part> parts = new ArrayList<>();
        for (int i = 0; i < bitmaps.size(); i++) {
            parts.add(createMultipartFromBitmap(bitmaps.get(i), "image_" + i));
        }
        return parts;
    }

    public static MultipartBody.Part createMultipartFromBitmap(Bitmap bitmap, String name) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, bos);
        byte[] bytes = bos.toByteArray();

        RequestBody reqFile = RequestBody.create(bytes, MediaType.parse("image/jpeg"));
        return MultipartBody.Part.createFormData("files", name + ".jpg", reqFile);
    }
}
