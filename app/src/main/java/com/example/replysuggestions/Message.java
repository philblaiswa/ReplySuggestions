package com.example.replysuggestions;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.Telephony;
import android.text.TextUtils;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Date;

public class Message implements Parcelable {
    protected Message(Parcel in) {
        mId = in.readLong();
        mMessageType = (in.readLong() == 0 ? MessageType.SMS : MessageType.MMS);
        mDate = new Date(in.readLong());
        mFromExternal = in.readByte() != 0;
        mBody = in.readString();
    }

    public static final Creator<Message> CREATOR = new Creator<Message>() {
        @Override
        public Message createFromParcel(Parcel in) {
            return new Message(in);
        }

        @Override
        public Message[] newArray(int size) {
            return new Message[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(mId);
        dest.writeInt(mMessageType==MessageType.SMS ? 0 : 1);
        dest.writeLong(mDate.getTime());
        dest.writeByte((byte) (mFromExternal ? 1 : 0));
        dest.writeString(mBody);
    }

    enum MessageType {
        SMS,
        MMS
    };

    long mId;
    MessageType mMessageType;
    Date mDate;
    boolean mFromExternal;
    String mBody;

    public Message(MessageType messageType, Cursor cursor) {
        mMessageType = messageType;
        if (mMessageType == MessageType.SMS) {
            mId = cursor.getLong(cursor.getColumnIndex(Telephony.Sms._ID));
            mDate = new Date(cursor.getLong(cursor.getColumnIndex(Telephony.Sms.DATE)));
            int type = cursor.getInt(cursor.getColumnIndex(Telephony.Sms.TYPE));
            mFromExternal = (type == Telephony.Sms.MESSAGE_TYPE_INBOX);
            mBody = cursor.getString(cursor.getColumnIndex(Telephony.Sms.BODY));
        } else {
            mId = cursor.getLong(cursor.getColumnIndex(Telephony.Mms._ID));
            mDate = new Date(cursor.getLong(cursor.getColumnIndex(Telephony.Mms.DATE)));
            int boxType = cursor.getInt(cursor.getColumnIndex(Telephony.Mms.MESSAGE_BOX));
            mFromExternal = (boxType == Telephony.Mms.MESSAGE_BOX_INBOX);
        }
    }

    public void populateBody(Cursor cursor, ContentResolver contentResolver) {
        StringBuffer buffer = new StringBuffer();
        while (cursor.moveToNext()) {
            String contentId = cursor.getString(cursor.getColumnIndex(Telephony.Mms.Part.CONTENT_TYPE));
            if (!TextUtils.isEmpty(contentId) && contentId.equalsIgnoreCase("text/plain")) {
                String text = cursor.getString(cursor.getColumnIndex(Telephony.Mms.Part.TEXT));
                if (TextUtils.isEmpty(text)) {
                    long partId = cursor.getLong(cursor.getColumnIndex(Telephony.Mms.Part._ID));
                    if (contentId.equals("text/plain")) {
                        try {
                            ByteArrayOutputStream os = retrievePartBytes(contentResolver, partId);
                            text = new String(os.toByteArray(), "UTF-8");
                        } catch (UnsupportedEncodingException e) {
                        }
                    }
                }
                if (!TextUtils.isEmpty(text)) {
                    buffer.append(text);
                }
            }
        }
        mBody = buffer.toString();
    }

    private ByteArrayOutputStream retrievePartBytes(ContentResolver contentResolver, long partId) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        Uri uri = Uri.withAppendedPath(Uri.withAppendedPath(Telephony.Mms.CONTENT_URI, "path"), String.valueOf(partId));
        try (InputStream is = contentResolver.openInputStream(uri)) {
            if (is != null) {
                byte[] buffer = new byte[1024];
                int bytesRead = 0;
                while ((bytesRead = is.read(buffer, 0, buffer.length)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return os;
    }

    public long getId() {
        return mId;
    }

    public Date getDate() {
        return mDate;
    }

    public boolean isFromExternal() {
        return mFromExternal;
    }

    public String getBody() {
        return mBody;
    }
}
