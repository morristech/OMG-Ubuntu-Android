package com.ohso.omgubuntu;

import java.io.IOException;
import java.io.InputStream;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.sax.Element;
import android.sax.EndElementListener;
import android.sax.EndTextElementListener;
import android.sax.RootElement;
import android.sax.StartElementListener;
import android.util.Log;
import android.util.Xml;

public class RSSHandler extends DefaultHandler {
	//private RSSChannel channel;
	private RSSItems items;
	private RSSItem item;
	
	public RSSHandler() {
		items = new RSSItems();
	}
	
	public RSSItems parse(InputStream is) {
		RootElement root = new RootElement("rss");
		Element chanElement = root.getChild("channel");
//		Element chanTitle = chanElement.getChild("title");
//		Element chanLink = chanElement.getChild("link");
//		Element chanDescription = chanElement.getChild("description");
//		Element chanLastBuildDate = chanElement.getChild("lastBuildDate");
		
		Element chanItem = chanElement.getChild("item");
		Element itemTitle = chanItem.getChild("title");
		Element itemAuthor = chanItem.getChild("http://purl.org/dc/elements/1.1/", "creator");
		Element itemLink = chanItem.getChild("link");
		
//		chanElement.setStartElementListener(new StartElementListener() {
//			@Override
//			public void start(Attributes attributes) {
//				channel = new RSSChannel();
//			}
//		});
		
		chanItem.setStartElementListener(new StartElementListener() {
			@Override
			public void start(Attributes attributes) {
				item = new RSSItem();
			}
		});
		
		chanItem.setEndElementListener(new EndElementListener() {
			@Override
			public void end() {
				Log.i("OMG!", "Adding item! "+item.toString());
				items.add(item);
			}
		});
		
		itemTitle.setEndTextElementListener(new EndTextElementListener() {
			@Override
			public void end(String body) {
				Log.i("OMG!", "Adding: "+body.toString());
				item.setTitle(body);
			}
		});
		
		itemAuthor.setEndTextElementListener(new EndTextElementListener() {
			@Override
			public void end(String body) {
				Log.i("OMG!", "by "+body.toString());
				item.setAuthor(body);
			}
		});
		
		itemLink.setEndTextElementListener(new EndTextElementListener() {
			@Override
			public void end(String body) {
				item.setLink(body);
			}
		});
		
		try {
			Xml.parse(is, Xml.Encoding.UTF_8, root.getContentHandler());
			return items; // :D
		}
		catch (SAXException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		
		return null; // :(
		
	}

}
