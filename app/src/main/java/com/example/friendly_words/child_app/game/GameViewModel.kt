package com.example.friendly_words.child_app.game

import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.friendly_words.child_app.data.GameRound
import com.example.friendly_words.child_app.data.generateGameRounds
import com.example.shared.data.entities.LearningSettings
import com.example.shared.data.entities.asRoundSettings
import com.example.shared.data.entities.toLearningSettings
import com.example.shared.data.repositories.ConfigurationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import jakarta.inject.Inject

@HiltViewModel
class GameViewModel @Inject constructor(
    private val configurationRepository: ConfigurationRepository,
) : ViewModel() {

    var isTestMode = mutableStateOf(false)

    var rounds = mutableStateOf<List<GameRound>>(emptyList())
    var currentRoundIndex = mutableStateOf(0)
    var correctAnswersCount = mutableStateOf(0)
    var wrongAnswersCount = mutableStateOf(0)
    var repeatStage = mutableStateOf(0)

    var showCongratsScreen = mutableStateOf(false)
    var goNextAfterCongrats = mutableStateOf(false)
    var answerJudged = mutableStateOf(false)
    var correctClicked = mutableStateOf(false)
    var hadMistakeThisRound = mutableStateOf(false)
    var showHint = mutableStateOf(false)

    var activeLearningSettings = mutableStateOf<LearningSettings?>(null)
    var activeTestSettings = mutableStateOf<com.example.shared.data.entities.TestSettings?>(null)

    val dimIncorrect: State<Boolean> = derivedStateOf {
        activeLearningSettings.value?.typesOfHints?.contains("Wyszarz niepoprawne") == true
    }
    val scaleCorrect: State<Boolean> = derivedStateOf {
        activeLearningSettings.value?.typesOfHints?.contains("Powiększ poprawną") == true
    }
    val animateCorrect: State<Boolean> = derivedStateOf {
        activeLearningSettings.value?.typesOfHints?.contains("Porusz poprawną") == true
    }
    val outlineCorrect: State<Boolean> = derivedStateOf {
        activeLearningSettings.value?.typesOfHints?.contains("Obramuj poprawną") == true
    }
    val animationsEnabled: State<Boolean> = derivedStateOf {
        activeLearningSettings.value?.animationsEnabled == true
    }
    val shouldReinforce = mutableStateOf(false)

    val commandType: State<String> = derivedStateOf {
        if (isTestMode.value) {
            when (activeTestSettings.value?.commandType) {
                "SHORT" -> "{Słowo}"
                "WHERE_IS" -> "Gdzie jest {Słowo}"
                "SHOW_ME" -> "Pokaż gdzie jest {Słowo}"
                else -> "{Słowo}"
            }
        } else {
            when (activeLearningSettings.value?.commandType) {
                "SHORT" -> "{Słowo}"
                "WHERE_IS" -> "Gdzie jest {Słowo}"
                "SHOW_ME" -> "Pokaż gdzie jest {Słowo}"
                else -> "{Słowo}"
            }
        }
    }

    // ---------- HISTORIA POZYCJI POPRAWNEJ OPCJI (do pseudolosowania) ----------
    private val correctPosHistory = mutableMapOf<String, MutableSet<Int>>()

    private fun keyFor(round: GameRound): String = round.correctItem.label

    fun noteCorrectPos(round: GameRound) {
        val idx = round.options.indexOf(round.correctItem)
        if (idx >= 0) {
            val key = keyFor(round)
            correctPosHistory.getOrPut(key) { mutableSetOf() }.add(idx)
        }
    }

    fun shuffledRoundAvoidingPrevious(round: GameRound, displayedImages: Int): GameRound {
        if (displayedImages <= 1) return round

        val key = keyFor(round)
        val avoid = correctPosHistory[key] ?: emptySet()

        val options = round.options
        val correct = round.correctItem

        if (avoid.size >= options.size) {
            return round.copy(options = options.shuffled())
        }

        var best = options.shuffled()
        repeat(50) {
            val candidate = options.shuffled()
            val idx = candidate.indexOf(correct)
            if (idx !in avoid) {
                best = candidate
                return@repeat
            }
        }
        return round.copy(options = best)
    }

    init {
        viewModelScope.launch {
            configurationRepository.getActiveConfiguration().collect { config ->
                println("CONFIG FROM REPO: $config")

                if (config != null) {
                    isTestMode.value = (config.activeMode == "test")

                    val materialState = configurationRepository.getMaterialState(config.id)
                    val reinforcementState = configurationRepository.getReinforcementState(config.id)

                    activeLearningSettings.value = config.toLearningSettings(
                        materialState = materialState,
                        reinforcementState = reinforcementState
                    )

                    activeTestSettings.value = config.testSettings

                    val roundSettings = if (isTestMode.value) {
                        activeTestSettings.value?.asRoundSettings()
                    } else {
                        activeLearningSettings.value?.asRoundSettings()
                    }

                    rounds.value = if (roundSettings != null) {
                        generateGameRounds(configurationRepository, roundSettings, isTestMode.value)
                    } else emptyList()
                } else {
                    println("NO ACTIVE CONFIGURATION!")
                }

            }
        }
    }

    // ---------- FUNKCJA PRZEJŚCIA DO KOLEJNEJ RUNDY ----------
    fun goToNext(onGameFinished: (correct: Int, wrong: Int) -> Unit) {
        if (currentRoundIndex.value < rounds.value.lastIndex) {
            currentRoundIndex.value++
        } else {
            onGameFinished(correctAnswersCount.value, wrongAnswersCount.value)
            correctAnswersCount.value = 0
            wrongAnswersCount.value = 0
            currentRoundIndex.value = 0
            repeatStage.value = 0
            viewModelScope.launch {
                val roundSettings = if (isTestMode.value) {
                    activeTestSettings.value?.asRoundSettings()
                } else {
                    activeLearningSettings.value?.asRoundSettings()
                }
                rounds.value = if (roundSettings != null) {
                    generateGameRounds(configurationRepository, roundSettings, isTestMode.value)
                } else emptyList()
            }
        }

        answerJudged.value = false
        correctClicked.value = false
        hadMistakeThisRound.value = false
        showCongratsScreen.value = false
        goNextAfterCongrats.value = false
        showHint.value = false
        shouldReinforce.value = false
    }
}
