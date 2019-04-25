package com.example.replysuggestions;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.Telephony;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.smartreply.FirebaseSmartReply;
import com.google.firebase.ml.naturallanguage.smartreply.FirebaseTextMessage;
import com.google.firebase.ml.naturallanguage.smartreply.SmartReplySuggestionResult;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

class Conversation implements Parcelable {
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MM-dd-yyyy");
    public static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:SS");

    private long mId;
    private Date mDate;
    private int mMessageCount;
    private String mSnippet;
    //private ArrayList<FirebaseTextMessage> mConversation = new ArrayList<>();


    public Conversation(Cursor cursor) {
        mId = cursor.getLong(cursor.getColumnIndex(Telephony.Threads._ID));
        mDate = new Date(cursor.getLong(cursor.getColumnIndex(Telephony.Threads.DATE)));
        mMessageCount = cursor.getInt(cursor.getColumnIndex(Telephony.Threads.MESSAGE_COUNT));
        mSnippet = cursor.getString(cursor.getColumnIndex(Telephony.Threads.SNIPPET));
    }

    public Conversation(Parcel in) {
        mId = in.readLong();
        mDate = new Date(in.readLong());
        mMessageCount = in.readInt();
        mSnippet = in.readString();
    }

    public static final Creator<Conversation> CREATOR = new Creator<Conversation>() {
        @Override
        public Conversation createFromParcel(Parcel in) {
            return new Conversation(in);
        }

        @Override
        public Conversation[] newArray(int size) {
            return new Conversation[size];
        }
    };

    public void addMessages(Collection<Message> messages) {
        /*
        for(Message message : messages) {
            String body = message.getBody();
            if (!TextUtils.isEmpty(body)) {
                FirebaseTextMessage textMessage;
                if (message.isFromExternal()) {
                    textMessage = FirebaseTextMessage.createForRemoteUser(
                            message.getBody(), message.getDate().getTime(), "other");
                } else {
                    textMessage = FirebaseTextMessage.createForLocalUser(
                            message.getBody(),message.getDate().getTime());
                }
                if (textMessage != null) {
                    mConversation.add(textMessage);
                }
            }
        }
        */
    }

    public long getId() {
        return mId;
    }

    public Date getDate() {
        return mDate;
    }

    public int getmMessageCount() {
        return mMessageCount;
    }

    public String getSnippet() {
        return mSnippet;
    }

    public void suggestReplies(
            OnSuccessListener<SmartReplySuggestionResult> successListener,
            OnFailureListener failureListener)
    {
        FirebaseSmartReply smartReply = FirebaseNaturalLanguage.getInstance().getSmartReply();
        smartReply.suggestReplies(mConversation)
                .addOnSuccessListener(successListener)
                .addOnFailureListener(failureListener);
    }

    public String toString() {
        return String.format("%s [%d] %s",
                DATE_FORMAT.format(mDate),
                mMessageCount,
                mSnippet);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(mId);
        dest.writeLong(mDate.getTime());
        dest.writeInt(mMessageCount);
        dest.writeString(mSnippet);
    }
}
