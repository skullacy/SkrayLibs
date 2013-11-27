package com.koushikdutta.urlimageviewhelper;

import com.skray.skraylibs.model.LruCache;

import android.graphics.Bitmap;

public class LruBitmapCache extends LruCache<String, Bitmap> {
    public LruBitmapCache(int maxSize) {
        super(maxSize);
    }

    @Override
    protected int sizeOf(String key, Bitmap value) {
        return value.getRowBytes() * value.getHeight();
    }
}
