package com.example.tennisscoring;

import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tennisscoring.database.AppDatabase;
import com.example.tennisscoring.database.Match;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends BaseActivity {

    private RecyclerView recyclerView;
    private HistoryAdapter historyAdapter;
    private TextView tvEmptyHistory;
    private List<Match> matches = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        setupToolbar();

        recyclerView = findViewById(R.id.recycler_view_history);
        tvEmptyHistory = findViewById(R.id.tv_empty_history);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        historyAdapter = new HistoryAdapter(matches);
        recyclerView.setAdapter(historyAdapter);

        loadMatches();
        setupSwipeToDelete();
    }

    private void setupSwipeToDelete() {
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            private final ColorDrawable background = new ColorDrawable();

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false; // We don't want to handle move gestures
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                showDeleteConfirmationDialog(position);
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                View itemView = viewHolder.itemView;

                TypedValue typedValue = new TypedValue();
                HistoryActivity.this.getTheme().resolveAttribute(com.google.android.material.R.attr.colorPrimary, typedValue, true);
                int color = typedValue.data;

                background.setColor(color);
                background.setBounds(itemView.getLeft(), itemView.getTop(), itemView.getRight(), itemView.getBottom());
                background.draw(c);
            }
        }).attachToRecyclerView(recyclerView);
    }

    private void showDeleteConfirmationDialog(final int position) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Delete Match")
                .setMessage("Are you sure you want to delete this match from your history?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    deleteMatch(position);
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    historyAdapter.notifyItemChanged(position);
                })
                .setOnCancelListener(dialog -> {
                    historyAdapter.notifyItemChanged(position);
                })
                .show();
    }

    private void deleteMatch(int position) {
        Match matchToDelete = matches.get(position);
        AppDatabase.databaseWriteExecutor.execute(() -> {
            AppDatabase.getInstance(HistoryActivity.this).matchDao().delete(matchToDelete);
            runOnUiThread(() -> {
                matches.remove(position);
                historyAdapter.notifyItemRemoved(position);
                updateEmptyView();
            });
        });
    }

    private void loadMatches() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            matches.clear();
            matches.addAll(AppDatabase.getInstance(HistoryActivity.this).matchDao().getAllMatches());
            runOnUiThread(() -> {
                historyAdapter.notifyDataSetChanged();
                updateEmptyView();
            });
        });
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
        AppDatabase.databaseWriteExecutor.execute(() -> {
            AppDatabase.getInstance(HistoryActivity.this).matchDao().deleteAll();
            runOnUiThread(() -> {
                matches.clear();
                historyAdapter.notifyDataSetChanged();
                updateEmptyView();
            });
        });
    }
}
