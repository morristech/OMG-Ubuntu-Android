package com.ohso.omgubuntu;

import java.io.IOException;
import java.io.InputStream;

import org.xmlpull.v1.XmlPullParserException;

public class OMGUbuntuParser {
	public RSSItems parse(InputStream in) throws XmlPullParserException, IOException {
		RSSItems articles = new RSSItems();
		//InputSource inputSource = new InputSource(in);
		RSSHandler xmlHandler = new RSSHandler();
		articles = xmlHandler.parse(in);
		return articles;
	}	
}
