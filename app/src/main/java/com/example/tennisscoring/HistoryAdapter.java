package com.example.tennisscoring;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.format.DateUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tennisscoring.database.Match;
import com.google.android.material.chip.Chip;

import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    private final List<Match> matches;
    private static final String TAG = "HistoryAdapter";

    public HistoryAdapter(List<Match> matches) {
        this.matches = matches;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_match_history, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        Match match = matches.get(position);
        holder.bind(match);

        holder.itemView.setOnClickListener(v -> {
            Log.d(TAG, "Item clicked. Match ID: " + match.getId());
            Context context = v.getContext();
            Intent intent = new Intent(context, MatchSummaryActivity.class);
            intent.putExtra("MATCH_ID", match.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return matches.size();
    }

    static class HistoryViewHolder extends RecyclerView.ViewHolder {

        private final Chip chipMatchType;
        private final TextView tvPlayerNames;
        private final TextView tvMatchScore;
        private final TextView tvMatchDate;
        private final TextView tvMatchVenue;
        private final TextView tvMatchTitle;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            chipMatchType = itemView.findViewById(R.id.chip_match_type);
            tvPlayerNames = itemView.findViewById(R.id.tv_player_names);
            tvMatchScore = itemView.findViewById(R.id.tv_match_score);
            tvMatchDate = itemView.findViewById(R.id.tv_match_date);
            tvMatchVenue = itemView.findViewById(R.id.tv_match_venue);
            tvMatchTitle = itemView.findViewById(R.id.tv_match_title);
        }

        public void bind(Match match) {
            Context context = itemView.getContext();

            if (match.getMatchType() != null && !match.getMatchType().isEmpty()) {
                chipMatchType.setText(match.getMatchType());
                chipMatchType.setVisibility(View.VISIBLE);
            } else {
                chipMatchType.setVisibility(View.GONE);
            }

            if (match.getMatchVenue() != null && !match.getMatchVenue().isEmpty()) {
                tvMatchVenue.setText(match.getMatchVenue());
                tvMatchVenue.setVisibility(View.VISIBLE);
            } else {
                tvMatchVenue.setVisibility(View.GONE);
            }

            if (match.getMatchTitle() != null && !match.getMatchTitle().isEmpty()) {
                tvMatchTitle.setText(match.getMatchTitle());
                tvMatchTitle.setVisibility(View.VISIBLE);
            } else {
                tvMatchTitle.setVisibility(View.GONE);
            }

            tvMatchDate.setText(DateUtils.getRelativeTimeSpanString(match.getTimestamp(), System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS));

            String p1Name = match.getPlayer1Name() != null ? match.getPlayer1Name() : "Player 1";
            String p2Name = match.getPlayer2Name() != null ? match.getPlayer2Name() : "Player 2";
            String playerNamesString = p1Name + " vs " + p2Name;
            Spannable coloredPlayerNames = new SpannableString(playerNamesString);

            String winnerName = match.getWinnerName();

            TypedValue typedValue = new TypedValue();
            context.getTheme().resolveAttribute(com.google.android.material.R.attr.colorPrimary, typedValue, true);
            int winnerColor = typedValue.data;

            if (winnerName != null) {
                if (winnerName.equals(p1Name)) {
                    coloredPlayerNames.setSpan(new ForegroundColorSpan(winnerColor), 0, p1Name.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                } else if (winnerName.equals(p2Name)) {
                    int p2NameStartIndex = playerNamesString.lastIndexOf(p2Name);
                    if (p2NameStartIndex != -1) {
                        coloredPlayerNames.setSpan(new ForegroundColorSpan(winnerColor), p2NameStartIndex, playerNamesString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                }
            }
            tvPlayerNames.setText(coloredPlayerNames);

            String detailedScore = match.getDetailedScore();
            if (detailedScore != null && !detailedScore.isEmpty()) {
                SpannableStringBuilder scoreBuilder = new SpannableStringBuilder();
                String[] setScores = detailedScore.split(", ");

                for (int i = 0; i < setScores.length; i++) {
                    String setScore = setScores[i];
                    String[] points = setScore.split("-");
                    if (points.length == 2) {
                        try {
                            int p1Games = Integer.parseInt(points[0].trim());
                            int p2Games = Integer.parseInt(points[1].trim());

                            SpannableString spannableSetScore = new SpannableString(setScore);
                            if (winnerName != null) {
                                if (winnerName.equals(p1Name) && p1Games > p2Games) {
                                    spannableSetScore.setSpan(new ForegroundColorSpan(winnerColor), 0, points[0].length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                } else if (winnerName.equals(p2Name) && p2Games > p1Games) {
                                    spannableSetScore.setSpan(new ForegroundColorSpan(winnerColor), points[0].length() + 1, setScore.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                }
                            }
                            scoreBuilder.append(spannableSetScore);

                        } catch (NumberFormatException e) {
                            scoreBuilder.append(setScore);
                        }
                    } else {
                        scoreBuilder.append(setScore);
                    }

                    if (i < setScores.length - 1) {
                        scoreBuilder.append(", ");
                    }
                }
                tvMatchScore.setText(scoreBuilder);
            } else {
                tvMatchScore.setText(match.getPlayer1Sets() + "-" + match.getPlayer2Sets());
            }
        }
    }
}
