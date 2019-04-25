package com.example.replysuggestions;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.ml.naturallanguage.smartreply.SmartReplySuggestion;
import com.google.firebase.ml.naturallanguage.smartreply.SmartReplySuggestionResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    ListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseApp.initializeApp(this);

        mListView = (ListView)findViewById(R.id.list_view);
        mListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Conversation conversation = (Conversation) parent.getItemAtPosition(position);

                Intent details = new Intent(view.getContext(), ConversationDetailsActivity.class);
                details.putExtra("conversation", conversation);
                startActivity(details);

                /*
                conversation.suggestReplies(
                        new OnSuccessListener<SmartReplySuggestionResult>() {
                            @Override
                            public void onSuccess(SmartReplySuggestionResult result) {
                                showSuggestions(result);
                            }
                        },
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                showError();
                            }
                        });
                */
            }
        });

        mListView.setAdapter(new ConversationAdapter(this, R.layout.list_item, new ArrayList<Conversation>()));
        new AsyncConversationLoader().execute();
    }

    private void showSuggestions(SmartReplySuggestionResult result) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder
                .setTitle("Suggestions")
                .setPositiveButton("OK", null);

        if (result.getStatus() == SmartReplySuggestionResult.STATUS_NOT_SUPPORTED_LANGUAGE) {
            alertDialogBuilder.setMessage("Language not supported");
        } else if (result.getStatus() == SmartReplySuggestionResult.STATUS_NO_REPLY) {
            alertDialogBuilder.setMessage("No replies");
        } else if (result.getStatus() != SmartReplySuggestionResult.STATUS_SUCCESS){
            alertDialogBuilder.setMessage("Unknown error!");
        } else {
            List<SmartReplySuggestion> resultSuggestions = result.getSuggestions();
            if (resultSuggestions.size() > 0) {
                String[] suggestions = new String[resultSuggestions.size()];
                for (int i = 0; i < resultSuggestions.size(); i++) {
                    suggestions[i] = resultSuggestions.get(i).getText();
                }

                boolean[] choices = new boolean[resultSuggestions.size()];
                alertDialogBuilder.setMultiChoiceItems(suggestions, choices, new DialogInterface.OnMultiChoiceClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {

                    }
                });
            }
        }

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void showError() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder
                .setTitle("Suggestions")
                .setMessage("Failed to generate suggestions!")
                .setPositiveButton("OK", null);

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    class AsyncConversationLoader extends AsyncTask<Void, Conversation, String> {
        ArrayAdapter mAdapter;
        ProgressBar mProgressBar;
        int mProgress;
        ConversationReader.Conversations mConversations;

        @Override
        protected void onPreExecute() {
            mAdapter =(ArrayAdapter)mListView.getAdapter();

            mProgress = 0;
            mProgressBar = (ProgressBar)findViewById(R.id.progress_bar);
            mProgressBar.setIndeterminate(true);
            mProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(Void... voids) {
            if (mConversations == null) {
                ConversationReader reader = new ConversationReader(getApplicationContext());
                mConversations = reader.query(45);
            }

            for( Conversation conversation : mConversations) {
                MessageSet messages = new MessageSet();

                SmsReader smsReader = new SmsReader(getApplicationContext());
                SmsReader.Messages smsMessages = smsReader.query(conversation.getId(), 45, 10);
                for (Message sms : smsMessages) {
                    messages.add(sms);
                }

                MmsReader mmsReader = new MmsReader(getApplicationContext());
                MmsReader.Messages mmsMessages = mmsReader.query(conversation.getId(), 45, 10);
                for (Message mms : mmsMessages) {
                    messages.add(mms);
                }

                conversation.addMessages(messages);

                publishProgress(conversation);
            }
            return "All conversations were loaded successfully!";
        }

        @Override
        protected void onProgressUpdate(Conversation... conversations) {
            if (mProgressBar.isIndeterminate()) {
                mProgressBar.setIndeterminate(false);
                mProgressBar.setMax(mConversations.getCount());
            }

            mAdapter.add(conversations[0]);

            mProgress++;
            mProgressBar.setProgress(mProgress);
        }

        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();
            mProgressBar.setVisibility(View.GONE);
        }
    }
}
