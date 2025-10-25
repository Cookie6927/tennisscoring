package com.example.tennisscoring.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface MatchDao {

    @Insert
    void insert(Match match);

    @Query("SELECT * FROM matches ORDER BY timestamp DESC")
    List<Match> getAllMatches();

    @Query("DELETE FROM matches")
    void deleteAll();
}
