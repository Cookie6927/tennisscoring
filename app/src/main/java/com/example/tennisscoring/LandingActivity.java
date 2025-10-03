package com.example.tennisscoring;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class LandingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);

        // Find the buttons in the layout
        Button btnStartNewMatch = findViewById(R.id.btn_start_new_match);
        Button btnMatchHistory = findViewById(R.id.btn_match_history);
        Button btnSettings = findViewById(R.id.btn_settings);

        // Set a click listener for the "Start New Match" button
        btnStartNewMatch.setOnClickListener(v -> {
            HapticUtils.performHapticFeedback(this);
            // Create an Intent to open the MatchSetupActivity
            Intent intent = new Intent(LandingActivity.this, MatchSetupActivity.class);
            startActivity(intent);
        });

        // Set a click listener for the "Match History" button
        btnMatchHistory.setOnClickListener(v -> {
            HapticUtils.performHapticFeedback(this);
            // Create an Intent to open the HistoryActivity
            Intent intent = new Intent(LandingActivity.this, HistoryActivity.class);
            startActivity(intent);
        });

        // Set a click listener for the "Settings" button
        btnSettings.setOnClickListener(v -> {
            HapticUtils.performHapticFeedback(this);
            // Create an Intent to open the SettingsActivity
            Intent intent = new Intent(LandingActivity.this, SettingsActivity.class);
            startActivity(intent);
        });
    }
}

