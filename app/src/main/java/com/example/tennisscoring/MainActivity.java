package com.example.tennisscoring;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.example.tennisscoring.database.AppDatabase;
import com.example.tennisscoring.database.Match;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Stack;


public class MainActivity extends BaseActivity {

    // Match Setup Data
    private String player1Name, player2Name, matchTitle, matchVenue, matchType;
    private int setsToWin;

    // UI Elements
    private TextView tvTimer, tvPlayer1Points, tvPlayer1Games, tvPlayer1Sets;
    private TextView tvPlayer2Points, tvPlayer2Games, tvPlayer2Sets;
    private TextView tvPlayer1Name, tvPlayer2Name;
    private ImageView serverIndicatorP1, serverIndicatorP2;
    private Button btnAddPointP1, btnAddPointP2, btnUndo, btnReset, btnEnd;

    // Scoring state
    private int player1Points = 0;
    private int player1Games = 0;
    private int player1Sets = 0;
    private int player2Points = 0;
    private int player2Games = 0;
    private int player2Sets = 0;
    private ArrayList<String> setScores = new ArrayList<>();

    private boolean isPlayer1Serving = true;
    private boolean matchOver = false;

    // Timer state
    private int seconds = 0;
    private final Handler timerHandler = new Handler();
    private Runnable timerRunnable;

    // Undo functionality
    private final Stack<GameState> history = new Stack<>();

    private static class GameState {
        int p1Points, p1Games, p1Sets;
        int p2Points, p2Games, p2Sets;
        boolean p1Serving;
        ArrayList<String> setScores;

        GameState(int p1p, int p1g, int p1s, int p2p, int p2g, int p2s, boolean p1Serv, ArrayList<String> scores) {
            this.p1Points = p1p; this.p1Games = p1g; this.p1Sets = p1s;
            this.p2Points = p2p; this.p2Games = p2g; this.p2Sets = p2s;
            this.p1Serving = p1Serv;
            this.setScores = new ArrayList<>(scores);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        retrieveMatchData();
        initializeUI();
        setupToolbar();
        setupClickListeners();
        resetMatch();
    }

    private void retrieveMatchData() {
        Intent intent = getIntent();
        player1Name = intent.getStringExtra("PLAYER_1_NAME");
        player2Name = intent.getStringExtra("PLAYER_2_NAME");
        matchTitle = intent.getStringExtra("MATCH_TITLE");
        matchVenue = intent.getStringExtra("MATCH_VENUE");
        matchType = intent.getStringExtra("MATCH_TYPE");
        isPlayer1Serving = intent.getIntExtra("FIRST_SERVER", 1) == 1;
        setsToWin = intent.getIntExtra("SETS_TO_WIN", 2);
    }

    private void initializeUI() {
        tvTimer = findViewById(R.id.tv_timer);

        tvPlayer1Name = findViewById(R.id.tv_player1_name);
        tvPlayer1Points = findViewById(R.id.tv_player1_pt);
        tvPlayer1Games = findViewById(R.id.tv_player1_g);
        tvPlayer1Sets = findViewById(R.id.tv_player1_s);
        serverIndicatorP1 = findViewById(R.id.server_indicator_p1);

        tvPlayer2Name = findViewById(R.id.tv_player2_name);
        tvPlayer2Points = findViewById(R.id.tv_player2_pt);
        tvPlayer2Games = findViewById(R.id.tv_player2_g);
        tvPlayer2Sets = findViewById(R.id.tv_player2_s);
        serverIndicatorP2 = findViewById(R.id.server_indicator_p2);

        updatePlayerNames();

        btnAddPointP1 = findViewById(R.id.btn_add_point_p1);
        btnAddPointP1.setText("Add Point (" + player1Name + ")");
        btnAddPointP2 = findViewById(R.id.btn_add_point_p2);
        btnAddPointP2.setText("Add Point (" + player2Name + ")");

        btnUndo = findViewById(R.id.btn_undo);
        btnReset = findViewById(R.id.btn_reset);
        btnEnd = findViewById(R.id.btn_end_match);
    }

