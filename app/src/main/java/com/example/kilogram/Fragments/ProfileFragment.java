package com.example.kilogram.Fragments;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.kilogram.Adapters.PostAdapter;
import com.example.kilogram.Adapters.ProfilePostAdapter;
import com.example.kilogram.Models.Post;
import com.example.kilogram.R;
import com.example.kilogram.Utils.EndlessRecyclerViewScrollListener;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.parceler.Parcels;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
    public static final int LOAD_AT_ONCE = 5;
    public static final int PICK_PHOTO_CODE = 1046;

    public static final String KEY_PROFILE_PHOTO = "profilePhoto";
    public String photoFileName = "photo.jpg";

    protected ProfilePostAdapter adapter;

    private RecyclerView rvProfile;
    private ImageView ivProfilePicture;
    private TextView tvUsername;

    private EndlessRecyclerViewScrollListener scrollListener;

    private int limit = 0;
    private boolean loadMore = false;

    ParseUser user;

    public ProfileFragment() {
        // Required empty public constructor
    }

    public static ProfileFragment newInstance(ParseUser user) {
        ProfileFragment fragment = new ProfileFragment();
        fragment.user = user;
        return fragment;
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
        setupProfilePicture();
        setupUsername();
    }

    private void setupUsername() {
        tvUsername.setText(user.getUsername());
    }

    private void setupProfilePicture() {
        ParseFile profilePhoto = user.getParseFile(KEY_PROFILE_PHOTO);
        if (profilePhoto == null) {
            Glide.with(getContext())
                    .load(R.drawable.placeholder_profile_image)
                    .circleCrop()
                    .into(ivProfilePicture);
        } else {
            Glide.with(getContext())
                    .load(profilePhoto.getUrl())
                    .placeholder(R.drawable.placeholder_profile_image)
                    .circleCrop()
                    .into(ivProfilePicture);
        }
        ivProfilePicture.setClickable(true);
        ivProfilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create intent for picking a photo from the gallery
                Intent intent = new Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, PICK_PHOTO_CODE);
            }
        });
    }

    private void setupViews(View view) {
        rvProfile = (RecyclerView) view.findViewById(R.id.rvProfile);
        ivProfilePicture = (ImageView) view.findViewById(R.id.ivProfilePicture);
        tvUsername = (TextView) view.findViewById(R.id.tvUsername);
    }

    private void displayPosts() {
        // Look up the RecyclerView
        adapter = new ProfilePostAdapter(getContext(), new ArrayList<>());
        queryPosts();

        rvProfile.setAdapter(adapter);

        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), NUMBER_OF_COLUMNS);
        rvProfile.setLayoutManager(layoutManager);
        scrollListener = new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                loadNextDataFromApi(page);
            }
        };
        rvProfile.addOnScrollListener(scrollListener);
    }

    public void loadNextDataFromApi(int offset) {
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        query.include(Post.KEY_USER);
        query.whereEqualTo("user", user);
        query.setLimit(LOAD_AT_ONCE);
        query.addDescendingOrder("createdAt");

        query.setSkip(limit);
        query.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> queriedPosts, ParseException e) {
                if (e == null) {
                    limit += queriedPosts.size();
                    if (queriedPosts.size() != 0) {
                        loadMore = true;
                        Log.d(TAG, "the description of the first post is: " + String.valueOf(queriedPosts.get(0).getDescription()));
                        adapter.addAll(queriedPosts);
                    } else {
                        loadMore = false;
                        Log.d(TAG, "no posts!!!!");
                    }
                } else {
                    Log.d(TAG, "I can't get the posts..." + e.getMessage());
                }
            }
        });
    }

    private void queryPosts() {
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        query.include(Post.KEY_USER);
        query.whereEqualTo("user", user);
        query.setLimit(LOAD_AT_ONCE);
        query.addDescendingOrder("createdAt");
        query.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> queriedPosts, ParseException e) {
                if (e == null) {
                    if (queriedPosts.size() != 0) {
                        Log.d(TAG, "the description of the first post is: " + String.valueOf(queriedPosts.get(0).getDescription()));
                        adapter.clear();
                        adapter.addAll(queriedPosts);
                    } else {
                        Log.d(TAG, "no posts!!!!");
                    }
                } else {
                    Log.d(TAG, "I can't get the posts..." + e.getMessage());
                }
            }
        });
    }

    public Bitmap loadFromUri(Uri photoUri) {
        Bitmap image = null;
        try {
            if (Build.VERSION.SDK_INT > 27) {
                ImageDecoder.Source source = ImageDecoder.createSource(getContext().getContentResolver(), photoUri);
                image = ImageDecoder.decodeBitmap(source);
            } else {
                image = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), photoUri);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((data != null) && requestCode == PICK_PHOTO_CODE) {
            Uri photoUri = data.getData();
            Bitmap selectedImage = loadFromUri(photoUri);
            selectedImage = selectedImage.copy(Bitmap.Config.ARGB_8888, true);
            Glide.with(getContext())
                    .load(selectedImage)
                    .placeholder(R.drawable.placeholder_profile_image)
                    .circleCrop()
                    .into(ivProfilePicture);
            user.put(KEY_PROFILE_PHOTO, getParseFileFromBitmap(selectedImage));
            user.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null) {

                    } else {
                        Log.d(TAG, "Error in uploading profile picture!");
                    }
                }
            });
        }
    }

    private ParseFile getParseFileFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        Bitmap.CompressFormat format = Bitmap.CompressFormat.JPEG;
        int quality = 100;
        bitmap.compress(format, quality, stream);
        byte[] bitmapBytes = stream.toByteArray();

        ParseFile image = new ParseFile(photoFileName, bitmapBytes);
        return image;
    }

}