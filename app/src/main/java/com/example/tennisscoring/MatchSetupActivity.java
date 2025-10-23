package com.example.tennisscoring;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class MatchSetupActivity extends AppCompatActivity {

    private EditText etMatchTitle, etMatchVenue, etPlayer1Name, etPlayer2Name;
    private EditText etTeamAPlayer1Name, etTeamAPlayer2Name, etTeamBPlayer1Name, etTeamBPlayer2Name;
    private RadioGroup rgTossWon, rgGameType;
    private Spinner spinnerMatchFormat;
    private Button btnStartMatch;
    private LinearLayout singlesLayout, doublesLayout;
    private RadioButton rbPlayer1Toss, rbPlayer2Toss;


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

        // New views
        rgGameType = findViewById(R.id.rg_game_type);
        singlesLayout = findViewById(R.id.singles_layout);
        doublesLayout = findViewById(R.id.doubles_layout);
        etTeamAPlayer1Name = findViewById(R.id.et_team_a_player1_name);
        etTeamAPlayer2Name = findViewById(R.id.et_team_a_player2_name);
        etTeamBPlayer1Name = findViewById(R.id.et_team_b_player1_name);
        etTeamBPlayer2Name = findViewById(R.id.et_team_b_player2_name);
        rbPlayer1Toss = findViewById(R.id.rb_player1);
        rbPlayer2Toss = findViewById(R.id.rb_player2);


        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.match_formats, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMatchFormat.setAdapter(adapter);

        rgGameType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_singles) {
                singlesLayout.setVisibility(View.VISIBLE);
                doublesLayout.setVisibility(View.GONE);
                rbPlayer1Toss.setText(R.string.player_1);
                rbPlayer2Toss.setText(R.string.player_2);
            } else if (checkedId == R.id.rb_doubles) {
                singlesLayout.setVisibility(View.GONE);
                doublesLayout.setVisibility(View.VISIBLE);
                rbPlayer1Toss.setText("Team A");
                rbPlayer2Toss.setText("Team B");
            }
        });

        btnStartMatch.setOnClickListener(v -> {
            HapticUtils.performHapticFeedback(this);
            startMatch();
        });
    }

    private void startMatch() {
        String matchTitle = etMatchTitle.getText().toString().trim();
        String matchVenue = etMatchVenue.getText().toString().trim();

        int firstServer = (rgTossWon.getCheckedRadioButtonId() == R.id.rb_player1) ? 1 : 2;
        int setsToWin = spinnerMatchFormat.getSelectedItemPosition() == 0 ? 2 : 3; // Best of 3 sets needs 2 to win, Best of 5 needs 3.

        Intent intent = new Intent(MatchSetupActivity.this, MainActivity.class);
        intent.putExtra("MATCH_TITLE", matchTitle);
        intent.putExtra("MATCH_VENUE", matchVenue);
        intent.putExtra("FIRST_SERVER", firstServer);
        intent.putExtra("SETS_TO_WIN", setsToWin);

        if (rgGameType.getCheckedRadioButtonId() == R.id.rb_singles) {
            String p1Name = etPlayer1Name.getText().toString().trim();
            String p2Name = etPlayer2Name.getText().toString().trim();

            if (p1Name.isEmpty() || p2Name.isEmpty()) {
                Toast.makeText(this, "Player names cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }
            intent.putExtra("GAME_TYPE", "singles");
            intent.putExtra("PLAYER_1_NAME", p1Name);
            intent.putExtra("PLAYER_2_NAME", p2Name);
        } else {
            String teamAP1 = etTeamAPlayer1Name.getText().toString().trim();
            String teamAP2 = etTeamAPlayer2Name.getText().toString().trim();
            String teamBP1 = etTeamBPlayer1Name.getText().toString().trim();
            String teamBP2 = etTeamBPlayer2Name.getText().toString().trim();
            if (teamAP1.isEmpty() || teamAP2.isEmpty() || teamBP1.isEmpty() || teamBP2.isEmpty()) {
                Toast.makeText(this, "Player names for doubles cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }
            intent.putExtra("GAME_TYPE", "doubles");
            intent.putExtra("PLAYER_1_NAME", teamAP1 + " & " + teamAP2);
            intent.putExtra("PLAYER_2_NAME", teamBP1 + " & " + teamBP2);
        }
        
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return true;
    }
}
