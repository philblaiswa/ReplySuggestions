package com.example.replysuggestions;

import android.content.Context;
import android.database.Cursor;
import android.provider.Telephony;

import android.net.Uri;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;

public class ConversationReader extends ContentQuery {

    private static final String[] PROJECTION = {
        Telephony.Threads._ID,
        Telephony.Threads.DATE,
        Telephony.Threads.MESSAGE_COUNT,
        Telephony.Threads.SNIPPET
    };

    public ConversationReader(Context context) {
        super(context);
    }

    public Conversations query(long days) {
        Uri uri = Telephony.MmsSms.CONTENT_CONVERSATIONS_URI
                .buildUpon()
                .appendQueryParameter("simple", "true")
                .build();

        final long startDate = System.currentTimeMillis() - (days * Constants.TIME.MILLIS_PER_DAY);
        final String selection = String.format(Locale.ENGLISH,
                "%s > %s AND %s > 0",
                Telephony.Threads.DATE,
                String.valueOf(startDate),
                Telephony.Threads.MESSAGE_COUNT);

        Cursor cursor = query(uri, PROJECTION, selection, null, null);
        return new Conversations(cursor);
    }

    public class Conversations implements Iterable<Conversation> {
        private Cursor mCursor;

        public Conversations(Cursor cursor) {
            mCursor = cursor;
        }

        public int getCount() {
            return mCursor.getCount();
        }

        @Override
        public Iterator<Conversation> iterator() {
            return new ConversationIterator(mCursor);
        }
    }

    private class ConversationIterator implements Iterator<Conversation> {
        private Cursor mCursor;
        private boolean mHasNext;

        public ConversationIterator(Cursor cursor) {
            mCursor = cursor;
            mHasNext = cursor.moveToFirst();
        }

        @Override
        public boolean hasNext() {
            return mHasNext;
        }

        @Override
        public Conversation next() {
            if (mHasNext) {
                Conversation conversation = new Conversation(mCursor);
                mHasNext = mCursor.moveToNext();
                return conversation;
            }
            return null;
        }
    }

}
