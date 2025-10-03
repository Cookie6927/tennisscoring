package com.example.tennisscoring;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.tennisscoring.database.AppDatabase;
import com.example.tennisscoring.database.Match;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationView;

import java.util.Locale;
import java.util.Stack;
import java.util.Date;
import java.text.SimpleDateFormat;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    // Match Setup Data
    private String player1Name, player2Name, matchTitle, matchVenue;
    private int setsToWin;

    // UI Elements
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private Toolbar toolbar;
    private NavigationView navigationView;

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

    private boolean isPlayer1Serving = true;
    private boolean isDeuce = false;
    private boolean advantageP1 = false;
    private boolean advantageP2 = false;
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
        boolean p1Serving, isDeuceState, advP1, advP2;

        GameState(int p1p, int p1g, int p1s, int p2p, int p2g, int p2s, boolean p1Serv, boolean isDeuce, boolean adv1, boolean adv2) {
            this.p1Points = p1p; this.p1Games = p1g; this.p1Sets = p1s;
            this.p2Points = p2p; this.p2Games = p2g; this.p2Sets = p2s;
            this.p1Serving = p1Serv; this.isDeuceState = isDeuce;
            this.advP1 = adv1; this.advP2 = adv2;
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        retrieveMatchData();
        initializeUI();
        setupNavigationDrawer();
        setupClickListeners();
        resetMatch();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    // If the callback is enabled, simply call the super implementation
                    if (isEnabled()) {
                        setEnabled(false);
                        onBackPressed();
                    }
                }
            }
        });
    }

    private void retrieveMatchData() {
        Intent intent = getIntent();
        player1Name = intent.getStringExtra("PLAYER_1_NAME");
        player2Name = intent.getStringExtra("PLAYER_2_NAME");
        matchTitle = intent.getStringExtra("MATCH_TITLE");
        matchVenue = intent.getStringExtra("MATCH_VENUE");
        isPlayer1Serving = intent.getIntExtra("FIRST_SERVER", 1) == 1;
        setsToWin = intent.getIntExtra("SETS_TO_WIN", 2);
    }

    private void initializeUI() {
        toolbar = findViewById(R.id.toolbar);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
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

        tvPlayer1Name.setText(player1Name);
        tvPlayer2Name.setText(player2Name);

        btnAddPointP1 = findViewById(R.id.btn_add_point_p1);
        btnAddPointP1.setText("Add Point (" + player1Name + ")");
        btnAddPointP2 = findViewById(R.id.btn_add_point_p2);
        btnAddPointP2.setText("Add Point (" + player2Name + ")");

        btnUndo = findViewById(R.id.btn_undo);
        btnReset = findViewById(R.id.btn_reset);
        btnEnd = findViewById(R.id.btn_end_match);
    }

    private void setupNavigationDrawer() {
        setSupportActionBar(toolbar);
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
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
            showResetDialog();
        });

        btnEnd.setOnClickListener(v -> {
            HapticUtils.performHapticFeedback(this);
            new MaterialAlertDialogBuilder(this)
                .setTitle("End Match")
                .setMessage("Are you sure you want to end the current match?")
                .setPositiveButton("End Match", (dialog, which) -> endMatch())
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

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        HapticUtils.performHapticFeedback(this);
        if (id == R.id.nav_home) {
            finish();
        } else if (id == R.id.nav_history) {
            startActivity(new Intent(MainActivity.this, HistoryActivity.class));
        } else if (id == R.id.nav_settings) {
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
        } else if (id == R.id.nav_reset) {
            showResetDialog();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void addPoint(int player) {
        if (matchOver) return;
        saveCurrentState();
        if (isDeuce) {
            handleDeucePoint(player);
        } else {
            handleRegularPoint(player);
        }
        updateUI();
    }

    private void handleRegularPoint(int player) {
        if (player == 1) player1Points++;
        else player2Points++;

        if (player1Points >= 3 && player2Points >= 3) {
            if (player1Points == player2Points) isDeuce = true;
            else if (player1Points > 3) winGame(1);
            else if (player2Points > 3) winGame(2);
        } else if (player1Points > 3) {
            winGame(1);
        } else if (player2Points > 3) {
            winGame(2);
        }
    }

    private void handleDeucePoint(int player) {
        if (player == 1) {
            if (advantageP1) winGame(1);
            else if (advantageP2) { isDeuce = true; advantageP2 = false; }
            else { isDeuce = false; advantageP1 = true; }
        } else {
            if (advantageP2) winGame(2);
            else if (advantageP1) { isDeuce = true; advantageP1 = false; }
            else { isDeuce = false; advantageP2 = true; }
        }
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
        endMatch(); // Call endMatch to save the result
        new MaterialAlertDialogBuilder(this)
                .setTitle("Match Over!")
                .setMessage(winner + " wins the match!")
                .setPositiveButton("OK", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }

    private void updateUI() {
        if(advantageP1) { tvPlayer1Points.setText("AD"); tvPlayer2Points.setText("40"); }
        else if (advantageP2) { tvPlayer2Points.setText("AD"); tvPlayer1Points.setText("40"); }
        else if (isDeuce) { tvPlayer1Points.setText("40"); tvPlayer2Points.setText("40"); }
        else {
            tvPlayer1Points.setText(getPointString(player1Points));
            tvPlayer2Points.setText(getPointString(player2Points));
        }

        tvPlayer1Games.setText(String.valueOf(player1Games));
        tvPlayer1Sets.setText(String.valueOf(player1Sets));
        tvPlayer2Games.setText(String.valueOf(player2Games));
        tvPlayer2Sets.setText(String.valueOf(player2Sets));

        serverIndicatorP1.setVisibility(isPlayer1Serving ? View.VISIBLE : View.INVISIBLE);
        serverIndicatorP2.setVisibility(!isPlayer1Serving ? View.VISIBLE : View.INVISIBLE);
        btnUndo.setEnabled(!history.isEmpty());
    }

    private String getPointString(int point) {
        switch (point) {
            case 0: return "0"; case 1: return "15"; case 2: return "30"; case 3: return "40";
            default: return "";
        }
    }

    private void resetPoints() {
        player1Points = 0; player2Points = 0; isDeuce = false;
        advantageP1 = false; advantageP2 = false;
    }

    private void resetGames() { player1Games = 0; player2Games = 0; }

    private void resetMatch() {
        resetPoints(); resetGames();
        player1Sets = 0; player2Sets = 0;
        matchOver = false; history.clear();
        startTimer();
        updateUI();
    }

    private void endMatch() {
        if (matchOver) {
            stopTimer(); // Stop timer immediately
        }
        matchOver = true;

        // Correctly determine winner
        String winner;
        if (player1Sets > player2Sets) {
            winner = player1Name;
        } else if (player2Sets > player1Sets) {
            winner = player2Name;
        } else { // If sets are tied (e.g., manual end), decide by games
            winner = (player1Games > player2Games) ? player1Name : player2Name;
        }

        // Correctly ordered constructor call
        Match match = new Match(
                player1Name,
                player2Name,
                player1Sets,
                player2Sets,
                winner,
                matchTitle,
                matchVenue,
                System.currentTimeMillis()
        );

        new Thread(() -> AppDatabase.getInstance(getApplicationContext()).matchDao().insert(match)).start();

        if (!isFinishing()) {
            finish();
        }
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
                isPlayer1Serving, isDeuce, advantageP1, advantageP2));
    }

    private void undoLastPoint() {
        if (!history.isEmpty()) {
            GameState lastState = history.pop();
            player1Points = lastState.p1Points; player1Games = lastState.p1Games; player1Sets = lastState.p1Sets;
            player2Points = lastState.p2Points; player2Games = lastState.p2Games; player2Sets = lastState.p2Sets;
            isPlayer1Serving = lastState.p1Serving; isDeuce = lastState.isDeuceState;
            advantageP1 = lastState.advP1; advantageP2 = lastState.advP2;
            if (matchOver) { matchOver = false; startTimer(); }
            updateUI();
        }
    }

    @Override
    protected void onDestroy() { super.onDestroy(); stopTimer(); }
}
