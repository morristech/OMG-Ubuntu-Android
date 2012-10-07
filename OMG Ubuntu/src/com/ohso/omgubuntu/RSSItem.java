package com.ohso.omgubuntu;

import java.util.ArrayList;
import java.util.HashMap;

public class RSSItem extends HashMap<String, String> {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private String title;
	private String author;
	private String link;
	private String thumb;
	private ArrayList<String> categories = new ArrayList<String>();

	public RSSItem() {
		setTitle(null);
		setAuthor(null);
		setLink(null);
		setThumb(null);
	}

	public void setTitle(String title) { this.title = title; }
	public String getTitle() { return title; }

	public void setAuthor(String author) { this.author = author; }
	public String getAuthor() { return author; }

	public void setLink(String link) { this.link = link; }
	public String getLink() { return link; }

	public void setThumb(String thumb) { this.thumb = thumb; }
	public String getThumb() { return thumb; }

	public void addCategory(String category) { this.categories.add(category); }
	public ArrayList<String> getCategories() { return categories; }

}
