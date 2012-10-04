package com.ohso.omgubuntu;

import java.util.HashMap;

public class RSSItem extends HashMap<String, String> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String title;
	private String author;
	private String link;
	
	public RSSItem() {
		setTitle(null);
		setAuthor(null);
		setLink(null);
	}
	
	public void setTitle(String title) { this.title = title; }
	public String getTitle() { return title; }
	
	public void setAuthor(String author) { this.author = author; }
	public String getAuthor() { return author; }
	
	public void setLink(String link) { this.link = link; }
	public String getLink() { return link; }

}
