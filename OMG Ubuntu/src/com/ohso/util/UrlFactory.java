/*
 * Copyright (C) 2012 Ohso Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.ohso.util;

import java.io.IOException;
import java.net.URLDecoder;

import android.content.res.Resources;

import com.ohso.omgubuntu.OMGUbuntuApplication;
import com.ohso.omgubuntu.R;
import com.ohso.omgubuntu.data.Category;

public class UrlFactory {
    private static Resources mResources = OMGUbuntuApplication.getContext().getResources();

    public static String fragmentForCategory(String name) {
        String urlFragment = null;
        urlFragment = "category/" + Category.getCategoriesListByName().get(name)[1] + "/feed";
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
