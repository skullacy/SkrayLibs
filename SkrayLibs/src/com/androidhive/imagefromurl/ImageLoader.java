package com.androidhive.imagefromurl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.skray.skraylibs.R;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.widget.ImageView;

public class ImageLoader {

	MemoryCache memoryCache = new MemoryCache();
	FileCache fileCache;
	private Map<ImageView, ImageProf> imageViews = Collections.synchronizedMap(new WeakHashMap<ImageView, ImageProf>());
	private Map<OnImageLoaderListener, ImageProf> listeners = Collections.synchronizedMap(new WeakHashMap<OnImageLoaderListener, ImageProf>());
	ExecutorService executorService;

	public ImageLoader(Context context) {
		fileCache = new FileCache(context);
		executorService = Executors.newFixedThreadPool(5);
	}

	int stub_id = R.drawable.ic_launcher;

	public void getImage(String url, OnImageLoaderListener listener) {
		listeners.put(listener, new ImageProf(url));
		Bitmap bitmap = memoryCache.get(url);
		if (bitmap != null)
			listener.onImageDownloaded(bitmap);
		else
			queuePhotoForListener(url, listener);
	}
	
	public void displayImage(String url, int loader, ImageView imageView, int round) {
		stub_id = loader;
		imageViews.put(imageView, new ImageProf(url, round));
		Bitmap bitmap = memoryCache.get(url);
		if (bitmap != null) {
			if (round > 0) bitmap = getRoundedCornerBitmap(bitmap, round);
			imageView.setImageBitmap(bitmap);
		}
		else {
			queuePhoto(url, imageView);
			imageView.setImageResource(loader);
		}
	}
	
	private Bitmap getRoundedCornerBitmap(Bitmap bitmap, int pixels) {
		Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(output);

		final int color = 0xff424242;
		final Paint paint = new Paint();
		final Rect rect = new Rect(0 - pixels, 0 - pixels, bitmap.getWidth(), bitmap.getHeight());
		final RectF rectF = new RectF(rect);
		final float roundPx = pixels;

		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(bitmap, rect, rect, paint);

		return output;
	}
	
	private void queuePhoto(String url, ImageView imageView) {
		PhotoToLoad p = new PhotoToLoad(url, imageView);
		executorService.submit(new PhotosLoader(p));
	}
	
	private void queuePhotoForListener(String url, OnImageLoaderListener listener) {
		PhotoToLoadForListener p = new PhotoToLoadForListener(url, listener);
		executorService.submit(new PhotosLoader(p));
	}

	private Bitmap getBitmap(String url) {
		File f = fileCache.getFile(url);
		
		// from SD cache
		Bitmap b = decodeFile(f);
		if (b != null) return b;

		// from web
		try {
			Bitmap bitmap = null;
			URL imageUrl = new URL(url);
			HttpURLConnection conn = (HttpURLConnection) imageUrl.openConnection();
			conn.setConnectTimeout(30000);
			conn.setReadTimeout(30000);
			conn.setInstanceFollowRedirects(true);
			InputStream is = conn.getInputStream();
			OutputStream os = new FileOutputStream(f);
			Utils.CopyStream(is, os);
			os.close();
			is.close();
			bitmap = decodeFile(f);
			return bitmap;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	// decodes image and scales it to reduce memory consumption
	private Bitmap decodeFile(File f) {
		try {
			// decode image size
			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(new FileInputStream(f), null, o);

			// Find the correct scale value. It should be the power of 2.
			final int REQUIRED_SIZE = 700;
			int width_tmp = o.outWidth, height_tmp = o.outHeight;
			int scale = 1;
			while (true) {
				if (width_tmp / 2 < REQUIRED_SIZE || height_tmp / 2 < REQUIRED_SIZE)
					break;
				width_tmp /= 2;
				height_tmp /= 2;
				scale *= 2;
			}

			// decode with inSampleSize
			BitmapFactory.Options o2 = new BitmapFactory.Options();
			o2.inSampleSize = scale;
			return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	// Task for the queue
	private class PhotoToLoad {
		public String url;
		public ImageView imageView;

		public PhotoToLoad(String u, ImageView i) {
			url = u;
			imageView = i;
		}
	}
	
	private class PhotoToLoadForListener extends PhotoToLoad {
		public OnImageLoaderListener listener;

		public PhotoToLoadForListener(String u, OnImageLoaderListener i) {
			super(u, null);
			url = u;
			listener = i;
		}
	}

	class PhotosLoader implements Runnable {
		PhotoToLoad photoToLoad;

		PhotosLoader(PhotoToLoad photoToLoad) {
			this.photoToLoad = photoToLoad;
		}
		
		@Override
		public void run() {
			if (imageViewReused(photoToLoad)) return;
			Bitmap bmp = getBitmap(photoToLoad.url);
			memoryCache.put(photoToLoad.url, bmp);
			if (imageViewReused(photoToLoad)) return;
			
			if (photoToLoad instanceof PhotoToLoadForListener) {
				((PhotoToLoadForListener) photoToLoad).listener.onImageDownloaded(bmp);
			}
			else {
				BitmapDisplayer bd = new BitmapDisplayer(bmp, photoToLoad);
				Activity a = (Activity) photoToLoad.imageView.getContext();
				a.runOnUiThread(bd);
			}
		}
	}

	boolean imageViewReused(PhotoToLoad photoToLoad) {
		if (photoToLoad.imageView == null) return false;
		ImageProf tag = imageViews.get(photoToLoad.imageView);
		if (tag == null || tag.url == null || !tag.url.equals(photoToLoad.url))
			return true;
		return false;
	}

	// Used to display bitmap in the UI thread
	class BitmapDisplayer implements Runnable {
		Bitmap bitmap;
		PhotoToLoad photoToLoad;

		public BitmapDisplayer(Bitmap b, PhotoToLoad p) {
			bitmap = b;
			photoToLoad = p;
		}

		public void run() {
			if (imageViewReused(photoToLoad)) return;
			
			if (bitmap != null) {
				ImageProf tag = imageViews.get(photoToLoad.imageView);
				if (tag.round > 0) bitmap = getRoundedCornerBitmap(bitmap, tag.round);
				photoToLoad.imageView.setImageBitmap(bitmap);
			}
			else
				photoToLoad.imageView.setImageResource(stub_id);
		}
	}

	public void clearCache() {
		memoryCache.clear();
		fileCache.clear();
	}
	
	public class ImageProf {
		public String url;
		public int round;
		public ImageProf(String url2, int round2) {
			this.url = url2;
			this.round = round2;
		}
		public ImageProf(String url2) {
			this.url = url2;
		}
	}
	
	public interface OnImageLoaderListener{
		void onImageDownloaded(Bitmap image);
	}

}