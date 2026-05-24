package com.example.data

import com.example.network.GeminiRepository
import com.example.network.MatchPredictionResult
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.flow.Flow

class PredictionRepository(
    private val predictionDao: PredictionDao,
    private val geminiRepository: GeminiRepository
) {
    val allPredictions: Flow<List<MatchPrediction>> = predictionDao.getAllPredictions()

    suspend fun createAndSavePrediction(
        homeTeam: String,
        awayTeam: String,
        stadium: String,
        additionalInfo: String
    ): MatchPredictionResult {
        // 1. Query Gemini API
        val result = geminiRepository.getMatchPrediction(
            homeTeam = homeTeam,
            awayTeam = awayTeam,
            stadium = stadium,
            additionalInfo = additionalInfo
        ) ?: throw IllegalStateException("Nem sikerült előállítani az elemzést.")

        // 2. Map Result to Database Entity
        val listType = Types.newParameterizedType(List::class.java, String::class.java)
        val adapter = Moshi.Builder().build().adapter<List<String>>(listType)
        val keyEventsJsonString = adapter.toJson(result.keyEvents)

        val predictionEntity = MatchPrediction(
            homeTeam = homeTeam,
            awayTeam = awayTeam,
            stadium = stadium,
            additionalInfo = additionalInfo,
            winner = result.winner,
            homeScorePr = result.homeScorePr,
            awayScorePr = result.awayScorePr,
            homeYellowCards = result.homeYellowCards,
            awayYellowCards = result.awayYellowCards,
            homeRedCards = result.homeRedCards,
            awayRedCards = result.awayRedCards,
            predictionConfidence = result.predictionConfidence,
            summary = result.summary,
            keyEventsJson = keyEventsJsonString,
            analysisText = result.analysisText
        )

        // 3. Save to Local Room DB
        predictionDao.insertPrediction(predictionEntity)

        return result
    }

    suspend fun deletePrediction(prediction: MatchPrediction) {
        predictionDao.deletePrediction(prediction)
    }

    suspend fun clearAllHistory() {
        predictionDao.clearHistory()
    }
}
