package com.example.kilogram.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.kilogram.Adapters.PostAdapter;
import com.example.kilogram.Models.Post;
import com.example.kilogram.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PostsFragment# newInstance} factory method to
 * create an instance of this fragment.
 */
public class PostsFragment extends Fragment {
    public static final String TAG = "PostsFragment";
    public static final int MAX_POSTS = 20;

    protected PostAdapter adapter;

    private SwipeRefreshLayout swipeContainer;
    private RecyclerView rvPosts;

    GoToProfileListener goToProfileListener;

    Post topPost;

    public PostsFragment() {
        // Required empty public constructor
    }

    public static PostsFragment newInstance(Post post, GoToProfileListener goToProfileListener) {
        PostsFragment fragment = new PostsFragment();
        fragment.goToProfileListener = goToProfileListener;
        Bundle bundle = new Bundle();
        bundle.putParcelable(Post.class.getSimpleName(), Parcels.wrap(post));
        fragment.setArguments(bundle);
        return fragment;
    }

    public static PostsFragment newInstance(GoToProfileListener goToProfileListener) {
        PostsFragment fragment = new PostsFragment();
        fragment.goToProfileListener = goToProfileListener;
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_posts, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Set the variable
        try {
            topPost = Parcels.unwrap(getArguments().getParcelable(Post.class.getSimpleName()));
            Log.d(TAG, "yes this actually works" + topPost.getDescription() + topPost.getImage().toString() + topPost.getUser().getUsername());
            insertPost(0);
        } catch (Exception e) {}
    }

    // Triggered soon after onCreateView()
    // View setup should occur here (view lookups and attaching view listeners)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupViews(view);
        displayPosts();
        setupSwipeToRefresh();
    }

    private void setupViews(View view) {
        swipeContainer = (SwipeRefreshLayout) view.findViewById(R.id.swipeContainer);
        rvPosts = (RecyclerView) view.findViewById(R.id.rvPosts);
    }


    private void setupSwipeToRefresh() {
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
        adapter = new PostAdapter(getContext(), new ArrayList<>(), goToProfileListener);
        queryPosts();

        rvPosts.setAdapter(adapter);
        rvPosts.setLayoutManager(new LinearLayoutManager(getContext()));
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

    public void insertPost(int position) {
        adapter.addPost(topPost, position);
        adapter.notifyItemInserted(position);
    }

    public interface GoToProfileListener {
        public void onProfileClick(ParseUser user);
    }
}