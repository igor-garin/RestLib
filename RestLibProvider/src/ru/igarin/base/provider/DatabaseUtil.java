package ru.igarin.base.provider;

import android.database.sqlite.SQLiteDatabase;

class DatabaseUtil {

    /**
     * Creates the string used by the database to create an index in a table.
     * This string can be used in the function
     * {@link SQLiteDatabase#execSQL(String)}
     * 
     * @param tableName The name of the table
     * @param columnName The name of the column to index
     * @return The index string
     */
    public static String getCreateIndexString(final String tableName, final String columnName) {
        return "create index " + tableName.toLowerCase() + '_' + columnName + " on " + tableName + " (" + columnName
                + ");";
    }
}
