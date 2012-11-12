package com.ohso.omgubuntu.sqlite;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class BaseDataSource {
    protected SQLiteDatabase database;
    protected OMGUbuntuDatabaseHelper helper;
    public BaseDataSource(Context context) {
        helper = new OMGUbuntuDatabaseHelper(context);
    }
    public void open() throws SQLException {
        database = helper.getWritableDatabase();
    }

    public void close() {
        helper.close();
    }
}
