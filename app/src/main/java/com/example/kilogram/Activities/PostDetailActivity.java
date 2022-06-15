package com.example.kilogram.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.kilogram.Models.Post;
import com.example.kilogram.R;

import org.parceler.Parcels;

public class PostDetailActivity extends AppCompatActivity {
    private ImageView ivPostImage;
    private TextView tvUsername;
    private TextView tvDescription;
    private TextView tvDateCreated;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);
        setupViews();
        showPost();
    }

    private void setupViews() {
        ivPostImage = (ImageView) findViewById(R.id.ivPostImage);
        tvUsername = (TextView) findViewById(R.id.tvUsername);
        tvDescription = (TextView) findViewById(R.id.tvDescription);
        tvDateCreated = (TextView) findViewById(R.id.tvDateCreated);
    }

    private void showPost() {
        Post post = (Post) Parcels.unwrap(getIntent().getParcelableExtra(Post.class.getSimpleName()));
        Glide.with(PostDetailActivity.this)
                .load(post.getImage().getUrl())
                .into(ivPostImage);
        tvUsername.setText(post.getUser().getUsername());
        tvDescription.setText(post.getDescription());

    }
}
