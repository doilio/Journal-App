package com.example.android.journalapp.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public final class JournalContract {

    private JournalContract() {
    }

    /*Setting up the URI*/

    //Content Authority
    public static final String CONTENT_AUTHORITY = "com.example.android.journalapp";

    //Base content Uri
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    //PATH_TableName
    public static final String PATH_MEMOS = "memos";

    public static final class JournalEntry implements BaseColumns {


        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MEMOS;

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MEMOS;

        //Complete Content Uri
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_MEMOS);

        public static final String TABLE_NAME = "memos";
        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_MEMO = "memo";

    }
}
