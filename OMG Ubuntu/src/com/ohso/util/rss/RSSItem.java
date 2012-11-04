package com.ohso.util.rss;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class RSSItem extends HashMap<String, String> {
    private static final long serialVersionUID = 1L;
    private String title;
    private String author;
    private String link;
    private String thumb;
    private Date date;
    private List<String> categories = new ArrayList<String>();

    public RSSItem() {
        setTitle(null);
        setAuthor(null);
        setLink(null);
        setThumb(null);
        setDate(null);
    }

    public void setTitle(String title) { this.title = title; }
    public String getTitle() { return title; }

    public void setAuthor(String author) { this.author = author; }
    public String getAuthor() { return author; }

    public void setLink(String link) { this.link = link; }
    public String getLink() { return link; }

    public void setThumb(String thumb) { this.thumb = thumb; }
    public String getThumb() { return thumb; }

    /*public Bitmap getThumbBitmap() {
        //Unless thumbnail has a cache entry, fetchThumbBitmap, then cache and return bitmap (async it all)
        sqlite.getThumb(thumb); //gets from cache or d/ls and caches on the fly, then returns

    }*/

    public void addCategory(String category) { this.categories.add(category); }
    public List<String> getCategories() { return categories; }

    public void setDate(Date date) { this.date = date; }
    public Date getDate() { return date; }

}
