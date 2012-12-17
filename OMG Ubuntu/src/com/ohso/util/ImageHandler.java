package com.ohso.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.util.TypedValue;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.jakewharton.DiskLruCache;
import com.jakewharton.DiskLruCache.Editor;
import com.jakewharton.DiskLruCache.Snapshot;
import com.ohso.omgubuntu.OMGUbuntuApplication;

/*
 * Some bits taken from the Android BitmapFun project found here:
 * http://developer.android.com/training/displaying-bitmaps/cache-bitmap.html
 * Notably the pausing/cancelling functionality.
 */
public class ImageHandler {
    private static final boolean DEBUG_LOG = false;
    private final int MEMORY_CLASS;
    private final int MEMORY_CACHE_SIZE;
    private final int DISK_CACHE_SIZE = 1024 * 1024 * 10; // 10 MB
    private final CompressFormat IMAGE_COMPRESS_FORMAT = Bitmap.CompressFormat.JPEG;
    private final int IMAGE_COMPRESS_QUALITY = 75;
    private final int IMAGE_DIMENSIONS = 75;
    private static Resources mResources = OMGUbuntuApplication.getContext().getResources();

    // Pausing, cancelling, and locking
    private final Object mPauseWorkLock = new Object();
    private boolean mPauseWork = false;
    private boolean mExitTaskEarly = false;

    private float mPixelSize;
    private RelativeLayout.LayoutParams mPlaceholderParams;

    private DiskLruCache mDiskCache;
    private LruCache<String, Bitmap> mMemoryCache;

    public ImageHandler(Activity activity) {
        MEMORY_CLASS = ((ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();
        MEMORY_CACHE_SIZE = 1024 * 1024 * MEMORY_CLASS / 8; // Use 1/8 the available memory
        mMemoryCache = new LruCache<String, Bitmap>(MEMORY_CACHE_SIZE) {
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getRowBytes() * bitmap.getHeight();
            }
        };
        File cacheDir = new File(activity.getCacheDir(), "thumbnails");
        try {
            mDiskCache = DiskLruCache.open(cacheDir, 1, 1, DISK_CACHE_SIZE);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mPixelSize = scaledDIP(IMAGE_DIMENSIONS);
        mPlaceholderParams = new RelativeLayout.LayoutParams((int) mPixelSize, (int) mPixelSize);
        mPlaceholderParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        mPlaceholderParams.topMargin = 0;
    }

    public static int scaledDIP (int dip) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, mResources.getDisplayMetrics());
    }

