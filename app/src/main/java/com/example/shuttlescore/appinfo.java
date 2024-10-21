package com.example.shuttlescore;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class appinfo extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appinfo);

        ImageView profileImageView = findViewById(R.id.image); // The ImageView to display the profile image

        // Retrieve the byteArray from Intent
        byte[] byteArray = getIntent().getByteArrayExtra("profileImage");

        if (byteArray != null) {
            // Decode the byteArray to a Bitmap
            Bitmap bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
            profileImageView.setImageBitmap(bmp); // Set the profile image
        }
    }
}
