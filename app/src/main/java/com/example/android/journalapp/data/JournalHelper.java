package com.example.android.journalapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.journalapp.data.JournalContract.JournalEntry;

public class JournalHelper extends SQLiteOpenHelper {

    /*Version Number of the Database file, increment this if the schema is changed*/
    private static final int DATABASE_VERSION = 1;
    /*Name of the the database file*/
    private static final String DATABASE_NAME = "journal.db";

    public JournalHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {

        String SQL_CREATE_MEMOS_TABLE = "CREATE TABLE " + JournalEntry.TABLE_NAME + "(" +
                JournalEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                JournalEntry.COLUMN_TITLE + " TEXT ," +
                JournalEntry.COLUMN_MEMO + " TEXT);";
        database.execSQL(SQL_CREATE_MEMOS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        if (newVersion == oldVersion + 1) {
            //Update the DB
        }
    }
}
