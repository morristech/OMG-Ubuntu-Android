package com.ohso.omgubuntu;

import java.io.Serializable;

public class RSSChannel implements Serializable {
	private RSSItems items;
	private String title;
	private String link;
	private String author;
	/* TODO Capture all the things we need, including:
	 * Images
	 * Summary
	 * Body
	 * Thumbnail
	 * Date
	 * Comment stuff
	 */
	
	
	public RSSChannel() {
		setItems(null);
		setTitle(null);
		setAuthor(null);
		setLink(null);
	}
	
	public void setItems(RSSItems items) { this.items = items; }
	public RSSItems getItems() {return items; }
	
	public void setTitle(String title) { this.title = title; }
	public String getTitle() { return title; }
	
	public void setAuthor(String author) { this.author = author; }
	public String getAuthor() { return author; }
	
	public void setLink(String link) { this.link = link; }
	public String getLink() { return link; }

}
