package com.example.kilogram;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.parse.ParseUser;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "MainActivity";

    private EditText etDescription;
    private Button btnCaptureImage;
    private ImageView ivPostImage;
    private Button btnSubmit;
    private Button btnLogout;
    private ParseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Attach views to the corresponding instance variables
        setupViews();

        // Setup the logout button
        setupLogoutButton();
    }

    private void setupViews() {
        etDescription = (EditText) findViewById(R.id.etDescription);
        btnCaptureImage = (Button) findViewById(R.id.btnCaptureImage);
        ivPostImage = (ImageView) findViewById(R.id.ivPostImage);
        btnSubmit = (Button) findViewById(R.id.btnSubmit);
    }

    private void setupLogoutButton() {
        btnLogout = (Button) findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ParseUser.logOut();
                currentUser = ParseUser.getCurrentUser();
                goLoginActivity();
            }
        });
    }

    private void goLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}