package com.appsters.unlimitedgames.app.util;

import android.widget.ImageView;
import androidx.databinding.BindingAdapter;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.appsters.unlimitedgames.R;

public class ImageBindingAdapters {

    // Loads an image from a URL or resource ID.
    // If isFaceUp is false, it forces the card back image.
    @BindingAdapter(value = { "imageUrl", "isFaceUp" }, requireAll = false)
    public static void loadImage(ImageView view, String url, Boolean isFaceUp) {
        // Default to true if not provided (e.g. for simple images)
        boolean faceUp = (isFaceUp == null) || isFaceUp;

        if (!faceUp) {
            // Load local card back
            Glide.with(view.getContext())
                    .load(R.drawable.card_back)
                    .placeholder(R.drawable.card_placeholder)
                    .into(view);
            return;
        }

        // If URL is empty or null, show placeholder
        if (url == null || url.isEmpty()) {
            Glide.with(view.getContext())
                    .load(R.drawable.card_placeholder)
                    .into(view);
            return;
        }

        // Load from URL with caching
        Glide.with(view.getContext())
                .load(url)
                .placeholder(R.drawable.card_placeholder)
                .error(R.drawable.card_placeholder) // or a specific error image
                .diskCacheStrategy(DiskCacheStrategy.ALL) // Cache generic cards
                .into(view);
    }
}
