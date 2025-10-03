package com.example.tennisscoring;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tennisscoring.database.AppDatabase;
import com.example.tennisscoring.database.Match;

import java.util.List;
import java.util.Objects;

public class HistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private HistoryAdapter historyAdapter;
    private TextView tvEmptyHistory;

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

        loadMatches();
    }

    private void loadMatches() {
        new Thread(() -> {
            List<Match> matches = AppDatabase.getInstance(this).matchDao().getAllMatches();
            runOnUiThread(() -> {
                if (matches.isEmpty()) {
                    tvEmptyHistory.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                } else {
                    tvEmptyHistory.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    historyAdapter = new HistoryAdapter(matches);
                    recyclerView.setAdapter(historyAdapter);
                }
            });
        }).start();
    }

    @Override
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return true;
    }
}
