package com.example.replysuggestions;

import java.util.Comparator;
import java.util.TreeSet;

public class MessageSet extends TreeSet<Message> {
    public MessageSet() {
        super(new Comparator<Message>() {
            @Override
            public int compare(Message o1, Message o2) {
                return o1.getDate().compareTo(o2.getDate());
            }
        });
    }
}
