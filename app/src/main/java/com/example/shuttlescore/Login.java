package com.example.shuttlescore;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Login extends AppCompatActivity {
    EditText loginemail,loginpassword;
    Button loginbuton;
    TextView redirect;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("Users");

        loginemail=findViewById(R.id.email);
        loginpassword=findViewById(R.id.password);
        redirect=findViewById(R.id.register);
        loginbuton=findViewById(R.id.login_button);

        loginbuton.setOnClickListener(v -> loginUser());

        redirect.setOnClickListener(view -> {
            Intent intent=new Intent(Login.this,Signup.class);
            startActivity(intent);
        });
    }

    private void loginUser() {
        String email = loginemail.getText().toString().trim();
        String password = loginpassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            loginemail.setError("Email is required");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            loginpassword.setError("Password is required");
            return;
        }
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Login successful
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null) {
                    saveUserData(user.getUid(), email);  // Save user data in Firebase Realtime Database
                    Toast.makeText(Login.this, "Login successful", Toast.LENGTH_SHORT).show();
                }
            } else {
                // If login fails
                Toast.makeText(Login.this, "Authentication failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveUserData(String userId, String email) {
        // Save user information in Firebase Realtime Database
        User user = new User(email);
        mDatabase.child(userId).setValue(user).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(Login.this, "User data saved", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(Login.this, "Failed to save user data: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static class User {
        public String email;

        public User() {
            // Default constructor required for calls to DataSnapshot.getValue(User.class)
        }

        public User(String email) {
            this.email = email;
        }
    }

    }