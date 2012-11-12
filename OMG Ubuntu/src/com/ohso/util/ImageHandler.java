package com.ohso.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.util.TypedValue;
import android.widget.ImageView;

import com.jakewharton.DiskLruCache;
import com.jakewharton.DiskLruCache.Editor;
import com.jakewharton.DiskLruCache.Snapshot;

public class ImageHandler {
    private final int MEMORY_CACHE_SIZE = 1024 * 1024 * 5; // 5 MB
    private final int DISK_CACHE_SIZE = 1024 * 1024 * 10; // 10 MB
    private final CompressFormat IMAGE_COMPRESS_FORMAT = Bitmap.CompressFormat.JPEG;
    private final int IMAGE_COMPRESS_QUALITY = 75;
    private final int IMAGE_DIMENSIONS = 75;
    private float pixelSize;

    private DiskLruCache diskCache;
    private LruCache<String, Bitmap> memoryCache;

    public ImageHandler(Activity activity) {
        memoryCache = new LruCache<String, Bitmap>(MEMORY_CACHE_SIZE) {
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getRowBytes() * bitmap.getHeight();
            }
        };
        File cacheDir = new File(activity.getCacheDir(), "thumbnails");
        try {
            diskCache = DiskLruCache.open(cacheDir, 1, 1, DISK_CACHE_SIZE);
        } catch (IOException e) {
            e.printStackTrace();
        }
        pixelSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, IMAGE_DIMENSIONS, activity.getResources().getDisplayMetrics());
    }

    public void closeCache() {
        try {
            diskCache.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void getImage(String url, ImageView imageView, int placeholder) {
        //imageView.setImageResource(placeholder);
        if (url == null) return;
        new ImageGetter(imageView, url, placeholder).execute();
    }

    private String getKey(String url) {
        try {
            String key = new URL(url).getPath().toLowerCase().replaceAll("[^a-z^0-9^_]", "_");
            if (key.length() > 64) return key.substring(0, 63);
            return key;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private class ImageGetter extends AsyncTask<Void, Void, Bitmap> {
        private final WeakReference<ImageView> mImageView;
        private final String mUrl;
        private final String mFullUrl;
        private final int mPlaceholderResource;
        private Bitmap imageBitmap;

        private ImageGetter(ImageView imageView, String url, int placeholder) {
            mImageView = new WeakReference<ImageView>(imageView);
            mUrl = getKey(url);
            mFullUrl = url;
            mPlaceholderResource = placeholder;
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            imageBitmap = getBitmapFromMemory();
            if(imageBitmap != null) {
                return imageBitmap;
            }
            imageBitmap = getBitmapFromDisk();
            if(imageBitmap != null) {
                return imageBitmap;
            }
            imageBitmap = getBitmapFromUrl();
            return imageBitmap;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            final ImageView finalView = mImageView.get();
            if (mImageView != null && result != null) {
                finalView.setImageBitmap(result);
            } else {
                finalView.setImageResource(mPlaceholderResource);
            }
            super.onPostExecute(result);
        }

        private Bitmap getBitmapFromUrl() {
            InputStream is = null;
            try {
                URL url = new URL(mFullUrl);
                is = url.openConnection().getInputStream();

                final BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                Bitmap bitmap = BitmapFactory.decodeStream(is, null, options);
                options.inSampleSize = calculateInSampleSize(options, (int) pixelSize, (int) pixelSize);

                if(is.markSupported()) {
                    //Log.i("OMG!","Reset supported");
                    is.reset();
                } else {
                    //Log.i("OMG!","Reset not supported");
                    is = url.openConnection().getInputStream();
                }

                options.inJustDecodeBounds = false;
                bitmap = BitmapFactory.decodeStream(is, null, options);
                is.close();

                memoryCache.put(mUrl, bitmap);
                try {
                    Editor editor = diskCache.edit(mUrl);
                    BufferedOutputStream stream = new BufferedOutputStream(editor.newOutputStream(0));
                    if (bitmap.compress(IMAGE_COMPRESS_FORMAT, IMAGE_COMPRESS_QUALITY, stream)) {
                        Log.i("OMG!", "Adding image to disk");
                        editor.commit();
                        diskCache.flush();
                        stream.close();
                    } else {
                        Log.i("OMG!", "ABORTING");
                        editor.abort();
                        stream.close();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
                Log.i("OMG!", "Wrote image for "+ mUrl);
                return bitmap;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        private Bitmap getBitmapFromDisk() {
            Snapshot diskCachedSnapshot = null;
            try {
                diskCachedSnapshot = diskCache.get(mUrl);
                if (diskCachedSnapshot != null) {
                    final InputStream inputStream = diskCachedSnapshot.getInputStream(0);
                    if (inputStream == null) return null;
                    BufferedInputStream diskCacheBitmap = new BufferedInputStream(inputStream);
                    final Bitmap bitmap = BitmapFactory.decodeStream(diskCacheBitmap);
                    inputStream.close();
                    memoryCache.put(mUrl, bitmap);
                    return bitmap;
                } else return null;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            } finally {
                if (diskCachedSnapshot != null) diskCachedSnapshot.close();
            }
        }

        private Bitmap getBitmapFromMemory() {
            return memoryCache.get(mUrl);
        }
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            if (width > height) {
                inSampleSize = Math.round((float)height / (float)reqHeight);
            } else {
                inSampleSize = Math.round((float)width / (float)reqWidth);
            }
        }
        return inSampleSize;
    }

}
