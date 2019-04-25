package com.example.replysuggestions;

import android.content.Context;
import android.database.Cursor;
import android.provider.Telephony;

import java.util.Iterator;
import java.util.Locale;

public class SmsReader extends ContentQuery{
    private static final String[] PROJECTION = {
            Telephony.Sms._ID,
            Telephony.Sms.DATE,
            Telephony.Sms.TYPE,
            Telephony.Sms.BODY
    };


    protected SmsReader(Context context) {
        super(context);
    }

    public Messages query(long threadId, long days, long maxMessages) {
        final long startDate = System.currentTimeMillis() - (days * Constants.TIME.MILLIS_PER_DAY);
        final String selection = String.format(Locale.ENGLISH,
                "(%s = ?) AND (%s > ?) AND ((%s = ?) OR (%s = ?))",
                Telephony.Sms.THREAD_ID,
                Telephony.Sms.DATE,
                Telephony.Sms.TYPE,
                Telephony.Sms.TYPE
                );
        final String[] selectionArgs = new String[] {
                String.valueOf(threadId),
                String.valueOf(startDate),
                String.valueOf(Telephony.Sms.MESSAGE_TYPE_INBOX),
                String.valueOf(Telephony.Sms.MESSAGE_TYPE_SENT)
        };
        final String sortOder = String.format(Locale.ENGLISH,
                "%s DESC LIMIT %s",
                Telephony.Sms.DATE,
                String.valueOf(maxMessages));

        Cursor cursor = query(Telephony.Sms.CONTENT_URI, PROJECTION, selection, selectionArgs, sortOder);
        return new Messages(cursor);
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
                Message message = new Message(Message.MessageType.SMS, mCursor);
                mHasNext = mCursor.moveToNext();
                return message;
            }
            return null;
        }
    }
}
