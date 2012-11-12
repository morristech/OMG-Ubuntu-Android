package com.ohso.omgubuntu.sqlite;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

public class CategoryDataSource extends BaseDataSource {
    private Category categorySpec = new Category();
    private ArticleCategory articleCategorySpec = new ArticleCategory();
    public CategoryDataSource(Context context) {
        super(context);
    }

    public Category getCategory(String title, boolean path) {
        Category category = new Category();
        Cursor cursor = database.query("category", categorySpec.getColumnNames(), "title = '" + title + "'", null, null, null, null);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            category = cursorToCategory(cursor, path);
        } else category = null;
        cursor.close();
        return category;
    }

    public List<Category> getCategories(String articleId, boolean path) {
        List<Category> categories = new ArrayList<Category>();
        Cursor cursor = database.query("article_category", articleCategorySpec.getColumnNames(), "article_id = '" + articleId + "'", null, null, null, null);
        if (cursor.getCount() < 0) {
            cursor.close();
            return null;
        }
        cursor.moveToFirst();
        while (cursor.isAfterLast() == false) {
            categories.add(cursorToCategory(cursor, path));
        }
        cursor.close();
        return categories;
    }

    public void setCategories(String articleId, List<String> categoryTitles) {
        for(String title : categoryTitles) {
            Log.i("OMG!", "Trying to set category "+title);
            Category dbCategory = getCategory(title, false);
            if (dbCategory == null) continue;
            ContentValues values = new ContentValues();
            values.put("category_id", dbCategory.getName());
            values.put("article_id", articleId);
            if(database.insert("article_category", null, values) > 0) {
                Log.i("OMG!", "Successfully added "+ title +" for "+articleId);
            }
        }
    }
    protected Category cursorToCategory(Cursor cursor, boolean path) {
        Category category = new Category();
        category.setName(cursor.getString(0));
        category.setTitle(cursor.getString(1));
        if (path) category.setPath(cursor.getString(2));
        return category;
    }
}
