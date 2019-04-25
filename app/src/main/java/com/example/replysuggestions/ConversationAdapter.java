package com.example.replysuggestions;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class ConversationAdapter extends ArrayAdapter<Conversation> {
    Context mContext;
    int mResourceId;
    List<Conversation> mData;

    public ConversationAdapter(Context context, int resource, List<Conversation> objects) {
        super(context, resource, objects);
        mContext = context;
        mResourceId = resource;
        mData = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ConversationHolder holder = null;

        if (row == null) {
            LayoutInflater inflater = ((Activity)mContext).getLayoutInflater();
            row = inflater.inflate(mResourceId, parent, false);

            holder = new ConversationHolder();
            holder.txtDate = (TextView)row.findViewById(R.id.item_date);
            holder.txtTime = (TextView)row.findViewById(R.id.item_time);
            holder.txtCount = (TextView)row.findViewById(R.id.item_count);
            holder.txtSnippet = (TextView)row.findViewById(R.id.item_snippet);

            row.setTag(holder);
        } else {
            holder = (ConversationHolder)row.getTag();
        }

        Conversation conversation = mData.get(position);
        holder.txtDate.setText(Conversation.DATE_FORMAT.format(conversation.getDate()));
        holder.txtTime.setText(Conversation.TIME_FORMAT.format(conversation.getDate()));
        int count = conversation.getmMessageCount();
        holder.txtCount.setText(String.valueOf(count) + ((count > 1) ? " messages" : " message"));
        holder.txtSnippet.setText(conversation.getSnippet());

        return row;
    }

    static class ConversationHolder {
        TextView txtDate;
        TextView txtTime;
        TextView txtCount;
        TextView txtSnippet;
    }
}

