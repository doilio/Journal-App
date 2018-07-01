package com.example.android.journalapp;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.android.journalapp.data.JournalContract;
import com.example.android.journalapp.data.JournalContract.JournalEntry;

public class EditorActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final int EXISTING_LOADER = 0;

    private Uri mCurrentMemoUri;
    private EditText mTitleEditText;
    private EditText mMemoEditText;
    private boolean mMemoChanged = false;

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mMemoChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        Intent intent = getIntent();
        mCurrentMemoUri = intent.getData();

        if (mCurrentMemoUri == null) {
            setTitle("Add Memo");

            invalidateOptionsMenu();
        } else {
            setTitle("Edit Memo");
            getLoaderManager().initLoader(EXISTING_LOADER, null, this);
        }

        // Find all relevant views that we will need to read user input from
        mTitleEditText = (EditText) findViewById(R.id.title_edit_text);
        mMemoEditText = (EditText) findViewById(R.id.memo_edit_text);

        mTitleEditText.setOnTouchListener(mTouchListener);
        mMemoEditText.setOnTouchListener(mTouchListener);
    }

    private void addMemo() {
        String titleString = mTitleEditText.getText().toString().trim();
        String memoString = mMemoEditText.getText().toString().trim();

        if (mCurrentMemoUri == null && TextUtils.isEmpty(titleString) && TextUtils.isEmpty(memoString)) {
            return;
        }

        ContentValues values = new ContentValues();
        values.put(JournalEntry.COLUMN_TITLE, titleString);
        values.put(JournalContract.JournalEntry.COLUMN_MEMO, memoString);

        if (mCurrentMemoUri == null) {
            Uri newUri = getContentResolver().insert(JournalContract.JournalEntry.CONTENT_URI, values);
            if (newUri == null) {
                Toast.makeText(this, "Error When Adding Memo", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Memo saved", Toast.LENGTH_SHORT).show();
            }
        } else {
            int rowsAffected = getContentResolver().update(mCurrentMemoUri, values, null, null);

            if (rowsAffected == 0) {
                Toast.makeText(this, "Error Updating Memo", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Memo Updated", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (mCurrentMemoUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                addMemo();
                finish();
                return true;
            case R.id.action_delete:
                deleteConfirmationDialog();
                return true;
            case android.R.id.home:
                if (!mMemoChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };
                unsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (!mMemoChanged) {
            super.onBackPressed();
            return;
        }

        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                };
        unsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {
                JournalContract.JournalEntry._ID,
                JournalContract.JournalEntry.COLUMN_TITLE,
                JournalEntry.COLUMN_MEMO};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this, mCurrentMemoUri, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        if (cursor.moveToFirst()) {
            int nameColumnIndex = cursor.getColumnIndex(JournalContract.JournalEntry.COLUMN_TITLE);
            int breedColumnIndex = cursor.getColumnIndex(JournalEntry.COLUMN_MEMO);

            String name = cursor.getString(nameColumnIndex);
            String breed = cursor.getString(breedColumnIndex);

            mTitleEditText.setText(name);
            mMemoEditText.setText(breed);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mTitleEditText.setText("");
        mMemoEditText.setText("");

    }

    private void unsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("You have unsaved changes!");
        builder.setPositiveButton("Discard", discardButtonClickListener);
        builder.setNegativeButton("Keep Editing", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }

    private void deleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Delete this Memo?");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteMemo();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteMemo() {
        if (mCurrentMemoUri != null) {
            int rowsDeleted = getContentResolver().delete(mCurrentMemoUri, null, null);

            if (rowsDeleted == 0) {
                Toast.makeText(this, "Error Deleting Memo", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this,"Memo Deleted", Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }
}