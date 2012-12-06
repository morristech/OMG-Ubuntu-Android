package com.ohso.util;

import java.io.IOException;
import java.net.URLDecoder;

import android.content.res.Resources;

import com.ohso.omgubuntu.OMGUbuntuApplication;
import com.ohso.omgubuntu.R;
import com.ohso.omgubuntu.sqlite.Category;
import com.ohso.omgubuntu.sqlite.CategoryDataSource;

public class UrlFactory {
    private static Resources mResources = OMGUbuntuApplication.getContext().getResources();
    public UrlFactory() {
    }

    public static String fragmentForCategory(String name) {
        String urlFragment = null;
        CategoryDataSource dataSource = new CategoryDataSource(OMGUbuntuApplication.getContext());
        dataSource.open();
        Category category = dataSource.getCategoryByName(name, true);
        dataSource.close();
        urlFragment = "category/" + category.getPath() + "/feed";
        return urlFragment;
    }

    public static String forCategoryPage(String urlFragment, int page) {
        String url = null;
        try {
            url = mResources.getString(R.string.base_url) + "/category/" + urlFragment + "/feed" +
                    URLDecoder.decode(mResources.getString(R.string.rss_paged_params), "UTF-8") + page;
        } catch (IOException e) {}
        return url;
    }

    public static String forPage(int page) {
        String url = null;
        try {
            url = mResources.getString(R.string.rss_base_url) + "feed" +
                    URLDecoder.decode(mResources.getString(R.string.rss_paged_params), "UTF-8") + page;
        } catch (IOException e) {}
        return url;
    }

    public static String forArticle(String article) {
        String url = null;
        try {
            url = mResources.getString(R.string.rss_base_url) +
                    article + mResources.getString(R.string.rss_article_suffix) +
                    URLDecoder.decode(mResources.getString(R.string.rss_article_params), "UTF-8");
        } catch (IOException e) {}
        return url;
    }

    public static String fromFragment(String fragment) {
        String url = null;
        try {
            url = mResources.getString(R.string.rss_base_url) + fragment + URLDecoder.decode(mResources.getString(R.string.rss_query_params), "UTF-8");
        } catch (IOException e) {}
        return url;
    }
}
