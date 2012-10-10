package com.ohso.omgubuntu;

import java.io.IOException;
import java.io.InputStream;

import org.xmlpull.v1.XmlPullParserException;

import com.ohso.util.rss.RSSHandler;
import com.ohso.util.rss.RSSItems;

public class OMGUbuntuParser {
    public RSSItems parse(InputStream in) throws XmlPullParserException, IOException {
        return new RSSHandler().parse(in);
    }
}
