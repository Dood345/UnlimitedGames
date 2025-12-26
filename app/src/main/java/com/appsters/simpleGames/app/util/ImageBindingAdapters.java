package com.appsters.simpleGames.app.util;

import android.widget.ImageView;
import androidx.databinding.BindingAdapter;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.appsters.simpleGames.R;

public class ImageBindingAdapters {

    // Loads an image from a URL or resource ID.
    // If isFaceUp is false, it forces the card back image.
    @BindingAdapter(value = { "imageUrl", "isFaceUp" }, requireAll = false)
    public static void loadImage(ImageView view, String url, Boolean isFaceUp) {
        // Default to true if not provided (e.g. for simple images)
        boolean faceUp = (isFaceUp == null) || isFaceUp;

        if (!faceUp) {
            // Load local card back (User provided)
            Glide.with(view.getContext())
                    .load(R.drawable.card_back)
                    .into(view);
            return;
        }

        // If URL is empty or null, clear the view (e.g. undealt board cards)
        if (url == null || url.isEmpty()) {
            view.setImageDrawable(null);
            return;
        }

        // Load from URL with caching
        Glide.with(view.getContext())
                .load(url)
                .placeholder(R.drawable.card_placeholder)
                .error(R.drawable.card_placeholder)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(view);
    }
}