    private void updatePlayerNames() {
        tvPlayer1Name.setText(player1Name);
        tvPlayer2Name.setText(player2Name);
    }

    private void setupClickListeners() {
        btnAddPointP1.setOnClickListener(v -> {
            HapticUtils.performHapticFeedback(this);
            addPoint(1);
        });
        btnAddPointP2.setOnClickListener(v -> {
            HapticUtils.performHapticFeedback(this);
            addPoint(2);
        });

        btnUndo.setOnClickListener(v -> {
            HapticUtils.performHapticFeedback(this);
            undoLastPoint();
        });

        btnReset.setOnClickListener(v -> {
            HapticUtils.performHapticFeedback(this);
            resetPoints();
            updateUI();
        });

        btnEnd.setOnClickListener(v -> {
            HapticUtils.performHapticFeedback(this);
            new MaterialAlertDialogBuilder(this)
                .setTitle("End Match")
                .setMessage("Are you sure you want to end the current match?")
                .setPositiveButton("End Match", (dialog, which) -> endMatch(false, ""))
                .setNegativeButton("Cancel", null)
                .show();
        });
    }

    private void showResetDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Reset Match")
                .setMessage("Are you sure you want to reset all scores and start a new match?")
                .setPositiveButton("Yes, Reset", (dialog, which) -> resetMatch())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void addPoint(int player) {
        if (matchOver) return;
        saveCurrentState();

        if (player == 1) {
            player1Points++;
        } else {
            player2Points++;
        }

        if (player1Points >= 4 && player1Points - player2Points >= 2) {
            winGame(1);
        } else if (player2Points >= 4 && player2Points - player1Points >= 2) {
            winGame(2);
        }

        updateUI();
    }

    private void winGame(int player) {
        if (player == 1) player1Games++;
        else player2Games++;
        resetPoints();

        if ((player1Games >= 6 || player2Games >= 6) && Math.abs(player1Games - player2Games) >= 2) {
            winSet(player);
        }
        isPlayer1Serving = !isPlayer1Serving;
    }

    private void winSet(int player) {
        if (player == 1) {
            player1Sets++;
        } else {
            player2Sets++;
        }
        setScores.add(player1Games + "-" + player2Games);
        resetGames();

        if (player1Sets >= setsToWin) {
            declareWinner(player1Name);
        } else if (player2Sets >= setsToWin) {
            declareWinner(player2Name);
        }
    }

    private void declareWinner(String winner) {
        matchOver = true;
        stopTimer();
        endMatch(true, winner);
    }

    private void updateUI() {
        updatePlayerNames();

        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(com.google.android.material.R.attr.colorOnSurface, typedValue, true);
        int defaultTextColor = typedValue.data;

        int advantageColor = ContextCompat.getColor(this, R.color.advantage_red);

        tvPlayer1Points.setTextColor(defaultTextColor);
        tvPlayer2Points.setTextColor(defaultTextColor);
        tvPlayer1Games.setTextColor(defaultTextColor);
        tvPlayer2Games.setTextColor(defaultTextColor);
        tvPlayer1Sets.setTextColor(defaultTextColor);
        tvPlayer2Sets.setTextColor(defaultTextColor);
        tvPlayer1Name.setTextColor(defaultTextColor);
        tvPlayer2Name.setTextColor(defaultTextColor);
        tvTimer.setTextColor(defaultTextColor);

        if (player1Points >= 3 && player2Points >= 3) {
            if (player1Points == player2Points) {
                tvPlayer1Points.setText("Deuce");
                tvPlayer2Points.setText("Deuce");
                tvPlayer1Points.setTextColor(advantageColor);
                tvPlayer2Points.setTextColor(advantageColor);
            } else if (player1Points > player2Points) {
                tvPlayer1Points.setText("Ad");
                tvPlayer2Points.setText("40");
                tvPlayer1Points.setTextColor(advantageColor);
            } else {
                tvPlayer2Points.setText("Ad");
                tvPlayer1Points.setText("40");
                tvPlayer2Points.setTextColor(advantageColor);
            }
        } else {
            tvPlayer1Points.setText(getPointString(player1Points));
            tvPlayer2Points.setText(getPointString(player2Points));
        }

        tvPlayer1Games.setText(String.valueOf(player1Games));
        tvPlayer2Games.setText(String.valueOf(player2Games));
        tvPlayer1Sets.setText(String.valueOf(player1Sets));
        tvPlayer2Sets.setText(String.valueOf(player2Sets));

        serverIndicatorP1.setVisibility(isPlayer1Serving ? View.VISIBLE : View.INVISIBLE);
        serverIndicatorP2.setVisibility(!isPlayer1Serving ? View.VISIBLE : View.INVISIBLE);
        btnUndo.setEnabled(!history.isEmpty());
    }

