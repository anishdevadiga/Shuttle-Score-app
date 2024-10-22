package com.example.shuttlescore;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Base64;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Objects;

public class profileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE = 1;
    private ImageView selectedImage; // Profile image in main profile
    private ImageView dialogImage; // ImageView in the dialog
    private Bitmap selectedBitmap;
    boolean passwordvisible, passvisible;// To store the selected bitmap
    private FirebaseAuth mAuth;
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        // Initialize Firebase Auth and Database
        mAuth = FirebaseAuth.getInstance();
        userRef = FirebaseDatabase.getInstance().getReference("Users");

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomnavigationbar);
        bottomNavigationView.setSelectedItemId(R.id.person);
        selectedImage = findViewById(R.id.profilepic); // The profile picture in the main layout

        // Retrieve and set saved image from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("profilePrefs", Context.MODE_PRIVATE);
        String savedImage = prefs.getString("profileImage", null);
        if (savedImage != null) {
            byte[] imageBytes = Base64.decode(savedImage, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            selectedImage.setImageBitmap(bitmap); // Set saved image
        }

        // Load username and email from Firebase Realtime Database
        TextView user = findViewById(R.id.username);
        TextView email = findViewById(R.id.email);
        loadUserProfile(user, email);

        // Bottom navigation selection listener
        bottomNavigationView.setOnItemSelectedListener(item -> {
            String str = (String) item.getTitle();
            switch (Objects.requireNonNull(str)) {
                case "Home":
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    finish();
                    return true;
                case "me":
                    return true;
            }
            return false;
        });

        // Image delete logic
        ImageView delete = findViewById(R.id.deleteIcon);
        delete.setOnClickListener(view -> {
            // Clear the image from SharedPreferences
            SharedPreferences.Editor editor = getSharedPreferences("profilePrefs", Context.MODE_PRIVATE).edit();
            editor.remove("profileImage");
            editor.apply();

            // Clear the ImageView by setting it to a placeholder image or null
            selectedImage.setImageResource(R.drawable.profile_foreground); // Replace with your placeholder
            selectedBitmap = null; // Remove the bitmap reference
        });

        // Menu items
        LinearLayout l1 = findViewById(R.id.menu1);
        LinearLayout l2 = findViewById(R.id.menu2);
        LinearLayout l3 = findViewById(R.id.menu3);
        LinearLayout l4 = findViewById(R.id.menu4);

        // Logic for l1 to pass image to next intent
        l1.setOnClickListener(view -> {
            Intent intent = new Intent(profileActivity.this, appinfo.class);
            startActivity(intent);
            finish();
            // Ensure there's an image set
//            if (selectedImage.getDrawable() != null) {
//                // Convert the image to a ByteArray
//                selectedImage.setDrawingCacheEnabled(true);
//                selectedImage.buildDrawingCache();
//                Bitmap bitmap = selectedBitmap; // Use the current selectedBitmap
//
//                ByteArrayOutputStream stream = new ByteArrayOutputStream();
//                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream); // Compress bitmap to PNG
//                byte[] byteArray = stream.toByteArray();
//
//                // Pass the byteArray to the new Intent
//                Intent intent = new Intent(profileActivity.this, appinfo.class);
//                intent.putExtra("profileImage", byteArray);
//                startActivity(intent);
//                finish();
//            }
        });

        // Click listener for profile update dialog
        l2.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(profileActivity.this);
            View dialogView = getLayoutInflater().inflate(R.layout.changeprofile, null);
            builder.setView(dialogView);

            dialogImage = dialogView.findViewById(R.id.imagebox); // ImageView in the dialog
            AlertDialog dialog = builder.create();

            dialogView.findViewById(R.id.btncancel).setOnClickListener(v -> dialog.dismiss());

            dialogView.findViewById(R.id.btnupload).setOnClickListener(v -> {
                if (selectedBitmap != null) {
                    selectedImage.setImageBitmap(selectedBitmap);

                    // Save the selected image to SharedPreferences
                    SharedPreferences.Editor editor = getSharedPreferences("profilePrefs", Context.MODE_PRIVATE).edit();
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    selectedBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                    byte[] imageBytes = baos.toByteArray();
                    String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
                    editor.putString("profileImage", encodedImage);
                    editor.apply();
                }
                dialog.dismiss();
            });

            // Image box in dialog to open gallery
            dialogImage.setOnClickListener(v -> {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, PICK_IMAGE);
            });

            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }

            dialog.show();
        });

        // Change password logic in l3
        l3.setOnClickListener(view -> showChangePasswordDialog());

        // Logout logic in l4
        l4.setOnClickListener(view -> {
            AlertDialog.Builder b2 = new AlertDialog.Builder(profileActivity.this);
            View v2 = getLayoutInflater().inflate(R.layout.logoutdialog, null);
            b2.setView(v2);

            AlertDialog dialog2 = b2.create();

            // Cancel button to dismiss the dialog
            v2.findViewById(R.id.btnlogcancel).setOnClickListener(view1 -> dialog2.dismiss());

            // Confirm logout button
            v2.findViewById(R.id.btnlogout).setOnClickListener(view12 -> {
                // Perform logout
                mAuth.signOut();
                Toast.makeText(profileActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();

                // Go back to the login screen
//                Intent intent = new Intent(profileActivity.this, LoginActivity.class);
//                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
//                startActivity(intent);
//                finish(); // Optional: Call finish() to remove this activity from the back stack

                dialog2.dismiss(); // Dismiss the dialog after logout
            });

            if (dialog2.getWindow() != null) {
                dialog2.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }
            dialog2.show();
        });
    }

    // Load user profile (username, email) from Firebase Realtime Database
    private void loadUserProfile(TextView usernameTextView, TextView emailTextView) {
        // Check if the user is logged in
        if (mAuth.getCurrentUser() != null) {
            String userId = mAuth.getCurrentUser().getUid();
            userRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String username = dataSnapshot.child("username").getValue(String.class);
                        String email = dataSnapshot.child("email").getValue(String.class);
                        usernameTextView.setText(username);
                        emailTextView.setText(email);
                    } else {
                        // Handle case where user data does not exist
                        usernameTextView.setText("User not found");
                        emailTextView.setText("No email found");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle database error
                    usernameTextView.setText("Error loading username");
                    emailTextView.setText("Error loading email");
                }
            });
        } else {
            // User is not logged in
            usernameTextView.setText("Not logged in");
            emailTextView.setText("No email available");
        }
    }


    // Logic to toggle password visibility

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            try {
                selectedBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                dialogImage.setImageBitmap(selectedBitmap); // Set the selected image in the dialog
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private void showChangePasswordDialog() {
        AlertDialog.Builder b1 = new AlertDialog.Builder(profileActivity.this);
        View v1 = getLayoutInflater().inflate(R.layout.changepassword, null);
        b1.setView(v1);

        AlertDialog dialog1 = b1.create();
        EditText currentpass = v1.findViewById(R.id.currentpassbox);
        EditText newpass = v1.findViewById(R.id.newpassbox);
        TextView authenticateBtn = v1.findViewById(R.id.btnauth);
        TextView changePasswordBtn = v1.findViewById(R.id.btnchange);

        // Initially disable new password field and change button
        newpass.setEnabled(false);
        changePasswordBtn.setEnabled(false);

        // Logic to toggle password visibility
        setPasswordToggleLogic(currentpass, true);
        setPasswordToggleLogic(newpass, false);

        // Authenticate current password
        authenticateBtn.setOnClickListener(view -> {
            String enteredCurrentPassword = currentpass.getText().toString();
            if (enteredCurrentPassword.isEmpty()) {
                Toast.makeText(profileActivity.this, "Enter current password", Toast.LENGTH_SHORT).show();
                return;
            }
            authenticateCurrentPassword(enteredCurrentPassword, newpass, changePasswordBtn);
        });

        // Change password on valid authentication
        changePasswordBtn.setOnClickListener(view -> {
            String newPassword = newpass.getText().toString();
            if (!newPassword.isEmpty()) {
                changePassword(newPassword);
                dialog1.dismiss();
            } else {
                Toast.makeText(profileActivity.this, "Enter new password", Toast.LENGTH_SHORT).show();
            }
        });

        v1.findViewById(R.id.btncancelpass).setOnClickListener(view1 -> dialog1.dismiss());

        if (dialog1.getWindow() != null) {
            dialog1.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }
        dialog1.show();
    }

    // Authenticate current password using Firebase Realtime Database
    private void authenticateCurrentPassword(String currentPassword, EditText newPass, TextView changePasswordBtn) {
        String userId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        userRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String storedPassword = dataSnapshot.child("password").getValue(String.class);
                    if (storedPassword != null && storedPassword.equals(currentPassword)) {
                        Toast.makeText(profileActivity.this, "Authenticated", Toast.LENGTH_SHORT).show();
                        newPass.setEnabled(true); // Enable new password field
                        changePasswordBtn.setEnabled(true); // Enable change password button
                    } else {
                        Toast.makeText(profileActivity.this, "Wrong password", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(profileActivity.this, "Error authenticating", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Change password in Firebase Realtime Database
    private void changePassword(String newPassword) {
        String userId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        userRef.child(userId).child("password").setValue(newPassword).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(profileActivity.this, "Password changed successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(profileActivity.this, "Failed to change password", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Logic to toggle password visibility
    @SuppressLint("ClickableViewAccessibility")
    private void setPasswordToggleLogic(EditText passwordEditText, boolean isCurrentPassword) {
        passwordEditText.setOnTouchListener((view, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                int drawableRight = 2;
                if (event.getRawX() >= (passwordEditText.getRight() - passwordEditText.getCompoundDrawables()[drawableRight].getBounds().width())) {
                    if (isCurrentPassword) {
                        if (passwordvisible) {
                            passwordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                            passwordEditText.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.visilbeon_foreground, 0);
                            passwordvisible = false;
                        } else {
                            passwordEditText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                            passwordEditText.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.visilbeff_foreground, 0);
                            passwordvisible = true;
                        }
                    } else {
                        if (passvisible) {
                            passwordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                            passwordEditText.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.visilbeon_foreground, 0);
                            passvisible = false;
                        } else {
                            passwordEditText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                            passwordEditText.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.visilbeff_foreground, 0);
                            passvisible = true;
                        }
                    }
                    return true;
                }
            }
            return false;
        });
    }
}
