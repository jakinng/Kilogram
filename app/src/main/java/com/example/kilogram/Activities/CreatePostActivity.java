package com.example.kilogram.Activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.kilogram.Adapters.PostAdapter;
import com.example.kilogram.Models.Post;
import com.example.kilogram.R;
import com.example.kilogram.Utils.BitmapScaler;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.File;
import java.util.List;

public class CreatePostActivity extends AppCompatActivity {
    public static final String TAG = "MainActivity";
    public final static int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1034;

    private EditText etDescription;
    private Button btnCaptureImage;
    private ImageView ivPostImage;
    private Button btnSubmit;
    private Button btnLogout;
    private Button btnFeed;

    private File photoFile;
    public String photoFileName = "photo.jpg";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);

        // Attach views to the corresponding instance variables
        setupViews();

        // Setup the logout button
        setupLogoutButton();

        // Setup the take photo button
        setupCaptureImageButton();

        // Setup the submit button
        setupSubmitButton();
        
        // Setup the feed button
        setupFeed();

        // Query posts
//        queryPosts();
    }

    private void setupFeed() {
        btnFeed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goFeedActivity();
            }
        });
    }

    private void goFeedActivity() {
        Intent intent = new Intent(CreatePostActivity.this, FeedActivity.class);
        startActivity(intent);
        finish();
    }


    private void setupCaptureImageButton() {
        btnCaptureImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchCamera();
            }
        });
    }

    private void launchCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        photoFile = getPhotoFileUri(photoFileName);

        Uri fileProvider = FileProvider.getUriForFile(CreatePostActivity.this, "com.codepath.fileprovider.kilogram", photoFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);

        if (intent.resolveActivity(getPackageManager()) != null) {
            // TODO: use activity launcher instead of startactivityforresult
            startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        }
    }

    // Create file reference
    private File getPhotoFileUri(String fileName) {
        // Get safe storage directory for photos
        // Use `getExternalFilesDir` on Context to access package-specific directories.
        // This way, we don't need to request external read/write runtime permissions.
        File mediaStorageDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), TAG);

        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()) {
            Log.d(TAG, "Failed to create directory");
        }

        File file = new File(mediaStorageDir.getPath() + File.separator + fileName);
        return file;
    }

    private void setupSubmitButton() {
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String postDescription = etDescription.getText().toString();
                if (postDescription.isEmpty()) {
                    Log.d(TAG, "The post description is empty...");
                    Toast.makeText(CreatePostActivity.this, "The description is empty!", Toast.LENGTH_SHORT).show();
                } else if (photoFile == null || ivPostImage.getDrawable() == null) {
                    Log.d(TAG, "There is no image on this post!");
                    Toast.makeText(CreatePostActivity.this, "There is no image attached!", Toast.LENGTH_SHORT).show();
                } else {
                    savePost(postDescription, photoFile);
                }
            }
        });
    }

    private void savePost(String description, File photoFile) {
        Post post = new Post(description, new ParseFile(photoFile), ParseUser.getCurrentUser());
        post.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    etDescription.setText(null);
                    ivPostImage.setImageResource(0);
                    Toast.makeText(CreatePostActivity.this, "Posting: " + description, Toast.LENGTH_SHORT).show();
                    Log.i(TAG, "Posted successfully: " + description);
                    goFeedActivity();
                } else {
                    Log.e(TAG, "Oh no! The post did not go through...");
                }
            }
        });
    }

    private void queryPosts() {
        // Specify which class to query
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        // Define query conditions
        query.include("user");
        query.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> posts, ParseException e) {
                if (e == null) {
                    for (Post post : posts) {
                        Log.i(TAG, "user: " + post.getUser().getUsername());
                    }
                } else {
                    Log.e(TAG, "Error in querying for posts: " + e.getMessage());
                }
            }
        });
    }

    // Set up the various views and attach them to the corresponding variables
    private void setupViews() {
        etDescription = (EditText) findViewById(R.id.etDescription);
        btnCaptureImage = (Button) findViewById(R.id.btnCaptureImage);
        ivPostImage = (ImageView) findViewById(R.id.ivPostImage);
        btnSubmit = (Button) findViewById(R.id.btnSubmit);
        btnLogout = (Button) findViewById(R.id.btnLogout);
        btnFeed = (Button) findViewById(R.id.btnFeed);
    }

    // Set up the log out button and attach an onclick listener
    private void setupLogoutButton() {
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ParseUser.logOut();
                goLoginActivity();
            }
        });
    }

    private void goLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // We have the camera photo on disk by now
                Bitmap takenImage = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
                // Resize Bitmap
                Bitmap resizedBitmap = BitmapScaler.scaleToFitWidth(takenImage, 200);
                // Load the taken image into a preview
                ivPostImage.setImageBitmap(resizedBitmap);
            } else {
                Log.d(TAG, "Picture was not taken.");
            }
        }
    }
}