package com.pulzit.discovery.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.pulzit.discovery.R;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.tweetui.TweetViewAdapter;

import java.util.List;

/**
 * Created by gastonsanguinetti on 27/04/16.
 */
public class UserTimelineActivity extends AppCompatActivity {

    private static final String QUERY_EXTRA = "queryExtra";
    TweetViewAdapter adapter;
    ListView tweetListView;

    public static void start(Context context, String searchQuery) {
        Intent intent = new Intent(context, UserTimelineActivity.class);
        intent.putExtra(QUERY_EXTRA, searchQuery);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_timeline);

        final View emptyView = findViewById(R.id.emptyView);
        final View progressView = findViewById(R.id.twitterProgress);
        tweetListView = (ListView) findViewById(R.id.tweetsListView);

        String screenName = getIntent().getStringExtra(QUERY_EXTRA);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("@" + screenName);

        TwitterCore.getInstance().getApiClient(Twitter.getSessionManager().getActiveSession())
                .getStatusesService().userTimeline(null,
                screenName,
                10,
                null,
                null,
                null,
                null,
                null,
                null,
                new Callback<List<Tweet>>() {
                    @Override
                    public void success(Result<List<Tweet>> result) {
                        adapter = new TweetViewAdapter(UserTimelineActivity.this,
                                result.data);
                        tweetListView.setAdapter(adapter);
                        progressView.setVisibility(View.GONE);

                        if(result.data.size() > 0) {
                            tweetListView.setVisibility(View.VISIBLE);
                        } else {
                            emptyView.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void failure(TwitterException e) {
                        emptyView.setVisibility(View.VISIBLE);
                        progressView.setVisibility(View.GONE);

                        Toast.makeText(UserTimelineActivity.this, getString(R.string.error_search),
                                Toast.LENGTH_LONG).show();
                    }
                }
        );

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
}