    public void closeCache() {
        try {
            mDiskCache.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void getImage(String url, ImageView imageView, Bitmap placeholder) {
        if (url == null) {
            imageView.setLayoutParams(mPlaceholderParams);
            imageView.setImageBitmap(placeholder);
            return;
        }
        Bitmap bitmap = null;
        bitmap = mMemoryCache.get(getKey(url));
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
        } else if (cancelPotentialWork(getKey(url), imageView)){
            final ImageGetter imageGetter = new ImageGetter(imageView, url);
            final AsyncDrawable drawable = new AsyncDrawable(mResources, placeholder, imageGetter);
            imageView.setImageDrawable(drawable);
            imageGetter.executeOnExecutor(AsyncTask.DUAL_THREAD_EXECUTOR);
        }
    }

    private String getKey(String url) {
        try {
            String key = new URL(url).getPath().toLowerCase(Locale.ENGLISH).replaceAll("[^a-z^0-9^_]", "_");
            if (key.length() > 64) return key.substring(0, 63);
            return key;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private class ImageGetter extends AsyncTask<Void, Void, Bitmap> {
        private final WeakReference<ImageView> mImageView;
        private String mUrl;
        private final String mFullUrl;

        private ImageGetter(ImageView imageView, String url) {
            mImageView = new WeakReference<ImageView>(imageView);
            mFullUrl = url;
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            if (DEBUG_LOG) Log.d("OMG!", "Starting background work");
            Bitmap imageBitmap = null;
            mUrl = getKey(mFullUrl);
            synchronized (mPauseWorkLock) {
                while (mPauseWork && !isCancelled()) {
                    try {
                        mPauseWorkLock.wait();
                    } catch (InterruptedException e) {}
                }
            }

            // From disk
            if (!isCancelled() && getAttachedImageView() != null && !mExitTaskEarly) {
                imageBitmap = getBitmapFromDisk();
            }
            // From url
            if (imageBitmap == null && !isCancelled() && getAttachedImageView() != null && !mExitTaskEarly) {
                imageBitmap = getBitmapFromUrl();
            }
            // Put into memory cache
            if (imageBitmap != null) mMemoryCache.put(mUrl, imageBitmap);
            return imageBitmap;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            if (isCancelled() || mExitTaskEarly) {
                result = null;
            }
            final ImageView finalView = getAttachedImageView();
            if (finalView != null && result != null) {
                //finalView.setImageBitmap(result);
                final TransitionDrawable trans = new TransitionDrawable(new Drawable[] {
                        new ColorDrawable(android.R.color.transparent),
                        new BitmapDrawable(mResources, result)
                });
                finalView.setImageDrawable(trans);
                trans.startTransition(250);
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            synchronized (mPauseWorkLock) {
                mPauseWorkLock.notifyAll();
            }
        }

        private Bitmap getBitmapFromUrl() {
            InputStream is = null;
            try {
                URL url = new URL(mFullUrl);
                is = url.openConnection().getInputStream();

                final BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                Bitmap bitmap = BitmapFactory.decodeStream(is, null, options);
                options.inSampleSize = calculateInSampleSize(options, (int) mPixelSize, (int) mPixelSize);

                // reset() isn't always supported.
                if(is.markSupported()) is.reset();
                else is = url.openConnection().getInputStream();

                options.inJustDecodeBounds = false;
                bitmap = BitmapFactory.decodeStream(is, null, options);
                is.close();

                if(isCancelled() || getAttachedImageView() == null || mExitTaskEarly) {
                    return null;
                }

                try {
                    Editor editor = mDiskCache.edit(mUrl);
                    BufferedOutputStream stream = new BufferedOutputStream(editor.newOutputStream(0));
                    if(isCancelled() || getAttachedImageView() == null || mExitTaskEarly || bitmap == null) {
                        return null;
                    }
                    if (bitmap.compress(IMAGE_COMPRESS_FORMAT, IMAGE_COMPRESS_QUALITY, stream)) {
                        if (DEBUG_LOG) Log.d("OMG!", "Adding image to disk");
                        editor.commit();
                        mDiskCache.flush();
                        stream.close();
                    } else {
                        if (DEBUG_LOG) Log.d("OMG!", "ABORTING");
                        editor.abort();
                        stream.close();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (DEBUG_LOG) Log.d("OMG!", "Wrote image for "+ mUrl);
                return bitmap;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        private Bitmap getBitmapFromDisk() {
            InputStream inputStream = null;
            try {
                final Snapshot diskCachedSnapshot = mDiskCache.get(mUrl);
                if (diskCachedSnapshot != null) {
                    inputStream = diskCachedSnapshot.getInputStream(0);
                    if (inputStream != null) {
                        // START
/*                        final BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inJustDecodeBounds = true;
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, options);
                        options.inSampleSize = calculateInSampleSize(options, (int) mPixelSize, (int) mPixelSize);

                        // reset() isn't always supported.
                        if(inputStream.markSupported()) inputStream.reset();
                        else inputStream = diskCachedSnapshot.getInputStream(0);

                        options.inJustDecodeBounds = false;
                        bitmap = BitmapFactory.decodeStream(inputStream, null, options);*/

                        //END
                        final Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        diskCachedSnapshot.close();
                        return bitmap;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {}
                }
            }
            return null;
        }

        private ImageView getAttachedImageView() {
            final ImageView imageView = mImageView.get();
            final ImageGetter imageGetter = getImageGetter(imageView);

            if (this == imageGetter) {
                return imageView;
            }
            return null;
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

    public void setExitTasksEarly(boolean exit) {
        mExitTaskEarly = exit;
    }

    public void setPauseWork(boolean pause) {
        synchronized (mPauseWorkLock) {
            mPauseWork = pause;
            if (!mPauseWork) {
                if (DEBUG_LOG) Log.i("OMG!", "Resuming work");
                mPauseWorkLock.notifyAll();
            }
        }
    }

    public static void cancelWork(ImageView imageView) {
        final ImageGetter imageGetter = getImageGetter(imageView);
        if (imageGetter != null) {
            imageGetter.cancel(true);
            if (DEBUG_LOG) Log.i("OMG!", "Cancelled work!");
        }
    }

    private static ImageGetter getImageGetter(ImageView imageView) {
        if (imageView != null) {
            final Drawable drawable = imageView.getDrawable();
            if (drawable instanceof AsyncDrawable) {
                final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
                return asyncDrawable.getImageGetter();
            }
        }
        return null;
    }

    public static boolean cancelPotentialWork(String url, ImageView imageView) {
        final ImageGetter imageGetter = getImageGetter(imageView);

        if (imageGetter != null) {
            final String fullUrl = imageGetter.mFullUrl;
            if (fullUrl == null || !fullUrl.equals(url)) {
                imageGetter.cancel(true);
                if (DEBUG_LOG) Log.i("OMG!", "Cancelled potential work");
            } else {
                // Not potential, but already occurring.
                if (DEBUG_LOG) Log.i("OMG!", "Work in progress!");
                return false;
            }
        }
        return true;
    }

    private static class AsyncDrawable extends BitmapDrawable {
        private final WeakReference<ImageGetter> imageGetterReference;

        public AsyncDrawable(Resources res, Bitmap bitmap, ImageGetter imageGetter) {
            super(res, bitmap);
            imageGetterReference = new WeakReference<ImageGetter>(imageGetter);
        }

        public ImageGetter getImageGetter() {
            return imageGetterReference.get();
        }
    }

    public void flush() {
        try {
            mDiskCache.flush();
        } catch (IOException e) {}
    }

}
