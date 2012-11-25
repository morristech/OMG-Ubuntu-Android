package com.ohso.omgubuntu.sqlite;

import java.util.ArrayList;
import java.util.List;

import android.content.res.Resources;
import android.content.res.TypedArray;

import com.ohso.omgubuntu.OMGUbuntuApplication;
import com.ohso.omgubuntu.R;

public class Category extends BaseTableObject {
    private String name;
    private String titleDB;
    private String path;
    List<String[]> categories = new ArrayList<String[]>();

    public Category() {
        setName(null);
        Resources res = OMGUbuntuApplication.getContext().getResources();
        TypedArray data = res.obtainTypedArray(R.array.category_list);
        for (int i = 0; i < data.length(); i++) {
            int id = data.getResourceId(i, 0);
            String[] cat = res.getStringArray(id);
            if (id > 0) categories.add(new String[] {res.getResourceEntryName(id), cat[0], cat[1]});
        }
    }

    public Category(String name, String title) {
        setName(name);
        setTitle(title);
        setPath(null);
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

    @Override
    public void setSQL() {
        title = "category";
        primaryId = "name";
        primaryIdType = "TEXT";
        primaryIdAutoIncrement = false;
        addColumn(new Column("title", "TEXT"));
        addColumn(new Column("path", "TEXT"));
    }

    // TODO Default categories set up
    // Alphabetised, which it should already be
    // name, [title, path]
    @Override
    public void setData() {
        for(String[] category : categories) {
            addDefaultData(category);
        }
    }

    @Override
    public String getDefaultDataSQL() {
        StringBuilder sql = new StringBuilder();
        sql.append(String.format("INSERT INTO %s ", title));
        StringBuilder rows = new StringBuilder();
        for (String[] row : defaultData) { // (col1, col2, ...n)
            if (rows.length() != 0) {
                rows.append(String.format("UNION SELECT '%s', '%s', '%s'", row[0], row[1], row[2]));
            } else {
            rows.append(String.format("SELECT '%s', '%s', '%s'", row[0], row[1], row[2]));
            }
        }
        sql.append(rows);
        sql.append(";");
        return sql.toString();
    }



}
