package com.example.kilogram.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;
import android.view.Menu;

import com.example.kilogram.Fragments.ProfileFragment;
import com.example.kilogram.R;
import com.parse.ParseUser;

import org.parceler.Parcels;

public class PostProfileActivity extends AppCompatActivity {
    public static final String TAG = "PostProfileActivity";

    final FragmentManager fragmentManager = getSupportFragmentManager();
    ParseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_profile);

        user = Parcels.unwrap(getIntent().getParcelableExtra(ParseUser.class.getSimpleName()));
        ProfileFragment profileFragment = ProfileFragment.newInstance(user);
        fragmentManager.beginTransaction().replace(R.id.flContainer, profileFragment).commit();

        androidx.appcompat.widget.Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
}