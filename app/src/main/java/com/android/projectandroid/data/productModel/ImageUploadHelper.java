package com.android.projectandroid.data.productModel;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class ImageUploadHelper {

    public static MultipartBody.Part createImagePart(Context context, Uri imageUri, String partName) {
        try {
            ContentResolver contentResolver = context.getContentResolver();
            InputStream inputStream = contentResolver.openInputStream(imageUri);

            // Get the actual MIME type from the URI
            String mimeType = contentResolver.getType(imageUri);

            // Fallback to image/jpeg if MIME type is null or generic
            if (mimeType == null || mimeType.equals("image/*")) {
                mimeType = "image/jpeg";
            }

            // Validate that it's an allowed image type
            if (!isValidImageType(mimeType)) {
                return null;
            }

            // Create appropriate file extension based on MIME type
            String fileName = "image_" + System.currentTimeMillis() + getFileExtension(mimeType);

            // Read bytes from InputStream
            byte[] bytes = getBytes(inputStream);
            inputStream.close();

            // Create RequestBody with specific MIME type
            RequestBody requestBody = RequestBody.create(MediaType.parse(mimeType), bytes);

            // Create MultipartBody.Part
            return MultipartBody.Part.createFormData(partName, fileName, requestBody);

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    // Helper method to validate image types
    private static boolean isValidImageType(String mimeType) {
        return mimeType.equals("image/jpeg") ||
                mimeType.equals("image/jpg") ||
                mimeType.equals("image/png") ||
                mimeType.equals("image/webp");
    }

    // Helper method to get file extension from MIME type
    private static String getFileExtension(String mimeType) {
        switch (mimeType) {
            case "image/jpeg":
            case "image/jpg":
                return ".jpg";
            case "image/png":
                return ".png";
            case "image/webp":
                return ".webp";
            default:
                return ".jpg"; // Default fallback
        }
    }

    private static byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }

        return byteBuffer.toByteArray();
    }

    public static RequestBody createPartFromString(String stringData) {
        return RequestBody.create(MediaType.parse("text/plain"), stringData);
    }
}
