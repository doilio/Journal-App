package com.example.android.journalapp;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.android.journalapp.data.JournalContract;

public class MemoCursorAdapter extends CursorAdapter {


    public MemoCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView titleTextView = (TextView) view.findViewById(R.id.title_text_view);
        TextView memoTextView = (TextView) view.findViewById(R.id.memo_text_view);

        int titleColumnIndex = cursor.getColumnIndex(JournalContract.JournalEntry.COLUMN_TITLE);
        int memoColumnIndex = cursor.getColumnIndex(JournalContract.JournalEntry.COLUMN_MEMO);

        String title = cursor.getString(titleColumnIndex);
        String memo = cursor.getString(memoColumnIndex);

        titleTextView.setText(title);
        memoTextView.setText(memo);

    }
}