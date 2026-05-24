package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.MatchPrediction
import com.example.data.PredictionDatabase
import com.example.data.PredictionRepository
import com.example.network.GeminiRepository
import com.example.network.MatchPredictionResult
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface PredictState {
    object Idle : PredictState
    object Loading : PredictState
    data class Success(val result: MatchPredictionResult) : PredictState
    data class Error(val message: String) : PredictState
}

class PredictionViewModel(application: Application) : AndroidViewModel(application) {

    private val db = PredictionDatabase.getDatabase(application)
    private val repository = PredictionRepository(
        predictionDao = db.predictionDao(),
        geminiRepository = GeminiRepository()
    )

    // Form states
    var homeInput = MutableStateFlow("")
        private set
    var awayInput = MutableStateFlow("")
        private set
    var stadiumInput = MutableStateFlow("")
        private set
    var additionalInfoInput = MutableStateFlow("")
        private set

    // Status state
    private val _predictState = MutableStateFlow<PredictState>(PredictState.Idle)
    val predictState: StateFlow<PredictState> = _predictState.asStateFlow()

    // History list from database
    val history: StateFlow<List<MatchPrediction>> = repository.allPredictions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Currently focused prediction for detailed viewing
    private val _selectedPrediction = MutableStateFlow<MatchPrediction?>(null)
    val selectedPrediction: StateFlow<MatchPrediction?> = _selectedPrediction.asStateFlow()

    fun updateHomeInput(value: String) { homeInput.value = value }
    fun updateAwayInput(value: String) { awayInput.value = value }
    fun updateStadiumInput(value: String) { stadiumInput.value = value }
    fun updateAdditionalInfoInput(value: String) { additionalInfoInput.value = value }

    fun selectPrediction(prediction: MatchPrediction?) {
        _selectedPrediction.value = prediction
    }

    fun predictMatch() {
        val home = homeInput.value.trim()
        val away = awayInput.value.trim()

        if (home.isEmpty() || away.isEmpty()) {
            _predictState.value = PredictState.Error("A hazai és a vendég csapat nevét kötelező megadni!")
            return
        }

        viewModelScope.launch {
            _predictState.value = PredictState.Loading
            try {
                val result = repository.createAndSavePrediction(
                    homeTeam = home,
                    awayTeam = away,
                    stadium = stadiumInput.value.trim(),
                    additionalInfo = additionalInfoInput.value.trim()
                )
                _predictState.value = PredictState.Success(result)
                
                // Clear state inputs after prediction to allow typing new sport matches
                homeInput.value = ""
                awayInput.value = ""
                stadiumInput.value = ""
                additionalInfoInput.value = ""
            } catch (e: Exception) {
                _predictState.value = PredictState.Error(
                    e.message ?: "Ismeretlen hiba történt a jóslat előállítása során."
                )
            }
        }
    }

    fun deletePrediction(prediction: MatchPrediction) {
        viewModelScope.launch {
            repository.deletePrediction(prediction)
            if (_selectedPrediction.value?.id == prediction.id) {
                _selectedPrediction.value = null
            }
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearAllHistory()
        }
    }

    fun clearStatus() {
        _predictState.value = PredictState.Idle
    }

    // Factory
    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PredictionViewModel(application) as T
        }
    }
}
