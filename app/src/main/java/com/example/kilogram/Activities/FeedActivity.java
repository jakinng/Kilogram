package com.example.kilogram.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.example.kilogram.Adapters.PostAdapter;
import com.example.kilogram.Models.Post;
import com.example.kilogram.R;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

public class FeedActivity extends AppCompatActivity {
    public static final String TAG = "FeedActivity";
    public static final int MAX_POSTS = 20;

    protected PostAdapter adapter;
    private SwipeRefreshLayout swipeContainer;
    private RecyclerView rvPosts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);

        displayPosts();
        setupSwipeToRefresh();
    }

    private void setupSwipeToRefresh() {
        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                queryPosts();
                swipeContainer.setRefreshing(false);
            }
        });

        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
    }

    private void displayPosts() {
        // Look up the RecyclerView
        rvPosts = (RecyclerView) findViewById(R.id.rvPosts);
        adapter = new PostAdapter(this, new ArrayList<>());
        queryPosts();

        rvPosts.setAdapter(adapter);
        rvPosts.setLayoutManager(new LinearLayoutManager(this));
    }

    private void queryPosts() {
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        query.include(Post.KEY_USER);
        query.setLimit(MAX_POSTS);
        query.addDescendingOrder("createdAt");
        query.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> queriedPosts, ParseException e) {
                if (e == null) {
                    Log.d(TAG, "the description of the first post is: " + String.valueOf(queriedPosts.get(0).getDescription()));
                    adapter.clear();
                    adapter.addAll(queriedPosts);
                } else {
                    Log.d(TAG, "I can't get the posts..." + e.getMessage());
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.miCreate:
                openCreateActivity();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void openCreateActivity() {
        Intent intent = new Intent(FeedActivity.this, CreatePostActivity.class);
        startActivity(intent);
    }
}