    private String getPointString(int point) {
        switch (point) {
            case 0: return "0";
            case 1: return "15";
            case 2: return "30";
            case 3: return "40";
            default: return "40";
        }
    }

    private void resetPoints() {
        player1Points = 0; player2Points = 0;
    }

    private void resetGames() { player1Games = 0; player2Games = 0; }

    private void resetMatch() {
        resetPoints();
        resetGames();
        player1Sets = 0;
        player2Sets = 0;
        setScores.clear();
        matchOver = false;
        history.clear();
        startTimer();
        updateUI();
    }

    private void endMatch(boolean isWinnerDeclared, String winner) {
        if (matchOver) {
            stopTimer();
        }
        matchOver = true;

        String detailedScore = String.join(", ", setScores);

        Match match = new Match(
                player1Name,
                player2Name,
                player1Sets,
                player2Sets,
                winner,
                matchTitle,
                matchVenue,
                matchType,
                detailedScore,
                System.currentTimeMillis(),
                seconds
        );

        AppDatabase.databaseWriteExecutor.execute(() -> {
            long matchId = AppDatabase.getInstance(getApplicationContext()).matchDao().insert(match);
            runOnUiThread(() -> {
                if (isWinnerDeclared) {
                    Intent intent = new Intent(MainActivity.this, MatchSummaryActivity.class);
                    intent.putExtra("MATCH_ID", matchId);
                    startActivity(intent);
                    finish(); // Finish MainActivity after starting summary
                } else {
                    finish(); // Finish if match is ended manually without a winner
                }
            });
        });
    }


    private void startTimer() {
        seconds = 0;
        stopTimer();
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                seconds++;
                int hours = seconds / 3600, minutes = (seconds % 3600) / 60, secs = seconds % 60;
                tvTimer.setText(String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, secs));
                timerHandler.postDelayed(this, 1000);
            }
        };
        timerHandler.post(timerRunnable);
    }

    private void stopTimer() { if (timerRunnable != null) timerHandler.removeCallbacks(timerRunnable); }

    private void saveCurrentState() {
        history.push(new GameState(player1Points, player1Games, player1Sets,
                player2Points, player2Games, player2Sets,
                isPlayer1Serving, setScores));
    }

    private void undoLastPoint() {
        if (!history.isEmpty()) {
            GameState lastState = history.pop();
            player1Points = lastState.p1Points; player1Games = lastState.p1Games; player1Sets = lastState.p1Sets;
            player2Points = lastState.p2Points; player2Games = lastState.p2Games; player2Sets = lastState.p2Sets;
            isPlayer1Serving = lastState.p1Serving;
            setScores = new ArrayList<>(lastState.setScores);
            if (matchOver) { matchOver = false; startTimer(); }
            updateUI();
        }
    }

    @Override
    protected void onDestroy() { super.onDestroy(); stopTimer(); }
}
