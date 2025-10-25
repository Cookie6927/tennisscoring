package com.example.tennisscoring.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "matches")
public class Match {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String player1Name;
    private String player2Name;
    private int player1Sets;
    private int player2Sets;
    private String winnerName;
    private String matchTitle;
    private String matchVenue;
    private String matchType;
    private String detailedScore; // New field for per-set scores
    private long timestamp;

    public Match(String player1Name, String player2Name, int player1Sets, int player2Sets, String winnerName, String matchTitle, String matchVenue, String matchType, String detailedScore, long timestamp) {
        this.player1Name = player1Name;
        this.player2Name = player2Name;
        this.player1Sets = player1Sets;
        this.player2Sets = player2Sets;
        this.winnerName = winnerName;
        this.matchTitle = matchTitle;
        this.matchVenue = matchVenue;
        this.matchType = matchType;
        this.detailedScore = detailedScore;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getPlayer1Name() { return player1Name; }
    public void setPlayer1Name(String player1Name) { this.player1Name = player1Name; }
    public String getPlayer2Name() { return player2Name; }
    public void setPlayer2Name(String player2Name) { this.player2Name = player2Name; }
    public int getPlayer1Sets() { return player1Sets; }
    public void setPlayer1Sets(int player1Sets) { this.player1Sets = player1Sets; }
    public int getPlayer2Sets() { return player2Sets; }
    public void setPlayer2Sets(int player2Sets) { this.player2Sets = player2Sets; }
    public String getWinnerName() { return winnerName; }
    public void setWinnerName(String winnerName) { this.winnerName = winnerName; }
    public String getMatchTitle() { return matchTitle; }
    public void setMatchTitle(String matchTitle) { this.matchTitle = matchTitle; }
    public String getMatchVenue() { return matchVenue; }
    public void setMatchVenue(String matchVenue) { this.matchVenue = matchVenue; }
    public String getMatchType() { return matchType; }
    public void setMatchType(String matchType) { this.matchType = matchType; }
    public String getDetailedScore() { return detailedScore; }
    public void setDetailedScore(String detailedScore) { this.detailedScore = detailedScore; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
