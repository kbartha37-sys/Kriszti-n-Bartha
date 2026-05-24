package com.example

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.MatchPrediction
import com.example.ui.PredictState
import com.example.ui.PredictionViewModel
import com.example.network.MatchPredictionResult
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// --- Theme Colors ---
val EmeraldNightBackground = Color(0xFF090F0B)
val PitchDarkBase = Color(0xFF101912)
val EmeraldSurfaceCard = Color(0xFF16251A)
val PitchGreenAccent = Color(0xFF00E676)
val StadiumGoldAccent = Color(0xFFFFA000)
val LaserCrimson = Color(0xFFFF1744)
val ElectricWhite = Color(0xFFF1F5F2)
val SlateGrayText = Color(0xFF90A4AE)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val app = applicationContext as Application
            val viewModel: PredictionViewModel = viewModel(
                factory = PredictionViewModel.Factory(app)
            )

            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = PitchGreenAccent,
                    secondary = StadiumGoldAccent,
                    background = EmeraldNightBackground,
                    surface = EmeraldSurfaceCard,
                    onBackground = ElectricWhite,
                    onSurface = ElectricWhite
                )
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = EmeraldNightBackground
                ) {
                    MatchPredictorDashboard(viewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MatchPredictorDashboard(viewModel: PredictionViewModel) {
    val homeInput by viewModel.homeInput.collectAsStateWithLifecycle()
    val awayInput by viewModel.awayInput.collectAsStateWithLifecycle()
    val stadiumInput by viewModel.stadiumInput.collectAsStateWithLifecycle()
    val additionalInfoInput by viewModel.additionalInfoInput.collectAsStateWithLifecycle()
    val predictState by viewModel.predictState.collectAsStateWithLifecycle()
    val history by viewModel.history.collectAsStateWithLifecycle()
    val selectedPrediction by viewModel.selectedPrediction.collectAsStateWithLifecycle()

    val context = LocalContext.current

    // Popular Teams Suggestions
    val popularTeams = listOf(
        "Real Madrid", "Barcelona", "Manchester City", "Liverpool",
        "Paris SG", "Bayern München", "Arsenal", "Ferencváros"
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Main Scrollable Area
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(PitchDarkBase, EmeraldSurfaceCard)
                            )
                        )
                        .border(1.dp, PitchGreenAccent.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SportsSoccer,
                                contentDescription = "Síp",
                                tint = PitchGreenAccent,
                                modifier = Modifier.size(32.dp)
                            )
                            Text(
                                text = "MATCH PREDICTOR AI",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = ElectricWhite,
                                letterSpacing = 2.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Kerülj képbe a holnap meccsével! Intelligens labdarúgás végeredmény, gól, sárga és piros lap előrejelző asszisztens.",
                            fontSize = 13.sp,
                            color = SlateGrayText
                        )
                    }
                }

                // Input card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = EmeraldSurfaceCard)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "MECCS ADATOK BEVITELE",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = PitchGreenAccent,
                            letterSpacing = 1.sp
                        )

                        // Home Team Input
                        OutlinedTextField(
                            value = homeInput,
                            onValueChange = { viewModel.updateHomeInput(it) },
                            label = { Text("Hazai Csapat (pl. Real Madrid)") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("home_team_input"),
                            singleLine = true,
                            leadingIcon = {
                                Icon(Icons.Default.Home, contentDescription = null, tint = PitchGreenAccent)
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PitchGreenAccent,
                                unfocusedBorderColor = ElectricWhite.copy(alpha = 0.2f)
                            )
                        )

                        // Away Team Input
                        OutlinedTextField(
                            value = awayInput,
                            onValueChange = { viewModel.updateAwayInput(it) },
                            label = { Text("Vendég Csapat (pl. Barcelona)") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("away_team_input"),
                            singleLine = true,
                            leadingIcon = {
                                Icon(Icons.Default.DirectionsRun, contentDescription = null, tint = PitchGreenAccent)
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PitchGreenAccent,
                                unfocusedBorderColor = ElectricWhite.copy(alpha = 0.2f)
                            )
                        )

                        // Suggestions Row
                        Text(
                            text = "Kattints egy mintacsapatra a gyors kitöltéshez:",
                            fontSize = 11.sp,
                            color = SlateGrayText
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            popularTeams.forEach { team ->
                                SuggestionChip(
                                    onClick = {
                                        if (homeInput.isBlank()) {
                                            viewModel.updateHomeInput(team)
                                        } else if (awayInput.isBlank() && homeInput != team) {
                                            viewModel.updateAwayInput(team)
                                        } else {
                                            // overwrite home text
                                            viewModel.updateHomeInput(team)
                                        }
                                    },
                                    label = { Text(team, fontSize = 11.sp) },
                                    colors = SuggestionChipDefaults.suggestionChipColors(
                                        labelColor = ElectricWhite,
                                        containerColor = PitchDarkBase
                                    ),
                                    border = SuggestionChipDefaults.suggestionChipBorder(
                                        borderColor = PitchGreenAccent.copy(alpha = 0.3f),
                                        enabled = true
                                    ),
                                    modifier = Modifier.testTag("suggestion_chip_$team")
                                )
                            }
                        }

                        // Stadium Input
                        OutlinedTextField(
                            value = stadiumInput,
                            onValueChange = { viewModel.updateStadiumInput(it) },
                            label = { Text("Stadion / Helyszín (opcionális)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            leadingIcon = {
                                Icon(Icons.Default.Place, contentDescription = null, tint = SlateGrayText)
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PitchGreenAccent,
                                unfocusedBorderColor = ElectricWhite.copy(alpha = 0.2f)
                            )
                        )

                        // Additional Notes Input
                        OutlinedTextField(
                            value = additionalInfoInput,
                            onValueChange = { viewModel.updateAdditionalInfoInput(it) },
                            label = { Text("Kiegészítő információk, sérültek, időjárás...") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 3,
                            leadingIcon = {
                                Icon(Icons.Default.Info, contentDescription = null, tint = SlateGrayText)
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PitchGreenAccent,
                                unfocusedBorderColor = ElectricWhite.copy(alpha = 0.2f)
                            )
                        )

                        // Generate Button
                        Button(
                            onClick = { viewModel.predictMatch() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("generate_prediction_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PitchGreenAccent,
                                contentColor = EmeraldNightBackground
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "EREDMÉNY ÉS LAPOK JÓSLÁSA",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }

                // History Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "EDDIGI JÓSLATOK (${history.size})",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = StadiumGoldAccent
                    )
                    if (history.isNotEmpty()) {
                        TextButton(
                            onClick = { viewModel.clearHistory() },
                            colors = ButtonDefaults.textButtonColors(contentColor = LaserCrimson)
                        ) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Összes törlése", fontSize = 12.sp)
                        }
                    }
                }

                if (history.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(PitchDarkBase)
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = SlateGrayText.copy(alpha = 0.5f),
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = "Még nincsenek elmentett jóslataid.",
                                color = SlateGrayText,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Töltsd ki a fenti csapatokat és kattints az előrejelzés gombra az indításhoz!",
                                color = SlateGrayText.copy(alpha = 0.7f),
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        history.forEach { item ->
                            HistoryCard(
                                item = item,
                                onClick = { viewModel.selectPrediction(item) },
                                onDelete = { viewModel.deletePrediction(item) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(100.dp)) // padding for floating UI elements
            }

            // Global API Status Dialog overlay
            when (val state = predictState) {
                is PredictState.Loading -> {
                    Dialog(
                        onDismissRequest = {},
                        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = EmeraldSurfaceCard),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                CircularProgressIndicator(color = PitchGreenAccent)
                                Text(
                                    text = "Jósol a mesterséges intelligencia...",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = ElectricWhite
                                )
                                Text(
                                    text = "A Gemini elemzi a csapatok aktuális formáját, fiktív taktikai felállásait, a gól és lap statisztikákat...",
                                    fontSize = 12.sp,
                                    color = SlateGrayText,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
                is PredictState.Success -> {
                    // Map result to a MatchPrediction entity so we can view it in the exact same view details sheet
                    val listType = Types.newParameterizedType(List::class.java, String::class.java)
                    val adapter = Moshi.Builder().build().adapter<List<String>>(listType)
                    val keyEventsJsonString = adapter.toJson(state.result.keyEvents)

                    val tempPrediction = MatchPrediction(
                        homeTeam = homeInput.ifBlank { "Hazai Csapat" },
                        awayTeam = awayInput.ifBlank { "Vendég Csapat" },
                        stadium = stadiumInput,
                        additionalInfo = additionalInfoInput,
                        winner = state.result.winner,
                        homeScorePr = state.result.homeScorePr,
                        awayScorePr = state.result.awayScorePr,
                        homeYellowCards = state.result.homeYellowCards,
                        awayYellowCards = state.result.awayYellowCards,
                        homeRedCards = state.result.homeRedCards,
                        awayRedCards = state.result.awayRedCards,
                        predictionConfidence = state.result.predictionConfidence,
                        summary = state.result.summary,
                        keyEventsJson = keyEventsJsonString,
                        analysisText = state.result.analysisText
                    )

                    // Reset state and select prediction
                    LaunchedEffect(state) {
                        viewModel.clearStatus()
                        viewModel.selectPrediction(tempPrediction)
                    }
                }
                is PredictState.Error -> {
                    Dialog(onDismissRequest = { viewModel.clearStatus() }) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = EmeraldSurfaceCard),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Hiba",
                                    tint = LaserCrimson,
                                    modifier = Modifier.size(48.dp)
                                )
                                Text(
                                    text = "Hiba történt",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = ElectricWhite
                                )
                                Text(
                                    text = state.message,
                                    fontSize = 13.sp,
                                    color = SlateGrayText,
                                    textAlign = TextAlign.Center
                                )
                                Button(
                                    onClick = { viewModel.clearStatus() },
                                    colors = ButtonDefaults.buttonColors(containerColor = LaserCrimson),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Rendben", color = ElectricWhite)
                                }
                            }
                        }
                    }
                }
                PredictState.Idle -> { /* do nothing */ }
            }

            // Prediction Details Overlay Dialog
            selectedPrediction?.let { prediction ->
                PredictionDetailDialog(
                    prediction = prediction,
                    onDismiss = { viewModel.selectPrediction(null) }
                )
            }
        }
    }
}

