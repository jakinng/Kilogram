package com.example.kilogram.Adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.kilogram.Activities.PostDetailActivity;
import com.example.kilogram.Models.Post;
import com.example.kilogram.R;
import com.parse.ParseFile;

import org.parceler.Parcels;

import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {
    public static final String TAG = "PostAdapter";

    private Context context;
    private List<Post> posts;

    public PostAdapter(Context context, List<Post> posts) {
        this.context = context;
        this.posts = posts;
    }

    @NonNull
    @Override
    public PostAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View postView = inflater.inflate(R.layout.item_post, parent, false);
        ViewHolder viewHolder = new ViewHolder(postView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull PostAdapter.ViewHolder holder, int position) {
        // Get post based on position
        Post post = posts.get(position);
        holder.bind(post);
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public ImageView ivPostImage;
        public TextView tvUsername;
        public TextView tvDescription;
        public TextView tvDateCreated;
        public TextView tvBarUsername;
        public ImageView ivProfile;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            ivPostImage = (ImageView) itemView.findViewById(R.id.ivPostImage);
            tvUsername = (TextView) itemView.findViewById(R.id.tvUsername);
            tvDescription = (TextView) itemView.findViewById(R.id.tvDescription);
            tvDateCreated = (TextView) itemView.findViewById(R.id.tvDateCreated);
            tvBarUsername = (TextView) itemView.findViewById(R.id.tvBarUsername);
            ivProfile = (ImageView) itemView.findViewById(R.id.ivProfile);

            itemView.setOnClickListener(this);
        }

        public void bind(Post post) {
            // Load the image
            ParseFile postImage = post.getImage();
            if (postImage != null) {
                Glide.with(context)
                        .load(postImage.getUrl())
                        .placeholder(R.drawable.placeholder_image)
                        .centerCrop()
                        .into(ivPostImage);
            }
            // Load the username and description
            tvUsername.setText(post.getUser().getUsername());
            tvDescription.setText(post.getDescription());
            tvDateCreated.setText(Post.calculateTimeAgo(post.getCreatedAt()));
            tvBarUsername.setText(post.getUser().getUsername());
            if (postImage != null) {
                Glide.with(context)
                        .load(postImage.getUrl())
                        .placeholder(R.drawable.placeholder_profile_image)
                        .circleCrop()
                        .into(ivProfile);
            }
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                Post post = posts.get(position);
                Intent intent = new Intent(context, PostDetailActivity.class);
                intent.putExtra(Post.class.getSimpleName(), Parcels.wrap(post));
                context.startActivity(intent);
            }
        }
    }

    public void clear() {
        posts.clear();
        notifyDataSetChanged();
    }

    public void addAll(List<Post> list) {
        posts.addAll(list);
        notifyDataSetChanged();
    }

    public void addPost(Post postReceived, int position) {
        posts.add(position, postReceived);
        Log.d(TAG, "the post was created at" + postReceived.getDescription());
        notifyItemInserted(position);
    }
}
