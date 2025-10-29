package com.example.tennisscoring.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface MatchDao {

    @Insert
    long insert(Match match);

    @Delete
    void delete(Match match);

    @Query("SELECT * FROM matches ORDER BY timestamp DESC")
    List<Match> getAllMatches();

    @Query("DELETE FROM matches")
    void deleteAll();

    @Query("SELECT * FROM matches WHERE id = :matchId")
    Match getMatchById(long matchId);
}
