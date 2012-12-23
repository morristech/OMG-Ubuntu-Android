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
import java.util.List;

import android.database.SQLException;

/**
 * The Class BaseTableObject.
 *
 * @author Sam Tran <samvtran@gmail.com>
 */
public abstract class BaseTableObject {
    public String title;
    public String primaryId;
    public String primaryIdType;
    public boolean primaryIdAutoIncrement;
    protected List<String[]> defaultData = new ArrayList<String[]>();

    public List<String[]> getDefaultData() {
        return defaultData;
    }
    public void addDefaultData(String[] defaultRow) {
        defaultData.add(defaultRow);
    }

    /*
     * Override this function to generate default sql
     */
    public String getDefaultDataSQL() { return null; }

    public List<Column> columns = new ArrayList<Column>();

    /*
     * Instantiates columns. Called directly by getSQL()
     */
    public abstract void setSQL();
    public void setData() {}
    public void addColumn(Column column) { columns.add(column); }
    public List<Column> getColumns() { return columns; }
    public String[] getColumnNames() {
        List<String> columnNames = new ArrayList<String>();
        for(Column column : columns) {
            columnNames.add(column.getName());
        }
        return columnNames.toArray(new String[] {});
    }

    public String getSQL() throws SQLException {
        setSQL();
        if(columns == null) {
            throw new SQLException("Columns must be set in setSQL()");
        }
        StringBuilder sql = new StringBuilder();
        sql.append(String.format("CREATE TABLE %s (", title));
        if (primaryId != null) {
            sql.append(primaryId);
            sql.append(String.format(" %s PRIMARY KEY %s ", primaryIdType, getAutoIncrement()));
        }
        StringBuilder columnSql = new StringBuilder();
        StringBuilder fk = new StringBuilder();
        for (Column column: columns) {
            // Don't prepend a comma if there isn't a primary key declaration before it
            // i.e. Table content (, article_id...
            if(columnSql.length() != 0 || primaryId != null) {
                columnSql.append(", ");
            }
            columnSql.append(String.format("%s %s", column.getName(), column.getType()));
            if(column.hasForeignKey()) {
                fk.append(String.format(", FOREIGN KEY (%s) REFERENCES %s (%s) ON DELETE CASCADE",
                        column.getForeignKey(), column.getForeignTable(), column.getForeignColumn()));
            }
        }
        sql.append(columnSql.toString());
        sql.append(fk.toString());
        sql.append(" ); \n");
        return sql.toString();
    }

    public String getAutoIncrement() {
        return primaryIdAutoIncrement ? "AUTOINCREMENT" : "";
    }

    public class Column {
        String[] column;
        String[] foreignKey = null;

        /**
         * Instantiates a new column.
         *
         * @param name The name SQLite uses for this column
         * @param type The type of column (i.e., INTEGER, TEXT, etc.)
         */
        public Column(String name, String type) {
            column = new String[] {name, type};
        }

        /**
         * Instantiates a new column as a foreign key.
         *
         * @param name The name SQLite uses for this foreign key column
         * @param type The type of column (i.e., INTEGER, TEXT, etc.)
         * @param theirTable The table whose column is referenced
         * @param theirColumn the column being referenced for the foreign key
         */
        public Column(String name, String type, String theirTable, String theirColumn) {
            column = new String[] {name, type};
            setForeignKey(name, theirTable, theirColumn);
        }

        public String getName() { return column[0]; }
        public String getType() { return column[1]; }


        public void setForeignKey(String ours, String theirTable, String theirColumn) {
            foreignKey = new String[] {ours, theirTable, theirColumn};
        }
        public boolean hasForeignKey() { return foreignKey != null; }
        public String getForeignKey() { return foreignKey[0]; }
        public String getForeignTable() { return foreignKey[1]; }
        public String getForeignColumn() { return foreignKey[2]; }
    }
}
