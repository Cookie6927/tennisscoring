package com.example.tennisscoring;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.tennisscoring.database.AppDatabase;
import com.example.tennisscoring.database.Match;

import java.util.Locale;

public class MatchSummaryActivity extends BaseActivity {

    private Match currentMatch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match_summary);

        setupToolbar();
        getSupportActionBar().setTitle("Match Summary");

        long matchId = getIntent().getLongExtra("MATCH_ID", -1);
        if (matchId == -1) {
            finish(); // No match ID, can't show summary
            return;
        }

        AppDatabase.databaseWriteExecutor.execute(() -> {
            currentMatch = AppDatabase.getInstance(this).matchDao().getMatchById(matchId);
            runOnUiThread(() -> {
                if (currentMatch != null) {
                    populateMatchData(currentMatch);
                }
            });
        });

        setupButtonClickListeners();
    }

    private void populateMatchData(Match match) {
        TextView tvWinnerName = findViewById(R.id.tv_winner_name);
        TextView tvMatchDuration = findViewById(R.id.tv_match_duration);
        TableLayout scoreTable = findViewById(R.id.tl_score_details);
        scoreTable.removeAllViews(); // Clear any previous views

        tvWinnerName.setText(match.getWinnerName());

        String detailedScore = match.getDetailedScore();
        String[] setScores = (detailedScore != null && !detailedScore.isEmpty()) ? detailedScore.split(", ") : new String[0];
        int numSets = setScores.length;

        // Define layout parameters for table cells
        TableRow.LayoutParams playerColParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
        TableRow.LayoutParams scoreColParams = new TableRow.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT, 1f);

        // --- Header Row ---
        TableRow headerRow = new TableRow(this);
        TextView playerHeader = createTableCell(getString(R.string.player), true);
        headerRow.addView(playerHeader, playerColParams);

        for (int i = 1; i <= numSets; i++) {
            TextView setHeader = createTableCell(String.format(Locale.getDefault(), "Set %d", i), true);
            setHeader.setGravity(Gravity.CENTER);
            headerRow.addView(setHeader, scoreColParams);
        }
        scoreTable.addView(headerRow);

        boolean isPlayer1Winner = match.getWinnerName().equals(match.getPlayer1Name());

        // --- Player 1 Row ---
        TableRow p1Row = new TableRow(this);
        TextView p1Name = createTableCell(match.getPlayer1Name(), false);
        if (isPlayer1Winner) {
            p1Name.setTypeface(null, Typeface.BOLD);
        }
        p1Row.addView(p1Name, playerColParams);

        for (String setScore : setScores) {
            String[] scores = setScore.split("-");
            TextView p1Score = createTableCell(scores.length > 0 ? scores[0] : "", false);
            p1Score.setGravity(Gravity.CENTER);
            if (isPlayer1Winner) {
                p1Score.setTypeface(null, Typeface.BOLD);
            }
            p1Row.addView(p1Score, scoreColParams);
        }
        scoreTable.addView(p1Row);

        // --- Player 2 Row ---
        TableRow p2Row = new TableRow(this);
        TextView p2Name = createTableCell(match.getPlayer2Name(), false);
        if (!isPlayer1Winner) {
            p2Name.setTypeface(null, Typeface.BOLD);
        }
        p2Row.addView(p2Name, playerColParams);

        for (String setScore : setScores) {
            String[] scores = setScore.split("-");
            TextView p2Score = createTableCell(scores.length > 1 ? scores[1] : "", false);
            p2Score.setGravity(Gravity.CENTER);
            if (!isPlayer1Winner) {
                p2Score.setTypeface(null, Typeface.BOLD);
            }
            p2Row.addView(p2Score, scoreColParams);
        }
        scoreTable.addView(p2Row);

        // --- Duration ---
        long durationInSeconds = match.getDuration();
        if (durationInSeconds > 0) {
            long hours = durationInSeconds / 3600;
            long minutes = (durationInSeconds % 3600) / 60;
            String durationString;
            if (hours > 0) {
                durationString = String.format(Locale.getDefault(), "%d hr %d min", hours, minutes);
            } else {
                long secs = durationInSeconds % 60;
                durationString = String.format(Locale.getDefault(), "%d min %d sec", minutes, secs);
            }
            tvMatchDuration.setText("Match Duration: " + durationString);
            tvMatchDuration.setVisibility(View.VISIBLE);
        } else {
            tvMatchDuration.setVisibility(View.GONE);
        }
    }

    private TextView createTableCell(String text, boolean isHeader) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setPadding(32, 32, 32, 32);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
        textView.setBackgroundResource(R.drawable.table_cell_border);
        if (isHeader) {
            textView.setTypeface(null, Typeface.BOLD);
        }
        return textView;
    }

    private void setupButtonClickListeners() {
        Button btnShareResult = findViewById(R.id.btn_share_result);
        Button btnStartNewMatch = findViewById(R.id.btn_start_new_match);

        btnShareResult.setOnClickListener(v -> shareMatchResult());
        btnStartNewMatch.setOnClickListener(v -> startNewMatch());
    }

private void shareMatchResult() {
    if (currentMatch == null) return;

    String player1Name = currentMatch.getPlayer1Name();
    String player2Name = currentMatch.getPlayer2Name();
    String winnerName = currentMatch.getWinnerName();

    boolean isPlayer1Winner = winnerName.equals(player1Name);

    // Build the score string with the winner's score bolded
    StringBuilder scoreDetails = new StringBuilder();
    String[] setScores = currentMatch.getDetailedScore().split(", ");
    for (int i = 0; i < setScores.length; i++) {
        String[] scores = setScores[i].split("-");
        String p1Score = scores[0];
        String p2Score = scores[1];

        if (isPlayer1Winner) {
            scoreDetails.append("*").append(p1Score).append("*").append("-").append(p2Score);
        } else {
            scoreDetails.append(p1Score).append("-").append("*").append(p2Score).append("*");
        }

        if (i < setScores.length - 1) {
            scoreDetails.append(", ");
        }
    }

    String shareBody = "Tennis Match Result:\n" +
            "Winner: " + winnerName + "\n" +
            "Score: " + scoreDetails + "\n" +
            "Players: " + player1Name + " vs " + player2Name;

    Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
    sharingIntent.setType("text/plain");
    sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Match Result");
    sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
    startActivity(Intent.createChooser(sharingIntent, "Share via"));
}

    private void startNewMatch() {
        Intent intent = new Intent(this, MatchSetupActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
