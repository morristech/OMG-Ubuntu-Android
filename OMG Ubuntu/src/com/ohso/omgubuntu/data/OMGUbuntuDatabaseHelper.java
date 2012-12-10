package com.ohso.omgubuntu.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class OMGUbuntuDatabaseHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "omgubuntu.db";
    public static final List<? extends BaseTableObject> tables =
            Arrays.asList(new Article(), new ArticleCategory());
    public OMGUbuntuDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) { createDatabase(db); }

    // We're overriding so we can use foreign keys on every go
    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if(!db.isReadOnly()) db.execSQL("PRAGMA foreign_keys = ON;");
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.i("OMG!", "Database upgrade in progress.");
        for(BaseTableObject table : tables) {
            db.execSQL("DROP TABLE IF EXISTS " + table.title);
        }
        onCreate(db);
    }

    private void createDatabase(SQLiteDatabase db) {
        DatabaseLayout database = new DatabaseLayout();
        database.setTitle(DATABASE_NAME);
        for(BaseTableObject table : tables) {
            database.addTable(table);
        }
        database.create(db);
    }

    private class DatabaseLayout {
        String title;
        List<BaseTableObject> databaseTables = new ArrayList<BaseTableObject>();

        DatabaseLayout() {
            setTitle(null);
        }
        void create(SQLiteDatabase db) {
            Log.i("OMG!", "Creating db for " + getTitle());
            for (BaseTableObject table: databaseTables) {
                db.execSQL(table.getSQL());
            }
            for (BaseTableObject table: databaseTables) {
                if(table.getDefaultDataSQL() != null) {
                    table.setData();
                    db.execSQL(table.getDefaultDataSQL());
                }
            }
        }
        void setTitle(String title) { this.title = title; }
        String getTitle() { return title; }

        void addTable(BaseTableObject databaseTable) { databaseTables.add(databaseTable); }
    }
}
