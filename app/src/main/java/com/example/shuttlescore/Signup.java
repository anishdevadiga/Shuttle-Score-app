package com.example.shuttlescore;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Signup extends AppCompatActivity {

    EditText name, email, phonenum, password, confirm;
    Button register;
    TextView redirectLogin;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Initialize Firebase Authentication and Realtime Database
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("Users");

        // Initialize UI elements
        name = findViewById(R.id.Rname);
        email = findViewById(R.id.Remail);
        phonenum = findViewById(R.id.Rphone);
        password = findViewById(R.id.Rpassword);
        confirm = findViewById(R.id.Rconfirm);
        register = findViewById(R.id.register_button);
        redirectLogin = findViewById(R.id.relogin);

        // Handle registration process
        register.setOnClickListener(v -> registerUser());

        // Redirect to Login page
        redirectLogin.setOnClickListener(v -> {
            Intent intent = new Intent(Signup.this, Login.class);
            startActivity(intent);
        });
    }

    // Method to handle user registration
    private void registerUser() {
        String userName = name.getText().toString().trim();
        String userEmail = email.getText().toString().trim();
        String userPhone = phonenum.getText().toString().trim();
        String userPassword = password.getText().toString().trim();
        String confirmPassword = confirm.getText().toString().trim();

        // Input validation
        if (TextUtils.isEmpty(userName)) {
            name.setError("Name is required");
            return;
        }
        if (TextUtils.isEmpty(userEmail)) {
            email.setError("Email is required");
            return;
        }
        if (TextUtils.isEmpty(userPhone)) {
            phonenum.setError("Phone number is required");
            return;
        }
        if (TextUtils.isEmpty(userPassword)) {
            password.setError("Password is required");
            return;
        }
        if (!userPassword.equals(confirmPassword)) {
            confirm.setError("Passwords do not` match");
            return;
        }

        // Firebase Authentication: Create a new user with email and password
        mAuth.createUserWithEmailAndPassword(userEmail, userPassword)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Get current authenticated user
                        FirebaseUser user = mAuth.getCurrentUser();

                        if (user != null) {
                            // Save additional user information (name, phone) to Firebase Realtime Database
                            saveUserData(user.getUid(), userName, userEmail, userPhone);
                            Toast.makeText(Signup.this, "Registration successful", Toast.LENGTH_SHORT).show();

                            // Redirect to login activity after successful registration
                            Intent intent = new Intent(Signup.this, Login.class);
                            startActivity(intent);
                        }
                    } else {
                        // Handle registration failure
                        Toast.makeText(Signup.this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveUserData(String userId, String name, String email, String phone) {
        // Create a user object to store in Firebase Realtime Database
        User user = new User(name, email, phone);

        // Store user data under the user's UID in the "Users" node
        mDatabase.child(userId).setValue(user)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(Signup.this, "User data saved successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(Signup.this, "Failed to save user data: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // User class to represent user data in Firebase Realtime Database
    public static class User {
        public String name, email, phone;

        public User() {
            // Default constructor required for Firebase
        }

        public User(String name, String email, String phone) {
            this.name = name;
            this.email = email;
            this.phone = phone;
        }
    }
}
