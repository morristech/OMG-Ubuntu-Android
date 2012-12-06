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

import com.ohso.omgubuntu.sqlite.Article;
import com.ohso.omgubuntu.sqlite.Articles;
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

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    public int getRealCount() {
        return super.getCount();
    }

    public int getFooterHeight() { return mFooterHeight; }

    @Override
    public int getCount() {
        switch(mColumns) {
            case 2:
                if (super.getCount() % 2 == 0) {
                    return super.getCount() + 1;
                }
                return super.getCount() + 2;
            case 3:
                //Log.i("OMG!", "Count: " + super.getCount());
                if (super.getCount() % 3 == 0) {
                    //Log.i("OMG!", "Adding 1 additional row");
                    return super.getCount() + 1;
                } else if((super.getCount() + 1) % 3 == 0) {
                    //Log.i("OMG!", "Adding 2 additional rows");
                    return super.getCount() + 2;
                }
                //Log.i("OMG!", "Adding 3 additional rows");
                return super.getCount() + 3;
            default:
                return super.getCount() + 1;
        }
    }

    protected void setColumns(int columns) { mColumns = columns; }

    protected void setFooterEnabled(boolean footerEnabled) { mFooterEnabled = footerEnabled; }

    private boolean isInRealRow(int position) {
        boolean realRow = true;
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
            //Log.i("OMG!", "Empty view for pos " + position);
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
             * The following uses a workaround found here http://code.google.com/p/android/issues/detail?id=18273
             * setTag used a static WeakHashMap in Android < ICS, which led to memory leaks.
             * SparseArray is used in our custom ViewTagger, as it is in Android > ICS
             */
            ViewTagger.setTag(convertView, holder);
        } else {
            //Log.i("OMG!", "Recycling a view for pos " + position);
            holder = (ViewHolder) ViewTagger.getTag(convertView);
        }

        if (position >= getRealCount()) {
            if(isInRealRow(position)) {
                convertView.setVisibility(ViewGroup.GONE);
                return convertView;
            } else {
                final View footerReserveLayout = new FrameLayout(getContext());
                if (mFooterHeight == -1) {
                    int heightMeasureSpec = MeasureSpec.makeMeasureSpec(LayoutParams.WRAP_CONTENT, MeasureSpec.EXACTLY);
                    mFooterView.measure(0, heightMeasureSpec);
                    mFooterHeight = mFooterView.getMeasuredHeight();
                }
                final AbsListView.LayoutParams footerReserveParams = new AbsListView.LayoutParams(LayoutParams.WRAP_CONTENT,
                        mFooterEnabled ? mFooterHeight : 0);
                footerReserveLayout.setLayoutParams(footerReserveParams);
                return footerReserveLayout;
            }
        }

        convertView.setVisibility(ViewGroup.VISIBLE);
        ((GridViewItemLayout) convertView).setPosition(position);

        Article article = getItem(position);

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

        if(mColumns > 1) {
            holder.summary.setVisibility(TextView.VISIBLE);
            holder.summary.setText(article.getSummary());
        } else {
            holder.summary.setVisibility(TextView.GONE);
            holder.summary.setText(null);
        }

        imageHandler.getImage(article.getThumb(), holder.thumb, placeholder);


        //Log.i("OMG!", "Inflating view for " + article.getTitle() + " with height " + convertView.getMeasuredHeight());
        return convertView;
    }

    public void measureItems(int columnWidth) {
        final GridViewItemLayout gridItem = (GridViewItemLayout) mInflater.inflate(R.layout.article_row, null);

        final int widthMeasureSpec = MeasureSpec.makeMeasureSpec(columnWidth, MeasureSpec.EXACTLY);
        final int heightMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        ImageView thumb = (ImageView) gridItem.findViewById(R.id.article_row_image);
        TextView title = (TextView) gridItem.findViewById(R.id.article_row_text_title);
        TextView author = (TextView) gridItem.findViewById(R.id.article_row_text_author);
        TextView summary = (TextView) gridItem.findViewById(R.id.article_row_text_summary);

        thumb.setImageBitmap(placeholder);

        for (int i = 0; i < getCount() - 1; i++) { // Iterate over dummy items as well, except the footer.
            Article item;
            if (i >= getRealCount()) { // Dummy item
                item = getItem(getRealCount() - 1);
            } else { // Real row item
                item = getItem(i);
            }
            gridItem.setPosition(i);

            title.setText(item.getTitle());
            author.setText(item.getAuthor());

            summary.setText(item.getSummary());

            gridItem.requestLayout();
            gridItem.measure(widthMeasureSpec, heightMeasureSpec);
            //Log.i("OMG!", "Height: " + gridItem.getMeasuredHeight() + " for " + item.getTitle());
        }
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
