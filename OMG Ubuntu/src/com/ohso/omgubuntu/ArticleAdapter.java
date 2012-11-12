package com.ohso.omgubuntu;

import java.util.Date;
import java.util.List;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ohso.omgubuntu.sqlite.Article;
import com.ohso.omgubuntu.sqlite.Articles;
import com.ohso.util.ImageHandler;
import com.ohso.util.ViewTagger;

public class ArticleAdapter extends ArrayAdapter<Article> {
    private Articles              data;
    private LayoutInflater mInflater;
    private ImageHandler imageHandler;

    public ArticleAdapter(Context context, int resource, int textViewResourceId, List<Article> objects) {
        super(context, resource, textViewResourceId, objects);
        mInflater = LayoutInflater.from(context);
        data = (Articles) objects;
    }

    public void setImageHandler(ImageHandler imageHandler) { this.imageHandler = imageHandler; }

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

            /*
             * The following uses a workaround found here http://code.google.com/p/android/issues/detail?id=18273
             * setTag used a static WeakHashMap in Android < ICS, which led to memory leaks.
             * SparseArray is used in ViewTagger, as it is in Android > ICS
             */
            ViewTagger.setTag(convertView, holder);
        } else {
            holder = (ViewHolder) ViewTagger.getTag(convertView);
        }

        Article article = new Article();
        article = data.get(position);
        CharSequence date = DateUtils.getRelativeTimeSpanString(article.getDate(), new Date().getTime(), DateUtils.MINUTE_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE);

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
        imageHandler.getImage(article.getThumb(), holder.thumb, R.drawable.logo);
        return convertView;
    }

    static class ViewHolder {
        ImageView thumb;
        ImageView unread;
        ImageView starred;
        TextView title;
        TextView author;
        TextView time;
    }
}
