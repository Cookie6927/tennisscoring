package com.example.tennisscoring;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tennisscoring.database.AppDatabase;
import com.example.tennisscoring.database.Match;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class HistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private HistoryAdapter historyAdapter;
    private TextView tvEmptyHistory;
    private List<Match> matches = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Match History");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerView = findViewById(R.id.recycler_view_history);
        tvEmptyHistory = findViewById(R.id.tv_empty_history);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        historyAdapter = new HistoryAdapter(matches);
        recyclerView.setAdapter(historyAdapter);

        loadMatches();
    }

    private void loadMatches() {
        new Thread(() -> {
            matches.clear();
            matches.addAll(AppDatabase.getInstance(this).matchDao().getAllMatches());
            runOnUiThread(() -> {
                historyAdapter.notifyDataSetChanged();
                updateEmptyView();
            });
        }).start();
    }

    private void updateEmptyView() {
        if (matches.isEmpty()) {
            tvEmptyHistory.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvEmptyHistory.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.history_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_clear_history) {
            showClearHistoryDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showClearHistoryDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Clear History")
                .setMessage("Are you sure you want to delete all match history? This action cannot be undone.")
                .setPositiveButton("Clear", (dialog, which) -> clearHistory())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void clearHistory() {
        new Thread(() -> {
            AppDatabase.getInstance(this).matchDao().deleteAll();
            runOnUiThread(() -> {
                matches.clear();
                historyAdapter.notifyDataSetChanged();
                updateEmptyView();
            });
        }).start();
    }

    @Override
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return true;
    }
}
