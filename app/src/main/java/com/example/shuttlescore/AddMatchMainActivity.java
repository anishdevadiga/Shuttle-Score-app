package com.example.shuttlescore;

import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RadioButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

public class AddMatchMainActivity extends AppCompatActivity {
    RadioButton toggleSingle, toggleDouble;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.addmatchactivity_main);

        toggleSingle = findViewById(R.id.toggleSingle);
        toggleDouble = findViewById(R.id.toggleDouble);

        // Set default fragment to Single Player
        loadFragment(new SinglePlayerFragment());

        // Animation setup
        Animation toggleAnim = AnimationUtils.loadAnimation(this, R.anim.toggle_animation);

        toggleSingle.setOnClickListener(v -> {
            toggleSingle.startAnimation(toggleAnim);
            loadFragment(new SinglePlayerFragment());
            toggleSingle.setChecked(true);
            toggleDouble.setChecked(false);
        });

        toggleDouble.setOnClickListener(v -> {
            toggleDouble.startAnimation(toggleAnim);
            loadFragment(new DoublePlayerFragment());
            toggleDouble.setChecked(true);
            toggleSingle.setChecked(false);
        });

    }

    // Method to load the selected fragment
    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

}