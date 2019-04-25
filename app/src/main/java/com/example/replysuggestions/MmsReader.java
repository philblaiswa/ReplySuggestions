package com.example.replysuggestions;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Telephony;

import java.util.Iterator;
import java.util.Locale;

public class MmsReader extends ContentQuery {
    private static final String[] PROJECTION = {
            Telephony.Mms._ID,
            Telephony.Mms.DATE,
            Telephony.Mms.MESSAGE_BOX
    };

    protected MmsReader(Context context) {
        super(context);
    }

    public Messages query(long threadId, long days, long maxMessages) {
        final long startDate = System.currentTimeMillis() - (days * Constants.TIME.MILLIS_PER_DAY);
        final String selection = String.format(Locale.ENGLISH,
                "(%s = ?) AND (%s > ?) AND ((%s = ?) OR (%s = ?))",
                Telephony.Mms.THREAD_ID,
                Telephony.Mms.DATE,
                Telephony.Mms.MESSAGE_BOX,
                Telephony.Mms.MESSAGE_BOX
        );
        final String[] selectionArgs = new String[] {
                String.valueOf(threadId),
                String.valueOf(startDate / 1000),
                String.valueOf(Telephony.Mms.MESSAGE_BOX_SENT),
                String.valueOf(Telephony.Mms.MESSAGE_BOX_INBOX)
        };
        final String sortOder = String.format(Locale.ENGLISH,
                "%s DESC LIMIT %s",
                Telephony.Mms.DATE,
                String.valueOf(maxMessages));

        Cursor cursor = query(Telephony.Mms.CONTENT_URI, PROJECTION, selection, selectionArgs, sortOder);
        return new Messages(cursor);
    }

    private Cursor partsQuery(long messageId) {
        final String selection = String.format(Locale.ENGLISH,
                "%s=?",
                Telephony.Mms.Part.MSG_ID);
        final String[] selectionArgs = new String[] {
            String.valueOf(messageId)
        };
        Uri uri = Uri.withAppendedPath(Telephony.Mms.CONTENT_URI, "part");
        return query(uri, null, selection, selectionArgs, null);
    }

    public class Messages implements Iterable<Message> {
        private Cursor mCursor;

        public Messages(Cursor cursor) {
            mCursor = cursor;
        }

        public int getCount() {
            return mCursor.getCount();
        }

        @Override
        public Iterator<Message> iterator() {
            return new MessageIterator(mCursor);
        }
    }

    private class MessageIterator implements Iterator<Message> {
        private Cursor mCursor;
        private boolean mHasNext;

        public MessageIterator(Cursor cursor) {
            mCursor = cursor;
            mHasNext = cursor.moveToFirst();
        }

        @Override
        public boolean hasNext() {
            return mHasNext;
        }

        @Override
        public Message next() {
            if (mHasNext) {
                Message message = new Message(Message.MessageType.MMS, mCursor);
                message.populateBody(partsQuery(message.getId()), getContentResolver());
                mHasNext = mCursor.moveToNext();
                return message;
            }
            return null;
        }
    }
}

