package com.example.kilogram.Fragments;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioMetadata;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.kilogram.Activities.LoginActivity;
import com.example.kilogram.Models.Post;
import com.example.kilogram.R;
import com.example.kilogram.Utils.BitmapScaler;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.Format;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ComposeFragment# newInstance} factory method to
 * create an instance of this fragment.
 */
public class ComposeFragment extends Fragment {
    public static final String TAG = "ComposeFragment";
    public static final int PICK_PHOTO_CODE = 1046;
    public final static int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1034;

    private EditText etDescription;
    private Button btnCaptureImage;
    private Button btnSelectImage;
    private ImageView ivPostImage;
    private Button btnSubmit;

    private File photoFile;
    public String photoFileName = "photo.jpg";

    OnComposeFragmentSubmitListener onComposeFragmentSubmitListener;

    public ComposeFragment() {
        // Required empty public constructor
    }

    public ComposeFragment(OnComposeFragmentSubmitListener onComposeFragmentSubmitListener) {
        this.onComposeFragmentSubmitListener = onComposeFragmentSubmitListener;
    }

    // Called when Fragment creates its View object hierarchy
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_compose, container, false);
    }

    // Triggered soon after onCreateView()
    // View setup should occur here (view lookups and attaching view listeners)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupViews(view);

        // Setup the take photo button
        setupCaptureImageButton();

        // Setup the select picture button
        setupSelectImageButton();

        // Setup the submit button
        setupSubmitButton();
    }

    // Set up the various views and attach them to the corresponding variables
    private void setupViews(View view) {
        etDescription = (EditText) view.findViewById(R.id.etDescription);
        btnCaptureImage = (Button) view.findViewById(R.id.btnCaptureImage);
        btnSelectImage = (Button) view.findViewById(R.id.btnSelectImage);
        ivPostImage = (ImageView) view.findViewById(R.id.ivPostImage);
        btnSubmit = (Button) view.findViewById(R.id.btnSubmit);
    }

    private void setupCaptureImageButton() {
        btnCaptureImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchCamera();
            }
        });
    }

    private void setupSelectImageButton() {
        btnSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create intent for picking a photo from the gallery
                Intent intent = new Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                // As long as there exists such an intent (the result is not null), it's safe to launch for result
                Log.d(TAG, String.valueOf(intent.resolveActivity(getContext().getPackageManager())));
                // TODO : there should be a if statement checking if intent.resolveActivity(getContext().getPackageManager())) is null but removing it seems to have fixed my problem so i took it out
                // Bring up gallery
                startActivityForResult(intent, PICK_PHOTO_CODE);
            }
        });
    }


    private void setupSubmitButton() {
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String postDescription = etDescription.getText().toString();
                Log.d(TAG, "drawable: " + String.valueOf(ivPostImage.getDrawable()));

                if (postDescription.isEmpty()) {
                    Log.d(TAG, "The post description is empty...");
                    Toast.makeText(getContext(), "The description is empty!", Toast.LENGTH_SHORT).show();
                } else if (photoFile != null) {
                    savePostFromFile(postDescription, photoFile);
                } else if (ivPostImage.getDrawable() != null) {
                    savePostFromFile(postDescription, ivPostImage.getDrawable());
                } else {
                    Log.d(TAG, "There is no image attached!");
                    Toast.makeText(getContext(), "There is no image attached!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void launchCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        photoFile = getPhotoFileUri(photoFileName);

        Uri fileProvider = FileProvider.getUriForFile(getContext(), "com.codepath.fileprovider.kilogram", photoFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);

        if (intent.resolveActivity(getContext().getPackageManager()) != null) {
            // TODO: use activity launcher instead of startactivityforresult
            startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        }
    }

    // Create file reference
    private File getPhotoFileUri(String fileName) {
        // Get safe storage directory for photos
        // Use `getExternalFilesDir` on Context to access package-specific directories.
        // This way, we don't need to request external read/write runtime permissions.
        File mediaStorageDir = new File(getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), TAG);

        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()) {
            Log.d(TAG, "Failed to create directory");
        }

        File file = new File(mediaStorageDir.getPath() + File.separator + fileName);
        return file;
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

    private void savePost(Post post) {
        post.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    etDescription.setText(null);
                    ivPostImage.setImageResource(0);
                    onComposeFragmentSubmitListener.onButtonClick(post);
                } else {
                    Log.e(TAG, "Oh no! The post did not go through...");
                }
            }
        });
    }

    private void savePostFromFile(String description, File photoFile) {
        Post post = new Post(description, new ParseFile(photoFile), ParseUser.getCurrentUser());
        savePost(post);
    }

    private void savePostFromFile(String description, Drawable drawable) {
        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        Bitmap.CompressFormat format = Bitmap.CompressFormat.JPEG;
        int quality = 100;
        bitmap.compress(format, quality, stream);
        byte[] bitmapBytes = stream.toByteArray();

        ParseFile image = new ParseFile(photoFileName, bitmapBytes);
        Post post = new Post(description, image, ParseUser.getCurrentUser());
        savePost(post);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
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

        if ((data != null) && requestCode == PICK_PHOTO_CODE) {
            Uri photoUri = data.getData();
            Bitmap selectedImage = loadFromUri(photoUri);
            saveBitmapToFile(photoFileName, selectedImage);
            ivPostImage.setImageBitmap(selectedImage);
        }
    }

    public void saveBitmapToFile(String fileName, Bitmap bm) {
        File mediaStorageDir = new File(getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), TAG);

        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()) {
            Log.d(TAG, "Failed to create directory");
        }

        File imageFile = new File(mediaStorageDir, fileName);
        FileOutputStream fos = null;

        Bitmap.CompressFormat format = Bitmap.CompressFormat.PNG;
        int quality = 100;
        try {
            fos = new FileOutputStream(getPhotoFileUri(photoFileName));
            bm.compress(format, quality, fos);
            fos.close();
        } catch (IOException e) {
            Log.d(TAG, e.getMessage());
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    public interface OnComposeFragmentSubmitListener {
        public void onButtonClick(Post post);
    }
}