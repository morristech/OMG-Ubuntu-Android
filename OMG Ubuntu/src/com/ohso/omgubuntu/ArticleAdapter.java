/*
 * Copyright (C) 2012 - 2013 Ohso Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.ohso.omgubuntu;

import java.util.Date;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.ohso.omgubuntu.data.Article;
import com.ohso.omgubuntu.data.Articles;
import com.ohso.util.ImageHandler;
import com.ohso.util.ViewTagger;

public class ArticleAdapter extends ArrayAdapter<Article> {
    private LayoutInflater mInflater;
    private ImageHandler imageHandler;
    private final Bitmap placeholder;
    private int mColumns;
    private View mFooterView;
    private int mFooterHeight = -1;
    private boolean mFooterEnabled = false;

    public ArticleAdapter(Context context, int resource, int textViewResourceId, Articles objects,
            int columns, View footerView) {
        super(context, resource, textViewResourceId, objects);
        mInflater = LayoutInflater.from(context);
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(context.getResources(), R.drawable.logo);
        options.inSampleSize = options.outHeight / 100;
        options.inJustDecodeBounds = false;
        placeholder = BitmapFactory.decodeResource(context.getResources(), R.drawable.logo, options);
        mColumns = columns;
        mFooterView = footerView;
    }

    public void setImageHandler(ImageHandler imageHandler) { this.imageHandler = imageHandler; }

    public int getRealCount() {
        return super.getCount();
    }

    public int getFooterHeight() { return mFooterHeight; }

    @Override
    public int getCount() {
        switch(mColumns) {
            case 2:
                if (super.getCount() % 2 == 0) { // Need only a footer
                    return super.getCount() + 1;
                }
                return super.getCount() + 2; // Need a dummy item and a footer
            case 3:
                if (super.getCount() % 3 == 0) { // Need only a footer
                    return super.getCount() + 1;
                } else if((super.getCount() + 1) % 3 == 0) { // Need a dummy item and a footer
                    return super.getCount() + 2;
                }
                return super.getCount() + 3; // Need two dummies + footer
            default:
                return super.getCount() + 1; // Only need the footer for one column layouts
        }
    }

    protected void setColumns(int columns) { mColumns = columns; }

    protected void setFooterEnabled(boolean footerEnabled) { mFooterEnabled = footerEnabled; }

    protected void setFooterView(TextView footerView) { mFooterView = footerView; }

    private boolean isInRealRow(int position) {
        boolean realRow = true;
        // 0-index means being divisible by column # is the footer in its own row
        switch(mColumns) {
            case 2:
                realRow = position % 2 == 0 ? false : true;
                break;
            case 3:
                realRow = position % 3 == 0 ? false : true;
                break;
            default:
                realRow = false;
        }
        return realRow;
    }

    public Articles getItems() {
        Articles articles = new Articles();
        for (int i = 0; i < super.getCount(); i++) {
            articles.add(getItem(i));
        }
        return articles;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null || convertView.getTag() == null) {
            convertView = mInflater.inflate(R.layout.article_row, null);

            holder = new ViewHolder();
            holder.unread = (ImageView) convertView.findViewById(R.id.article_row_unread_status);
            holder.starred = (ImageView) convertView.findViewById(R.id.article_row_starred_status);
            holder.thumb = (ImageView) convertView.findViewById(R.id.article_row_image);
            holder.title = (TextView) convertView.findViewById(R.id.article_row_text_title);
            holder.author = (TextView) convertView.findViewById(R.id.article_row_text_author);
            holder.time = (TextView) convertView.findViewById(R.id.article_row_text_time);
            holder.summary = (TextView) convertView.findViewById(R.id.article_row_text_summary);

            /*
             * The following uses a workaround found here: http://code.google.com/p/android/issues/detail?id=18273
             * setTag used a static WeakHashMap in Android < ICS, which led to memory leaks.
             * SparseArray is used in our custom ViewTagger, as it is in Android >= ICS
             */
            ViewTagger.setTag(convertView, holder);
        } else {
            holder = (ViewHolder) ViewTagger.getTag(convertView);
        }

        if (position >= getRealCount()) {
            if(isInRealRow(position)) {
                convertView.setVisibility(ViewGroup.GONE);
                return convertView;
            } else {
                final View footerReserveLayout = new FrameLayout(getContext());
                int heightMeasureSpec = MeasureSpec.makeMeasureSpec(LayoutParams.WRAP_CONTENT, MeasureSpec.EXACTLY);

                mFooterView.measure(0, heightMeasureSpec);
                mFooterHeight = mFooterView.getMeasuredHeight();
                final AbsListView.LayoutParams footerReserveParams =
                        new AbsListView.LayoutParams(LayoutParams.MATCH_PARENT, mFooterEnabled ? mFooterHeight : 0);
                footerReserveLayout.setLayoutParams(footerReserveParams);

                return footerReserveLayout;
            }
        }

        convertView.setVisibility(ViewGroup.VISIBLE);

        Article article = getItem(position);

        CharSequence date = DateUtils.getRelativeTimeSpanString(article.getDate(),
                new Date().getTime(), DateUtils.MINUTE_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE);

        if(article.isStarred()) {
            holder.starred.setVisibility(View.VISIBLE);
        } else {
            holder.starred.setVisibility(View.INVISIBLE);
        }

        if(article.isUnread()) {
            holder.unread.setVisibility(View.VISIBLE);
        } else {
            holder.unread.setVisibility(View.INVISIBLE);
        }
        holder.title.setText(article.getTitle());
        holder.author.setText(article.getAuthor());
        holder.time.setText(date);

        if(mColumns > 1) {
            holder.summary.setVisibility(TextView.VISIBLE);
            holder.summary.setText(article.getSummary());
        } else {
            holder.summary.setVisibility(TextView.GONE);
            holder.summary.setText(null);
        }

        imageHandler.getImage(article.getThumb(), holder.thumb, placeholder);
        return convertView;
    }

    static class ViewHolder {
        ImageView thumb;
        ImageView unread;
        ImageView starred;
        TextView title;
        TextView author;
        TextView time;
        TextView summary;
    }

}
