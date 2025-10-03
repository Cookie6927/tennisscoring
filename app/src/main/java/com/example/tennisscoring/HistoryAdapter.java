package com.example.tennisscoring;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tennisscoring.database.Match;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    private List<Match> matches;

    public HistoryAdapter(List<Match> matches) {
        // Reverse the list to show the most recent match first
        Collections.reverse(matches);
        this.matches = matches;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_match_history, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        Match match = matches.get(position);
        holder.playerNames.setText(match.getPlayer1Name() + " vs " + match.getPlayer2Name());
        holder.matchScore.setText("Winner: " + match.getWinnerName() + " (" + match.getPlayer1Sets() + " - " + match.getPlayer2Sets() + ")");
        holder.matchTitle.setText(match.getMatchTitle() + " - " + match.getMatchVenue());

        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault());
        holder.matchDate.setText(sdf.format(new Date(match.getTimestamp())));

        // Add animation
        Animation animation = AnimationUtils.loadAnimation(holder.itemView.getContext(), R.anim.fade_in);
        holder.itemView.startAnimation(animation);
    }

    @Override
    public int getItemCount() {
        return matches.size();
    }

    static class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView playerNames, matchScore, matchTitle, matchDate;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            playerNames = itemView.findViewById(R.id.tv_player_names);
            matchScore = itemView.findViewById(R.id.tv_match_score);
            matchTitle = itemView.findViewById(R.id.tv_match_title_venue);
            matchDate = itemView.findViewById(R.id.tv_match_date);
        }
    }
}
