package com.ohso.util.rss;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.graphics.drawable.Drawable;
import android.sax.Element;
import android.sax.EndTextElementListener;
import android.sax.RootElement;
import android.text.Html;
import android.text.Html.ImageGetter;
import android.util.Xml;

import com.ohso.omgubuntu.sqlite.Article;

public class ArticleHandler extends DefaultHandler {
    private Article  item;
    private DateFormat pubDateFormatter = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss ZZZZ");

    public ArticleHandler() {
        item = new Article();
    }

    public Article parse(InputStream is) {
        RootElement root = new RootElement("rss");
        Element chanElement = root.getChild("channel");

        Element chanItem = chanElement.getChild("item");
        Element itemTitle = chanItem.getChild("title");
        Element itemAuthor = chanItem.getChild("http://purl.org/dc/elements/1.1/", "creator");
        Element itemLink = chanItem.getChild("link");
        Element itemDescription = chanItem.getChild("description");
        Element itemContent = chanItem.getChild("http://purl.org/rss/1.0/modules/content/", "encoded");
        Element itemCategory = chanItem.getChild("category");
        Element itemDate = chanItem.getChild("pubDate");

        itemTitle.setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String body) {
                item.setTitle(body);
            }
        });

        itemAuthor.setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String body) {
                item.setAuthor(body);
            }
        });

        itemLink.setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String body) {
                URI path = null;
                try {
                    path = new URI(body);
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
                item.setPath(path.getPath());
            }
        });

        itemDescription.setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String body) {
                String thumb = new Thumbnail(body).getThumbnail();
                item.setThumb(thumb);
                item.setSummary(Html.fromHtml(body).toString());
            }
        });

        itemContent.setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String body) {
                item.setContent(body);
            }
        });

        itemCategory.setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String body) {
                if(Character.isUpperCase(body.charAt(0))) {
                    item.addCategory(body);
                }
            }
        });

        itemDate.setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String body) {
                Date date = null;
                try {
                    date = pubDateFormatter.parse(body);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                item.setDate(date.getTime());
            }
        });

        try {
            Reader reader = new InputStreamReader(is);
            Xml.parse(reader, root.getContentHandler());
            //Xml.parse(is, Xml.Encoding.UTF_8, root.getContentHandler());
            return item; // :D
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // TODO toast if null; possibly incomplete response
        return null; // :(

    }
    private class Thumbnail implements ImageGetter {
        private String thumbnailSrc;

        public Thumbnail(String desc) {
            Html.fromHtml(desc, this, null);
        }

        @Override
        public Drawable getDrawable(String source) {
            thumbnailSrc = source;
            return null;
        }

        public String getThumbnail() {
            return thumbnailSrc;
        }
    }

}
