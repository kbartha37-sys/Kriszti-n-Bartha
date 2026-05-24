package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PredictionDao {
    @Query("SELECT * FROM match_predictions ORDER BY timestamp DESC")
    fun getAllPredictions(): Flow<List<MatchPrediction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrediction(prediction: MatchPrediction): Long

    @Delete
    suspend fun deletePrediction(prediction: MatchPrediction)

    @Query("DELETE FROM match_predictions")
    suspend fun clearHistory()
}
