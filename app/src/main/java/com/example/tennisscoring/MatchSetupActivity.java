package com.example.tennisscoring;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class MatchSetupActivity extends AppCompatActivity {

    private EditText etMatchTitle, etMatchVenue, etPlayer1Name, etPlayer2Name;
    private RadioGroup rgTossWon;
    private Spinner spinnerMatchFormat;
    private Button btnStartMatch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match_setup);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("New Match Setup");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        etMatchTitle = findViewById(R.id.et_match_title);
        etMatchVenue = findViewById(R.id.et_match_venue);
        etPlayer1Name = findViewById(R.id.et_player1_name);
        etPlayer2Name = findViewById(R.id.et_player2_name);
        rgTossWon = findViewById(R.id.rg_toss_won);
        spinnerMatchFormat = findViewById(R.id.spinner_match_format);
        btnStartMatch = findViewById(R.id.btn_start_match);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.match_formats, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMatchFormat.setAdapter(adapter);

        btnStartMatch.setOnClickListener(v -> {
            HapticUtils.performHapticFeedback(this);
            startMatch();
        });
    }

    private void startMatch() {
        String p1Name = etPlayer1Name.getText().toString().trim();
        String p2Name = etPlayer2Name.getText().toString().trim();

        if (p1Name.isEmpty() || p2Name.isEmpty()) {
            Toast.makeText(this, "Player names cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        String matchTitle = etMatchTitle.getText().toString().trim();
        String matchVenue = etMatchVenue.getText().toString().trim();

        int firstServer = (rgTossWon.getCheckedRadioButtonId() == R.id.rb_player1) ? 1 : 2;
        int setsToWin = spinnerMatchFormat.getSelectedItemPosition() == 0 ? 2 : 3; // Best of 3 sets needs 2 to win, Best of 5 needs 3.

        Intent intent = new Intent(MatchSetupActivity.this, MainActivity.class);
        intent.putExtra("PLAYER_1_NAME", p1Name);
        intent.putExtra("PLAYER_2_NAME", p2Name);
        intent.putExtra("MATCH_TITLE", matchTitle);
        intent.putExtra("MATCH_VENUE", matchVenue);
        intent.putExtra("FIRST_SERVER", firstServer);
        intent.putExtra("SETS_TO_WIN", setsToWin);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return true;
    }
}
