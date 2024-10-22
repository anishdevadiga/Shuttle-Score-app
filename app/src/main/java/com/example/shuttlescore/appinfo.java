package com.example.shuttlescore;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.content.SharedPreferences;
import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class appinfo extends AppCompatActivity {

    private DatabaseReference userRef; // Reference to your user data in the database

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appinfo);

        // Initialize Firebase Database reference
        userRef = FirebaseDatabase.getInstance().getReference("Users"); // Adjust this to your database structure

        ImageView back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(appinfo.this, profileActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // Initialize TextViews
        TextView emailTextView = findViewById(R.id.emailtext);
        TextView userTextView = findViewById(R.id.nametext);
        TextView phoneTextView = findViewById(R.id.phonenumbertext);

        // Load user data
        loadUserData(emailTextView, userTextView, phoneTextView);
    }

    private void loadUserData(final TextView emailTextView, final TextView userTextView, final TextView phoneTextView) {
        // Assuming you have the user's ID to retrieve their data
        String userId = "user_id_here"; // Replace with actual user ID retrieval logic
        userRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Assuming the user object has these fields
                    String email = dataSnapshot.child("email").getValue(String.class);
                    String username = dataSnapshot.child("username").getValue(String.class);
                    String phone = dataSnapshot.child("phone").getValue(String.class);

                    // Set the values to TextViews
                    emailTextView.setText(email);
                    userTextView.setText(username);
                    phoneTextView.setText(phone);
                } else {
                    // Handle case where user data doesn't exist
                    emailTextView.setText("No email found");
                    userTextView.setText("No username found");
                    phoneTextView.setText("No phone number found");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle database error
                emailTextView.setText("Error loading email");
                userTextView.setText("Error loading username");
                phoneTextView.setText("Error loading phone number");
            }
        });
    }
}
