package com.example.friendly_words.child_app.game

import android.speech.tts.TextToSpeech
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.friendly_words.R
import com.example.friendly_words.child_app.components.RoundOptionsLayout
import com.example.friendly_words.child_app.data.GameItem
import com.example.friendly_words.child_app.theme.Blue
import kotlinx.coroutines.delay
import java.util.*

@Composable
fun GameScreen(
    viewModel: GameViewModel,
    onGameFinished: (correct: Int, wrong: Int) -> Unit
) {
    val currentRoundIndex by viewModel.currentRoundIndex
    val rounds by viewModel.rounds
    val showHint by viewModel.showHint
    val isTest = viewModel.isTestMode.value
    val shouldReinforce by viewModel.shouldReinforce


    if (rounds.isEmpty() || currentRoundIndex >= rounds.size) return

    val animationsEnabled by viewModel.animationsEnabled
    val currentRound = rounds[currentRoundIndex]
    val correctItem = currentRound.correctItem
    val commandType = viewModel.commandType.value
    val commandText = commandType.replace("{Słowo}", correctItem.label)
    val roundTimeoutMillis =
        ((viewModel.activeLearningSettings.value?.hintAfterSeconds ?: 1) * 1000L)

    // ===== Parametry zależne od trybu =====
    val displayedImages = if (isTest)
        viewModel.activeTestSettings.value?.displayedImagesCount ?: 4
    else
        viewModel.activeLearningSettings.value?.displayedImagesCount ?: 4

    val showLabels = if (isTest)
        viewModel.activeTestSettings.value?.showLabelsUnderImages ?: false
    else
        viewModel.activeLearningSettings.value?.showLabelsUnderImages ?: true

    val readCommand = if (isTest)
        viewModel.activeTestSettings.value?.readCommand ?: false
    else
        viewModel.activeLearningSettings.value?.readCommand ?: true

    // ===== TTS =====
    val context = LocalContext.current
    var ttsReady by remember { mutableStateOf(false) }
    var currentPraise by remember { mutableStateOf("") }
    val tts = remember {
        TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) ttsReady = true
        }
    }
    LaunchedEffect(ttsReady) { if (ttsReady) tts.language = Locale("pl", "PL") }

    // ---------- START NOWEJ RUNDY ----------
    LaunchedEffect(currentRoundIndex, ttsReady) {
        viewModel.noteCorrectPos(currentRound)

        // Czytanie polecenia tylko gdy włączone
        if (ttsReady && readCommand) {
            tts.speak(commandText, TextToSpeech.QUEUE_FLUSH, null, null)
        }

        // reset stanu
        viewModel.answerJudged.value = false
        viewModel.correctClicked.value = false
        viewModel.hadMistakeThisRound.value = false
        viewModel.showCongratsScreen.value = false
        viewModel.goNextAfterCongrats.value = false
        viewModel.showHint.value = false

        if (!isTest && roundTimeoutMillis > 0L) {
            delay(roundTimeoutMillis)
            if (!viewModel.correctClicked.value &&
                !viewModel.answerJudged.value &&
                !viewModel.showHint.value
            ) {
                viewModel.showHint.value = true
            }
        }else if (isTest && roundTimeoutMillis > 0L) {
            delay(roundTimeoutMillis)
            if (!viewModel.correctClicked.value && !viewModel.answerJudged.value) {
                viewModel.wrongAnswersCount.value++
                viewModel.answerJudged.value = true
                viewModel.goNextAfterCongrats.value = true
            }
        }
    }

    DisposableEffect(Unit) { onDispose { tts.stop(); tts.shutdown() } }

    // ---------- LICZENIE BŁĘDÓW PO POKAZANIU PODPOWIEDZI ----------
    LaunchedEffect(showHint) {
        if (!isTest && showHint && !viewModel.hadMistakeThisRound.value && !viewModel.correctClicked.value) {
            viewModel.wrongAnswersCount.value++
            viewModel.hadMistakeThisRound.value = true
            viewModel.answerJudged.value = true
        }
    }

    // ---------- OBSŁUGA KLIKNIĘĆ ----------
    fun handleAnswer(item: GameItem) {
        if (viewModel.showCongratsScreen.value) return

        val isCorrectClick = item == correctItem

        if (isCorrectClick && !viewModel.correctClicked.value) {
            viewModel.correctClicked.value = true

            val noMistake = !viewModel.hadMistakeThisRound.value && !viewModel.showHint.value
            viewModel.shouldReinforce.value = noMistake

            if (!viewModel.hadMistakeThisRound.value) viewModel.correctAnswersCount.value++

            if (isTest) {
                viewModel.goNextAfterCongrats.value = true
            } else {
                viewModel.showCongratsScreen.value = true
            }
            viewModel.answerJudged.value = true
        } else if (!isCorrectClick) {
            if (!isTest && !viewModel.showHint.value) {
                viewModel.showHint.value = true
            }
        }
    }

    // ---------- EKRAN GRATULACJI (tylko UCZENIE) ----------
    if (!isTest && viewModel.showCongratsScreen.value) {
        LaunchedEffect(viewModel.showCongratsScreen.value) {
            if (viewModel.showCongratsScreen.value) {
                val praises = viewModel.activeLearningSettings.value?.typesOfPraises ?: emptyList()
                currentPraise = praises.randomOrNull().orEmpty()
            }
        }

        val spritesFlowers = listOf(
            R.drawable.flower_orange,
            R.drawable.flower_pink,
            R.drawable.flower_purple
        )

        val spritesBalloons = listOf(
            R.drawable.balloon_blue,
            R.drawable.balloon_yellow,
            R.drawable.balloon_green,
            R.drawable.balloon_red
        )

        val spritesCars = listOf(
            R.drawable.car_red,
            R.drawable.car_yellow,
            R.drawable.car_green
        )

        val allSpriteSets = listOf(
            spritesFlowers to TravelDirection.UP,
            spritesBalloons to TravelDirection.UP,
            spritesCars to TravelDirection.RIGHT
        )

        val (chosenSprites, chosenDirection) = if (animationsEnabled && shouldReinforce) {
            allSpriteSets.random()
        } else (emptyList<Int>() to TravelDirection.UP)

        CorrectAnswerScreen(
            correctItem = correctItem,
            displayWord = correctItem.label,
            speakWordAndPraise = {
                if (ttsReady) {
                    tts.speak(correctItem.label, TextToSpeech.QUEUE_FLUSH, null, "word")
                    if (shouldReinforce && currentPraise.isNotBlank()) {
                        tts.speak(currentPraise, TextToSpeech.QUEUE_ADD, null, "praise")
                    }
                }
            },
            onTimeout = {
                viewModel.goNextAfterCongrats.value = true
                val roundsList = rounds.toMutableList()

                when (viewModel.repeatStage.value) {
                    0 -> {
                        if (viewModel.hadMistakeThisRound.value) {
                            // powtórka - ten sam układ
                            roundsList.add(currentRoundIndex + 1, currentRound)
                            viewModel.rounds.value = roundsList
                            viewModel.repeatStage.value = 1
                        }
                    }
                    1 -> {
                        if (viewModel.hadMistakeThisRound.value) {
                            // powtórka - ten sam układ
                            roundsList.add(currentRoundIndex + 1, currentRound)
                            viewModel.rounds.value = roundsList
                        } else {
                            // powtórka - pseudolosowy inny układ (unikamy poprzednich pozycji poprawnej)
                            val displayed = viewModel.activeLearningSettings.value?.displayedImagesCount ?: 4
                            val shuffledRound = viewModel.shuffledRoundAvoidingPrevious(currentRound, displayed)
                            roundsList.add(currentRoundIndex + 1, shuffledRound)
                            viewModel.rounds.value = roundsList
                            viewModel.repeatStage.value = 2
                        }
                    }
                    2 -> {
                        if (viewModel.hadMistakeThisRound.value) {
                            val displayed = viewModel.activeLearningSettings.value?.displayedImagesCount ?: 4
                            val shuffledRound = viewModel.shuffledRoundAvoidingPrevious(currentRound, displayed)
                            roundsList.add(currentRoundIndex + 1, shuffledRound)
                            viewModel.rounds.value = roundsList
                        } else {
                            viewModel.repeatStage.value = 0
                        }
                    }
                }
                viewModel.hadMistakeThisRound.value = false
            },
            showLabels = showLabels,
            overlaySprites = chosenSprites,
            overlayDirection = chosenDirection,
            overlayCount = when (chosenDirection) {
                TravelDirection.RIGHT, TravelDirection.LEFT -> 7
                else -> 20
            }

        )
    }

    // ---------- PRZEJŚCIE DO KOLEJNEJ RUNDY ----------
    LaunchedEffect(viewModel.goNextAfterCongrats.value) {
        if (viewModel.goNextAfterCongrats.value) {
            viewModel.goNextAfterCongrats.value = false
            viewModel.showCongratsScreen.value = false
            viewModel.goToNext(onGameFinished)
        }
    }

    // ---------- UI RUNDY ----------
    if ((!viewModel.showCongratsScreen.value || isTest) && !viewModel.goNextAfterCongrats.value) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Blue)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                Text(
                    text = commandText,
                    fontSize = 60.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(48.dp))

                RoundOptionsLayout(
                    options = currentRound.options.map { GameItem(it.label, it.imagePath) },
                    numberOfItems = displayedImages,
                    isDimmed = { item ->
                        !isTest &&
                                viewModel.dimIncorrect.value &&
                                viewModel.showHint.value &&
                                currentRound.options.any { it.label == item.label && it != currentRound.correctItem }
                    },
                    isScaled = { item ->
                        !isTest &&
                                viewModel.scaleCorrect.value &&
                                viewModel.showHint.value &&
                                currentRound.correctItem.label == item.label
                    },
                    animateCorrect = { item ->
                        !isTest &&
                                viewModel.animateCorrect.value &&
                                viewModel.showHint.value &&
                                currentRound.correctItem.label == item.label
                    },
                    outlineCorrect = { item ->
                        !isTest &&
                                viewModel.outlineCorrect.value &&
                                viewModel.showHint.value &&
                                currentRound.correctItem.label == item.label
                    },
                    showLabels = showLabels,
                    onClick = { item ->
                        currentRound.options.find { it.label == item.label }?.let { handleAnswer(it) }
                    }
                )
            }
        }
    }
}