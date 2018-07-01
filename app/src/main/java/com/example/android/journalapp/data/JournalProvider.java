package com.example.android.journalapp.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.example.android.journalapp.data.JournalContract.JournalEntry;


public class JournalProvider extends ContentProvider {

    public static final int MEMOS = 100;

    public static final int MEMOS_ID = 101;


    private static final UriMatcher mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);


    static {

        mUriMatcher.addURI(JournalContract.CONTENT_AUTHORITY, JournalContract.PATH_MEMOS, MEMOS);
        mUriMatcher.addURI(JournalContract.CONTENT_AUTHORITY, JournalContract.PATH_MEMOS + "/#", MEMOS_ID);
    }

    public static final String LOG_TAG = JournalProvider.class.getSimpleName();

    /*Database helper object */
    private JournalHelper mJournalHelper;


    @Override
    public boolean onCreate() {
        mJournalHelper = new JournalHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        // Get readable database
        SQLiteDatabase database = mJournalHelper.getReadableDatabase();

        Cursor cursor;

        int match = mUriMatcher.match(uri);
        switch (match) {
            case MEMOS:

                cursor = database.query(JournalEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case MEMOS_ID:
                selection = JournalEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                cursor = database.query(JournalEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = mUriMatcher.match(uri);
        switch (match) {
            case MEMOS:
                return addMemo(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    private Uri addMemo(Uri uri, ContentValues values) {

        String title = values.getAsString(JournalEntry.COLUMN_TITLE);

        SQLiteDatabase database = mJournalHelper.getWritableDatabase();

        long id = database.insert(JournalContract.JournalEntry.TABLE_NAME, null, values);

        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert a row for" + uri);
            return null;
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return ContentUris.withAppendedId(uri, id);

    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {

        final int match = mUriMatcher.match(uri);
        switch (match) {
            case MEMOS:
                return editMemo(uri, contentValues, selection, selectionArgs);
            case MEMOS_ID:
                selection = JournalEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return editMemo(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    private int editMemo(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        if (values.containsKey(JournalEntry.COLUMN_TITLE)) {
            String title = values.getAsString(JournalContract.JournalEntry.COLUMN_TITLE);
            if (title == null) {
                throw new IllegalArgumentException("Memo requires a title");
            }
        }

        if (values.size() == 0) {
            return 0;
        }

        SQLiteDatabase database = mJournalHelper.getWritableDatabase();

        int rowsUpdated = database.update(JournalContract.JournalEntry.TABLE_NAME, values, selection, selectionArgs);
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        SQLiteDatabase database = mJournalHelper.getWritableDatabase();

        int rowsDeleted;

        final int match = mUriMatcher.match(uri);
        switch (match) {
            case MEMOS:
                rowsDeleted = database.delete(JournalContract.JournalEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case MEMOS_ID:
                selection = JournalContract.JournalEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(JournalContract.JournalEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsDeleted;
    }

    @Override
    public String getType(Uri uri) {
        final int match = mUriMatcher.match(uri);
        switch (match) {
            case MEMOS:
                return JournalContract.JournalEntry.CONTENT_LIST_TYPE;
            case MEMOS_ID:
                return JournalContract.JournalEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }
}