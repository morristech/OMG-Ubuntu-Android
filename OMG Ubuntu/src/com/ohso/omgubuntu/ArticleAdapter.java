package com.ohso.omgubuntu;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ArticleAdapter extends BaseAdapter {
	private Activity activity;
	//private ArrayList<HashMap<String, String>> data;
	private RSSItems data;
	private static LayoutInflater inflater = null;
	//public ImageLoader imageLoader
	
	public ArticleAdapter(Activity a, RSSItems d) {
		activity = a;
		data = d;
		// try .getLayoutInflater() ?
		inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		//imageLoader= new ImageLoader(activity.getApplicationContext());
	}

	@Override
	public int getCount() { return data.size();	}

	@Override
	public Object getItem(int position) { return position; }

	@Override
	public long getItemId(int position) { return position; }

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		if(convertView == null) {
			view = inflater.inflate(R.layout.article_row, null);
		}
		
		ImageView thumb = (ImageView)view.findViewById(R.id.article_row_image);
		TextView title = (TextView)view.findViewById(R.id.article_row_text_title);
		TextView author = (TextView)view.findViewById(R.id.article_row_text_author);
		
		//HashMap<String, String> article = new HashMap<String, String>();
		RSSItem article = new RSSItem();
		article = data.get(position);
		
		thumb.setImageResource(R.drawable.ic_launcher);
		//title.setText(article.get(ArticlesFragmentTab.KEY_TITLE));
		//author.setText(article.get(ArticlesFragmentTab.KEY_AUTHOR));
		title.setText(article.getTitle());
		author.setText("by " + article.getAuthor());
		return view;
	}
}
