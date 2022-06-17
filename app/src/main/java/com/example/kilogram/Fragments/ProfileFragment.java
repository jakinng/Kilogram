package com.example.kilogram.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.kilogram.Adapters.PostAdapter;
import com.example.kilogram.Adapters.ProfilePostAdapter;
import com.example.kilogram.Models.Post;
import com.example.kilogram.R;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileFragment# newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment {
    public static final String TAG = "ProfileFragment";
    public static final int NUMBER_OF_COLUMNS = 3;

    protected ProfilePostAdapter adapter;

    private RecyclerView rvProfile;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupViews(view);
        displayPosts();
    }

    private void setupViews(View view) {
        rvProfile = (RecyclerView) view.findViewById(R.id.rvProfile);
    }

    private void displayPosts() {
        // Look up the RecyclerView
        adapter = new ProfilePostAdapter(getContext(), new ArrayList<>());
        queryPosts();

        rvProfile.setAdapter(adapter);
        rvProfile.setLayoutManager(new GridLayoutManager(getContext(), NUMBER_OF_COLUMNS));
    }

    private void queryPosts() {
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        query.include(Post.KEY_USER);
        query.whereEqualTo("user", ParseUser.getCurrentUser());
        query.setLimit(20);
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
}