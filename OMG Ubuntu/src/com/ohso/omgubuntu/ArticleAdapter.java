package com.ohso.omgubuntu;

import android.app.Activity;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ohso.util.rss.RSSItem;
import com.ohso.util.rss.RSSItems;

public class ArticleAdapter extends BaseAdapter implements OnClickListener {
    private Activity              activity;
    private RSSItems              data;
    private static LayoutInflater inflater = null;

    // TODO public ImageLoader imageLoader

    public ArticleAdapter(Activity a, RSSItems d) {
        activity = a;
        data = d;
        inflater = activity.getLayoutInflater();
        // imageLoader= new ImageLoader(activity.getApplicationContext());
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (convertView == null) {
            view = inflater.inflate(R.layout.article_row, null);
            //view = inflater.inflate(R.layout.article_row, parent);
        }
        view.setOnClickListener(this);
        ImageView thumb = (ImageView) view.findViewById(R.id.article_row_image);
        TextView title = (TextView) view.findViewById(R.id.article_row_text_title);
        TextView author = (TextView) view.findViewById(R.id.article_row_text_author);
        TextView time = (TextView) view.findViewById(R.id.article_row_text_time);
        RSSItem article = new RSSItem();
        article = data.get(position);
        CharSequence date = DateUtils.getRelativeDateTimeString(activity, article.getDate().getTime(), DateUtils.MINUTE_IN_MILLIS, DateUtils.YEAR_IN_MILLIS, DateUtils.FORMAT_ABBREV_ALL);
        Log.i("OMG!", "Thumb: "+article.getThumb());
        thumb.setImageResource(R.drawable.testthumb);
        /*if(article.getThumb()) {
            thumb.setImageBitmap(article.getThumbBitmap());
        }*/
        title.setText(article.getTitle());
        author.setText("by " + article.getAuthor());
        time.setText(date);
        return view;
    }

    @Override
    public void onClick(View v) {
        Log.i("OMG!", "Got click in article list");

    }
}