@Composable
fun HistoryCard(
    item: MatchPrediction,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val dateString = SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault()).format(Date(item.timestamp))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("history_item_${item.id}"),
        colors = CardDefaults.cardColors(containerColor = PitchDarkBase),
        border = BorderStroke(1.dp, PitchGreenAccent.copy(alpha = 0.15f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = dateString,
                    fontSize = 10.sp,
                    color = SlateGrayText
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = item.homeTeam,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ElectricWhite,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Text(
                        text = "${item.homeScorePr} - ${item.awayScorePr}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = PitchGreenAccent
                    )
                    Text(
                        text = item.awayTeam,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ElectricWhite,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(StadiumGoldAccent.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${item.predictionConfidence}%",
                            color = StadiumGoldAccent,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = item.summary,
                        fontSize = 11.sp,
                        color = SlateGrayText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.testTag("delete_button_${item.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Törlés",
                    tint = LaserCrimson.copy(alpha = 0.6f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun PredictionDetailDialog(
    prediction: MatchPrediction,
    onDismiss: () -> Unit
) {
    // Deserialize events json string
    val listType = Types.newParameterizedType(List::class.java, String::class.java)
    val adapter = Moshi.Builder().build().adapter<List<String>>(listType)
    val keyEventsList = try {
        adapter.fromJson(prediction.keyEventsJson) ?: emptyList()
    } catch (e: Exception) {
        emptyList()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(EmeraldNightBackground),
            color = EmeraldNightBackground
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Toolbar Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = PitchGreenAccent
                        )
                        Text(
                            text = "AI MECCS ELEMZÉS",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = ElectricWhite
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(PitchDarkBase)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Bezárás", tint = ElectricWhite)
                    }
                }

                // Sports Scoreboard visualizer
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(PitchDarkBase, EmeraldSurfaceCard)
                            )
                        )
                        .border(1.dp, PitchGreenAccent.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                        .padding(20.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (prediction.stadium.isNotBlank()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.Place, contentDescription = null, tint = SlateGrayText, modifier = Modifier.size(12.dp))
                                Text(
                                    text = prediction.stadium.uppercase(),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = SlateGrayText,
                                    letterSpacing = 1.sp
                                )
                            }
                        }

                        // Big Score layout
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            // Home Team
                            Text(
                                text = prediction.homeTeam,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = ElectricWhite,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1f),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )

                            // Digital Scoreboard numbers
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 16.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.Black)
                                    .border(1.dp, PitchGreenAccent, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = "${prediction.homeScorePr} : ${prediction.awayScorePr}",
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = PitchGreenAccent,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    letterSpacing = 2.sp
                                )
                            }

                            // Away Team
                            Text(
                                text = prediction.awayTeam,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = ElectricWhite,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1f),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Probabilites & Confidence representation
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(StadiumGoldAccent.copy(alpha = 0.2f))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Verified,
                                        contentDescription = null,
                                        tint = StadiumGoldAccent,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "${prediction.predictionConfidence}% BIZTONSÁG",
                                        color = StadiumGoldAccent,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            val winLabel = when (prediction.winner.uppercase()) {
                                "HOME" -> "HAZAI GYŐZELEM 🏠"
                                "AWAY" -> "VENDÉG GYŐZELEM 🏃"
                                else -> "DÖNTETLEN 🤝"
                            }
                            val winColor = when (prediction.winner.uppercase()) {
                                "HOME", "AWAY" -> PitchGreenAccent
                                else -> SlateGrayText
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(winColor.copy(alpha = 0.2f))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = winLabel,
                                    color = winColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Divider(color = ElectricWhite.copy(alpha = 0.1f))

                        // Match overview sum slogan
                        Text(
                            text = prediction.summary,
                            color = ElectricWhite,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                }

                // Gols, Cards statistics visual graphics details
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = EmeraldSurfaceCard)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "VÁRHATÓ LAPOK ÉS FAKTOROK",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = PitchGreenAccent,
                            letterSpacing = 1.sp
                        )

                        // Cards graphic bar comparison
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Home Cards indicators
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Yellow card graphical symbol icon
                                Box(
                                    modifier = Modifier
                                        .size(20.dp, 28.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(Color(0xFFFFEA00)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = prediction.homeYellowCards.toString(),
                                        color = Color.Black,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 14.sp
                                    )
                                }
                                // Red card graphical symbol icon
                                Box(
                                    modifier = Modifier
                                        .size(20.dp, 28.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(LaserCrimson),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = prediction.homeRedCards.toString(),
                                        color = Color.White,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 14.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Hazai lapok",
                                    fontSize = 12.sp,
                                    color = SlateGrayText
                                )
                            }

                            Text(
                                text = "VS",
                                fontWeight = FontWeight.Bold,
                                color = SlateGrayText,
                                fontSize = 11.sp
                            )

                            // Away Cards indicators
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Vendég lapok",
                                    fontSize = 12.sp,
                                    color = SlateGrayText
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                // Yellow card
                                Box(
                                    modifier = Modifier
                                        .size(20.dp, 28.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(Color(0xFFFFEA00)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = prediction.awayYellowCards.toString(),
                                        color = Color.Black,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 14.sp
                                    )
                                }
                                // Red card
                                Box(
                                    modifier = Modifier
                                        .size(20.dp, 28.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(LaserCrimson),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = prediction.awayRedCards.toString(),
                                        color = Color.White,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }

                        // Additional notes info context indicator if available
                        if (prediction.additionalInfo.isNotBlank()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(PitchDarkBase)
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Tune, contentDescription = null, tint = StadiumGoldAccent, modifier = Modifier.size(16.dp))
                                Text(
                                    text = "Beállított finomhangolások: \"${prediction.additionalInfo}\"",
                                    fontSize = 11.sp,
                                    color = SlateGrayText
                                )
                            }
                        }
                    }
                }

                // Match Key Events Timeline
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = EmeraldSurfaceCard)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "MÉRKŐZÉS SZIMULÁLT ESEMÉNYEK",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = PitchGreenAccent,
                            letterSpacing = 1.sp
                        )

                        if (keyEventsList.isEmpty()) {
                            Text(
                                text = "Nincsenek részletes perc-események mentve.",
                                color = SlateGrayText,
                                fontSize = 12.sp
                            )
                        } else {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                keyEventsList.forEach { event ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Custom Icon based on trigger words in events
                                        val icon = when {
                                            event.contains("gól", ignoreCase = true) || event.contains("gol", ignoreCase = true) -> Icons.Default.SportsSoccer
                                            event.contains("sárga", ignoreCase = true) || event.contains("sarga", ignoreCase = true) -> Icons.Default.Style
                                            event.contains("piros", ignoreCase = true) -> Icons.Default.Flag
                                            else -> Icons.Default.HourglassEmpty
                                        }
                                        val tint = when {
                                            event.contains("gól", ignoreCase = true) || event.contains("gol", ignoreCase = true) -> PitchGreenAccent
                                            event.contains("sárga", ignoreCase = true) || event.contains("sarga", ignoreCase = true) -> Color(0xFFFFEA00)
                                            event.contains("piros", ignoreCase = true) -> LaserCrimson
                                            else -> SlateGrayText
                                        }

                                        Icon(
                                            imageVector = icon,
                                            contentDescription = null,
                                            tint = tint,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = event,
                                            fontSize = 13.sp,
                                            color = ElectricWhite
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // AI Expert Analysis Text Readout
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = EmeraldSurfaceCard)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Analytics,
                                contentDescription = null,
                                tint = PitchGreenAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "RÉSZLETES TAKTIKAI ELEMZÉS",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = PitchGreenAccent,
                                letterSpacing = 1.sp
                            )
                        }

                        Text(
                            text = prediction.analysisText,
                            fontSize = 13.sp,
                            lineHeight = 20.sp,
                            color = ElectricWhite.copy(alpha = 0.95f),
                            textAlign = TextAlign.Justify
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Bottom floating action close button
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = PitchDarkBase),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, PitchGreenAccent.copy(alpha = 0.5f))
                ) {
                    Text(text = "BEZÁRÁS", color = PitchGreenAccent, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
