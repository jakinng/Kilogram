package com.example.kilogram;

import android.app.Application;

import com.example.kilogram.Models.Post;
import com.parse.Parse;
import com.parse.ParseObject;

public class ParseApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        ParseObject.registerSubclass(Post.class);

        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId("XwRDkBSEVKpEFeLX2kYqNB8bQ3u2X64XOpEL9sDR")
                .clientKey("NJPXXcvJO20VSrvmRwpd5FZtTcxkRi60OiH8iR1r")
                .server("https://parseapi.back4app.com")
                .build());
    }
}
