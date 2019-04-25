package com.example.replysuggestions;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public abstract class ContentQuery {

    private ContentResolver mContentResolver;

    protected ContentQuery(Context context) {
        mContentResolver = context.getContentResolver();
    }

    protected ContentResolver getContentResolver() {
        return mContentResolver;
    }

    protected Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return mContentResolver.query(uri, projection, selection, selectionArgs, sortOrder);
    }
}
