package com.example.letschat_nologin;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

public class ViewImage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_view_image);

        ImageView imageView = findViewById(R.id.myZoomageView);

        Intent intent = getIntent();
        String sender = intent.getStringExtra("sender");
        if (sender.equalsIgnoreCase("other")) {
            String url = intent.getStringExtra("url");
            Glide.with(this).load(url).into(imageView);
        }
    }
}