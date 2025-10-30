package com.appsters.unlimitedgames.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

public class ImageHelper {

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
}