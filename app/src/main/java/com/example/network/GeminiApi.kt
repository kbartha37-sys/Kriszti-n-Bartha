package com.example.network

import com.example.BuildConfig
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

// --- Gemini REST API Models ---

@JsonClass(generateAdapter = true)
data class GenerateContentRequest(
    val contents: List<Content>,
    val generationConfig: GenerationConfig? = null,
    val systemInstruction: Content? = null
)

@JsonClass(generateAdapter = true)
data class Content(
    val parts: List<Part>
)

@JsonClass(generateAdapter = true)
data class Part(
    val text: String? = null
)

@JsonClass(generateAdapter = true)
data class GenerationConfig(
    val responseMimeType: String? = null,
    val responseSchema: Schema? = null,
    val temperature: Float? = null
)

@JsonClass(generateAdapter = true)
data class Schema(
    val type: String,
    val properties: Map<String, Schema>? = null,
    val required: List<String>? = null,
    val items: Schema? = null,
    val description: String? = null
)

@JsonClass(generateAdapter = true)
data class GenerateContentResponse(
    val candidates: List<Candidate>?
)

@JsonClass(generateAdapter = true)
data class Candidate(
    val content: Content?
)

// --- Match Prediction Output Target Model ---

@JsonClass(generateAdapter = true)
data class MatchPredictionResult(
    val winner: String, // HOME, AWAY, DRAW
    val homeScorePr: Int,
    val awayScorePr: Int,
    val homeYellowCards: Int,
    val awayYellowCards: Int,
    val homeRedCards: Int,
    val awayRedCards: Int,
    val predictionConfidence: Int,
    val summary: String,
    val keyEvents: List<String>,
    val analysisText: String
)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

object RetrofitClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val service: GeminiApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
        retrofit.create(GeminiApiService::class.java)
    }
}

class GeminiRepository {
    private val service = RetrofitClient.service
    private val predictionAdapter = RetrofitClient.moshi.adapter(MatchPredictionResult::class.java)

    suspend fun getMatchPrediction(
        homeTeam: String,
        awayTeam: String,
        stadium: String,
        additionalInfo: String
    ): MatchPredictionResult? {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            throw IllegalStateException("A Gemini API Key hiányzik! Kérjük, állítsa be az AI Studio Secrets panelén.")
        }

        val prompt = """
            Jósold meg a következő labdarúgó mérkőzés kimenetelét!
            Hazai csapat: $homeTeam
            Vendég csapat: $awayTeam
            Helyszín/Stadion (opcionális): ${stadium.ifBlank { "Ismeretlen" }}
            További kontextus/infó (opcionális): ${additionalInfo.ifBlank { "Nincs megadva" }}

            Kérlek, végezz realisztikus előrejelzést a csapatok formája alapján, és add meg a pontos végeredményt (gólok), a várható sárga és piros lapokat mindkét csapatra, a győztest, a predictációs magabiztossági százalékot, egy rövid összefoglalót, a meccs kulcsfontosságú fiktív/előrejelzett eseményeit időrendben (ki szerzi a gólokat, hányadik percben, lapok stb.), valamint egy részletes taktikai elemzést.
            Minden szöveg és elemzés magyar nyelven készüljön!
        """.trimIndent()

        // Build Schema manually using typed Moshi schema objects
        val schema = Schema(
            type = "OBJECT",
            properties = mapOf(
                "winner" to Schema(type = "STRING", description = "A meccs győztese. Értéke szigorúan 'HOME', 'AWAY' vagy 'DRAW' lehet."),
                "homeScorePr" to Schema(type = "INTEGER", description = "A hazai csapat által szerzett gólok száma"),
                "awayScorePr" to Schema(type = "INTEGER", description = "A vendég csapat által szerzett gólok száma"),
                "homeYellowCards" to Schema(type = "INTEGER", description = "Várható sárga lapok száma a hazai csapatnál"),
                "awayYellowCards" to Schema(type = "INTEGER", description = "Várható sárga lapok száma a vendég csapatnál"),
                "homeRedCards" to Schema(type = "INTEGER", description = "Várható piros lapok száma a hazai csapatnál"),
                "awayRedCards" to Schema(type = "INTEGER", description = "Várható piros lapok száma a vendég csapatnál"),
                "predictionConfidence" to Schema(type = "INTEGER", description = "Az előrejelzés magabiztossága százalékban (0 és 100 között)"),
                "summary" to Schema(type = "STRING", description = "Rövid, frappáns összefoglaló mondat a mérkőzés várható hangulatáról és kimeneteléről magyarul."),
                "keyEvents" to Schema(
                    type = "ARRAY",
                    items = Schema(type = "STRING"),
                    description = "A virtuális mérkőzés eseményei perc szerint (pl. '12\' Mbappé gól', '45\' Ramos sárga lap') magyarul."
                ),
                "analysisText" to Schema(type = "STRING", description = "Részletes, szakmai hangvételű, taktikai elemzés a mérkőzésről magyarul.")
            ),
            required = listOf(
                "winner", "homeScorePr", "awayScorePr",
                "homeYellowCards", "awayYellowCards", "homeRedCards", "awayRedCards",
                "predictionConfidence", "summary", "keyEvents", "analysisText"
            )
        )

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            generationConfig = GenerationConfig(
                responseMimeType = "application/json",
                responseSchema = schema,
                temperature = 0.8f
            )
        )

        val response = service.generateContent(apiKey, request)
        val textResponse = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            ?: throw IllegalStateException("A mesterséges intelligencia nem küldött választ.")

        return predictionAdapter.fromJson(textResponse)
    }
}
