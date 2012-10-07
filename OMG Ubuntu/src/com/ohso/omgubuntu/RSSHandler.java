package com.ohso.omgubuntu;

import java.io.IOException;
import java.io.InputStream;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.graphics.drawable.Drawable;
import android.sax.Element;
import android.sax.EndElementListener;
import android.sax.EndTextElementListener;
import android.sax.RootElement;
import android.sax.StartElementListener;
import android.text.Html;
import android.text.Html.ImageGetter;
import android.util.Xml;

public class RSSHandler extends DefaultHandler {
	private RSSItems items;
	private RSSItem item;

	public RSSHandler() {
		items = new RSSItems();
	}

	public RSSItems parse(InputStream is) {
		RootElement root = new RootElement("rss");
		Element chanElement = root.getChild("channel");

		Element chanItem = chanElement.getChild("item");
		Element itemTitle = chanItem.getChild("title");
		Element itemAuthor = chanItem.getChild("http://purl.org/dc/elements/1.1/", "creator");
		Element itemLink = chanItem.getChild("link");
		Element itemDescription = chanItem.getChild("description");
		Element itemCategory = chanItem.getChild("category");

		chanItem.setStartElementListener(new StartElementListener() {
			@Override
			public void start(Attributes attributes) {
				item = new RSSItem();
			}
		});

		chanItem.setEndElementListener(new EndElementListener() {
			@Override
			public void end() {
				items.add(item);
			}
		});

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
				item.setLink(body);
			}
		});

		itemDescription.setEndTextElementListener(new EndTextElementListener() {
			@Override
			public void end(String body) {
				String thumb = new Thumbnail(body).getThumbnail();
				item.setThumb(thumb);
			}
		});

		itemCategory.setEndTextElementListener(new EndTextElementListener() {
			@Override
			public void end(String body) {
				//TODO implement addCategory() as String[]
				item.addCategory(body);
			}
		});

		try {
			Xml.parse(is, Xml.Encoding.UTF_8, root.getContentHandler());
			return items; // :D
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null; // :(

	}

	private class Thumbnail implements ImageGetter {
		private String thumbnailSrc;
		public Thumbnail (String desc) {
			Html.fromHtml(desc, this, null);
		}
		@Override
		public Drawable getDrawable(String source) {
			//TODO Send this to the thumbnail handler
			//Probably best to wait until article is in db, then we can get thumbnails
			thumbnailSrc = source;
			return null;
		}

		public String getThumbnail() { return thumbnailSrc; }
	}

}
