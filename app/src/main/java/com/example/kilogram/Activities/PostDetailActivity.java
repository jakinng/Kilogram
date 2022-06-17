package com.example.kilogram.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.kilogram.Models.Post;
import com.example.kilogram.R;

import org.parceler.Parcels;

import java.util.Date;

public class PostDetailActivity extends AppCompatActivity {
    private ImageView ivPostImage;
    private TextView tvUsername;
    private TextView tvDescription;
    private TextView tvDateCreated;
    public TextView tvBarUsername;
    public ImageView ivProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);
        setupViews();
        showPost();

        androidx.appcompat.widget.Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    private void setupViews() {
        ivPostImage = (ImageView) findViewById(R.id.ivPostImage);
        tvUsername = (TextView) findViewById(R.id.tvUsername);
        tvDescription = (TextView) findViewById(R.id.tvDescription);
        tvDateCreated = (TextView) findViewById(R.id.tvDateCreated);
        tvBarUsername = (TextView) findViewById(R.id.tvBarUsername);
        ivProfile = (ImageView) findViewById(R.id.ivProfile);
    }

    private void showPost() {
        Post post = (Post) Parcels.unwrap(getIntent().getParcelableExtra(Post.class.getSimpleName()));
        Glide.with(PostDetailActivity.this)
                .load(post.getImage().getUrl())
                .into(ivPostImage);
        tvUsername.setText(post.getUser().getUsername());
        tvDescription.setText(post.getDescription());
        tvDateCreated.setText(Post.calculateTimeAgo(post.getCreatedAt()));
        tvBarUsername.setText(post.getUser().getUsername());
        if (post.getImage() != null) {
            Glide.with(PostDetailActivity.this)
                    .load(post.getImage().getUrl())
                    .placeholder(R.drawable.placeholder_profile_image)
                    .circleCrop()
                    .into(ivProfile);
        }
    }
}
