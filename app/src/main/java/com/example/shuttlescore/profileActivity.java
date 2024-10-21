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

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Objects;

public class profileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE = 1;
    private ImageView selectedImage; // Profile image in main profile
    private ImageView dialogImage; // ImageView in the dialog
    private Bitmap selectedBitmap;
    boolean passwordvisible, passvisible;// To store the selected bitmap

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

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

        // Menu items
        LinearLayout l1 = findViewById(R.id.menu1);
        LinearLayout l2 = findViewById(R.id.menu2);
        LinearLayout l3 = findViewById(R.id.menu3);
        LinearLayout l4 = findViewById(R.id.menu4);

        // Logic for l1 to pass image to next intent
        l1.setOnClickListener(view -> {
            // Ensure there's an image set
            if (selectedImage.getDrawable() != null) {
                // Convert the image to a ByteArray
                selectedImage.setDrawingCacheEnabled(true);
                selectedImage.buildDrawingCache();
                Bitmap bitmap = selectedBitmap; // Use the current selectedBitmap

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream); // Compress bitmap to PNG
                byte[] byteArray = stream.toByteArray();

                // Pass the byteArray to the new Intent
                Intent intent = new Intent(profileActivity.this, appinfo.class);
                intent.putExtra("profileImage", byteArray);
                startActivity(intent);
                finish();
            }
        });

        // Click listener for profile update
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

        l3.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public void onClick(View view) {
                AlertDialog.Builder b1 = new AlertDialog.Builder(profileActivity.this);
                View v1 = getLayoutInflater().inflate(R.layout.changepassword, null);
                b1.setView(v1);

                AlertDialog dialog1 = b1.create();
                EditText currentpass = v1.findViewById(R.id.currentpassbox);
                EditText newpass = v1.findViewById(R.id.newpassbox);
                currentpass.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        final int End = 2;
                        if (event.getAction() == MotionEvent.ACTION_UP) {
                            if (event.getRawX() >= currentpass.getRight() - currentpass.getCompoundDrawables()[End].getBounds().width()) {
                                int selection = currentpass.getSelectionEnd();
                                if (passwordvisible) {
                                    currentpass.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.visilbeff_foreground, 0);
                                    currentpass.setTransformationMethod(PasswordTransformationMethod.getInstance());
                                    passwordvisible = false;
                                } else {
                                    currentpass.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.visilbeon_foreground, 0);
                                    currentpass.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                                    passwordvisible = true;
                                }
                                currentpass.setSelection(selection);
                                return true;
                            }
                        }
                        return false;
                    }
                });

                newpass.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        final int End = 2;
                        if (event.getAction() == MotionEvent.ACTION_UP) {
                            if (event.getRawX() >= newpass.getRight() - newpass.getCompoundDrawables()[End].getBounds().width()) {
                                int selection = currentpass.getSelectionEnd();
                                if (passvisible) {
                                    newpass.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.visilbeff_foreground, 0);
                                    newpass.setTransformationMethod(PasswordTransformationMethod.getInstance());
                                    passvisible = false;
                                } else {
                                    newpass.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.visilbeon_foreground, 0);
                                    newpass.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                                    passvisible = true;
                                }
                                newpass.setSelection(selection);
                                return true;
                            }
                        }
                        return false;
                    }
                });

                v1.findViewById(R.id.btncancelpass).setOnClickListener(view1 -> dialog1.dismiss());
                if (dialog1.getWindow() != null) {
                    dialog1.getWindow().setBackgroundDrawable(new ColorDrawable(0));
                }
                dialog1.show();
            }
        });

        l4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder b2 = new AlertDialog.Builder(profileActivity.this);
                View v2 = getLayoutInflater().inflate(R.layout.logoutdialog, null);
                b2.setView(v2);

                AlertDialog dialog2 = b2.create();
                v2.findViewById(R.id.btnlogcancel).setOnClickListener(view1 -> dialog2.dismiss());
                if (dialog2.getWindow() != null) {
                    dialog2.getWindow().setBackgroundDrawable(new ColorDrawable(0));
                }
                dialog2.show();
            }
        });
    }

    // Handle the result from the gallery intent
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            try {
                selectedBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                dialogImage.setImageBitmap(selectedBitmap); // Set image in dialog
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
