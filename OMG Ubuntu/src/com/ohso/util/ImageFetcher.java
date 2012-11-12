package com.ohso.util;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.ImageView;

public class ImageFetcher {
    private OnImageFetched mCallback;
    public ImageFetcher() {}

    public void fetchImage(Activity activity, String url, WeakReference<ImageView> imageView) {
        try {
            mCallback = (OnImageFetched) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnImageFetched");
        }
        new ImageGetter().execute(url, imageView);
    }


    private class ImageGetter extends AsyncTask<Object, Void, Bitmap> {
        private WeakReference<ImageView> mImageView;
        @Override
        protected Bitmap doInBackground(Object... params) {
            String url = (String) params[0];
            mImageView = (WeakReference<ImageView>) params[1];
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            mCallback.onImageReceived();
            super.onPostExecute(result);
        }
    }

    private interface OnImageFetched {
        void onImageReceived();
    }
}
