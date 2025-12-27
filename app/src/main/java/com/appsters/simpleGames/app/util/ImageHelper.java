package com.appsters.simpleGames.app.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.util.Base64;

import androidx.exifinterface.media.ExifInterface;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ImageHelper {

    private static final int MAX_IMAGE_SIZE = 5 * 1024 * 1024; // 5MB in bytes
    private static final int MAX_DIMENSION = 1024; // Max width/height in pixels
    private static final int COMPRESSION_QUALITY = 85; // JPEG quality (0-100)

    /**
     * Generate initials from a username
     * @param username The user's username
     * @return Initials (1-2 characters)
     */
    public static String getInitials(String username) {
        if (username == null || username.isEmpty()) {
            return "?";
        }

        String trimmed = username.trim();
        String[] parts = trimmed.split("\\s+");

        if (parts.length >= 2) {
            return (parts[0].substring(0, 1) + parts[1].substring(0, 1)).toUpperCase();
        } else {
            return trimmed.substring(0, Math.min(2, trimmed.length())).toUpperCase();
        }
    }

    /**
     * Create a circular avatar bitmap with initials
     * @param initials The initials to display
     * @param backgroundColor The background color (hex string)
     * @param size The size of the bitmap in pixels
     * @return A circular bitmap with initials
     */
    public static Bitmap createInitialsAvatar(String initials, String backgroundColor, int size) {
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        // Draw circle background
        Paint backgroundPaint = new Paint();
        backgroundPaint.setAntiAlias(true);
        backgroundPaint.setColor(Color.parseColor(backgroundColor));
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, backgroundPaint);

        // Draw initials text
        Paint textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(size * 0.4f);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setFakeBoldText(true);

        // Center text vertically
        Rect textBounds = new Rect();
        textPaint.getTextBounds(initials, 0, initials.length(), textBounds);
        float textHeight = textBounds.height();
        float textY = (size / 2f) + (textHeight / 2f);

        canvas.drawText(initials, size / 2f, textY, textPaint);

        return bitmap;
    }

    /**
     * Compress and convert an image URI to Base64 string
     * @param context Application context
     * @param imageUri URI of the selected image
     * @return Base64 encoded string with data URI prefix, or null if failed
     */
    public static String compressImageToBase64(Context context, Uri imageUri) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            if (inputStream == null) return null;

            // Decode image
            Bitmap originalBitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();

            if (originalBitmap == null) return null;

            // Fix orientation
            Bitmap rotatedBitmap = fixImageOrientation(context, imageUri, originalBitmap);

            // Resize if needed
            Bitmap resizedBitmap = resizeBitmap(rotatedBitmap, MAX_DIMENSION);

            // Compress to JPEG
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, COMPRESSION_QUALITY, outputStream);
            byte[] imageBytes = outputStream.toByteArray();

            // Check size
            if (imageBytes.length > MAX_IMAGE_SIZE) {
                // Try lower quality
                outputStream.reset();
                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream);
                imageBytes = outputStream.toByteArray();

                if (imageBytes.length > MAX_IMAGE_SIZE) {
                    return null; // Still too large
                }
            }

            // Convert to Base64
            String base64Image = Base64.encodeToString(imageBytes, Base64.NO_WRAP);

            // Clean up
            if (rotatedBitmap != originalBitmap) {
                originalBitmap.recycle();
            }
            resizedBitmap.recycle();
            outputStream.close();

            // Return with data URI prefix
            return "data:image/jpeg;base64," + base64Image;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Decode Base64 string to Bitmap
     * @param base64String Base64 encoded image string (with or without data URI prefix)
     * @return Decoded Bitmap, or null if failed
     */
    public static Bitmap decodeBase64ToBitmap(String base64String) {
        if (base64String == null || base64String.isEmpty()) {
            return null;
        }

        try {
            // Remove data URI prefix if present
            String cleanBase64 = base64String;
            if (base64String.startsWith("data:image")) {
                cleanBase64 = base64String.substring(base64String.indexOf(",") + 1);
            }

            byte[] decodedBytes = Base64.decode(cleanBase64, Base64.NO_WRAP);
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Resize bitmap to fit within max dimensions while maintaining aspect ratio
     */
    private static Bitmap resizeBitmap(Bitmap bitmap, int maxDimension) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        if (width <= maxDimension && height <= maxDimension) {
            return bitmap; // No resize needed
        }

        float scale;
        if (width > height) {
            scale = (float) maxDimension / width;
        } else {
            scale = (float) maxDimension / height;
        }

        int newWidth = Math.round(width * scale);
        int newHeight = Math.round(height * scale);

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
    }

    /**
     * Fix image orientation based on EXIF data
     */
    private static Bitmap fixImageOrientation(Context context, Uri imageUri, Bitmap bitmap) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            if (inputStream == null) return bitmap;

            ExifInterface exif = new ExifInterface(inputStream);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            inputStream.close();

            Matrix matrix = new Matrix();
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    matrix.postRotate(90);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    matrix.postRotate(180);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    matrix.postRotate(270);
                    break;
                default:
                    return bitmap; // No rotation needed
            }

            return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

        } catch (IOException e) {
            e.printStackTrace();
            return bitmap;
        }
    }

    /**
     * Get estimated size of Base64 image in bytes
     */
    public static long getBase64ImageSize(String base64String) {
        if (base64String == null || base64String.isEmpty()) {
            return 0;
        }

        String cleanBase64 = base64String;
        if (base64String.startsWith("data:image")) {
            cleanBase64 = base64String.substring(base64String.indexOf(",") + 1);
        }

        return (cleanBase64.length() * 3L) / 4L; // Approximate decoded size
    }
}