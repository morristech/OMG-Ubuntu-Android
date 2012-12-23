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
package com.ohso.omgubuntu.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.res.Resources;
import android.content.res.TypedArray;

import com.ohso.omgubuntu.OMGUbuntuApplication;
import com.ohso.omgubuntu.R;

public class Category {
    private String name;
    private String titleDB;
    private String path;
    private static Resources mResources = OMGUbuntuApplication.getContext().getResources();
    private static HashMap<String, String[]> categoriesByName = new HashMap<String,String[]>();
    private static HashMap<String, String[]> categoriesByTitle = new HashMap<String,String[]>();
    private static List<String[]> categories = new ArrayList<String[]>();

    public Category() {
        setName(null);
    }

    public Category(String name, String title, String path) {
        setName(name);
        setTitle(title);
        setPath(path);
    }

    /**
     * @return HashMap with name as the key and a String[] with title and path
     */
    public static HashMap<String, String[]> getCategoriesListByName() {
        if (categories.size() < 1) populateCategoriesList();
        if (categoriesByName.size() < 1) {
            for (String[] category : categories) {
                categoriesByName.put(category[0], new String[] {category[1], category[2]});
            }
        }
        return categoriesByName;
    }

    /**
     * @return String[] with title and path of the category or null if empty
     */
    public static String[] getCategoryByName(String name) {
        return getCategoriesListByName().get(name);
    }

    /**
     * @return HashMap with title as the key and a String[] with name and path
     */
    public static HashMap<String, String[]> getCategoriesListByTitle() {
        if (categories.size() < 1) populateCategoriesList();
        if (categoriesByTitle.size() < 1) {
            for (String[] category : categories) {
                categoriesByTitle.put(category[1], new String[] {category[0], category[2]});
            }
        }
        return categoriesByTitle;
    }

    /**
     * @return String[] with name and path of the category or null if empty.
     */
    public static String[] getCategoryByTitle(String title) {
        return getCategoriesListByTitle().get(title);
    }

    private static void populateCategoriesList() {
        TypedArray data = mResources.obtainTypedArray(R.array.category_list);
        for (int i = 0; i < data.length(); i++) {
            int id = data.getResourceId(i, 0);
            String[] cat = mResources.getStringArray(id);
            if (id > 0) categories.add(new String[] {mResources.getResourceEntryName(id), cat[0], cat[1]});
        }
    }

    public void setName(String name) { this.name = name; }
    public String getName() { return name; }

    public void setTitle(String title) { this.titleDB = title; }
    public String getTitle() { return titleDB; }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    public List<String[]> getCategories() {
        return categories;
    }

    public String[] getCategory(int location) {
        return categories.get(location);
    }
}
