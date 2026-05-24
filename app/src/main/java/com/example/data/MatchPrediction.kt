package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.squareup.moshi.Types

@Entity(tableName = "match_predictions")
data class MatchPrediction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val homeTeam: String,
    val awayTeam: String,
    val stadium: String,
    val additionalInfo: String,
    val winner: String, // HOME, AWAY, DRAW
    val homeScorePr: Int,
    val awayScorePr: Int,
    val homeYellowCards: Int,
    val awayYellowCards: Int,
    val homeRedCards: Int,
    val awayRedCards: Int,
    val predictionConfidence: Int,
    val summary: String,
    val keyEventsJson: String, // List of key events serialized as JSON
    val analysisText: String,
    val timestamp: Long = System.currentTimeMillis()
)

class DataConverters {
    private val moshi = com.squareup.moshi.Moshi.Builder().build()
    private val listType = Types.newParameterizedType(List::class.java, String::class.java)
    private val adapter = moshi.adapter<List<String>>(listType)

    @TypeConverter
    fun fromStringList(value: List<String>?): String {
        return adapter.toJson(value ?: emptyList())
    }

    @TypeConverter
    fun toStringList(value: String?): List<String> {
        if (value.isNullOrBlank()) return emptyList()
        return try {
            adapter.fromJson(value) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}
