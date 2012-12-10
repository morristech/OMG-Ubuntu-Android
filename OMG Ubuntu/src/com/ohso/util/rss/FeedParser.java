package com.ohso.util.rss;

import java.io.IOException;
import java.io.InputStream;

import org.xmlpull.v1.XmlPullParserException;

import com.ohso.omgubuntu.data.Article;
import com.ohso.omgubuntu.data.Articles;

public class FeedParser {
    public Articles parseArticles(InputStream in) throws XmlPullParserException, IOException {
        return new ArticlesHandler().parse(in);
    }
    public Article parseArticle(InputStream in) throws XmlPullParserException, IOException {
        return new ArticleHandler().parse(in);
    }
}
