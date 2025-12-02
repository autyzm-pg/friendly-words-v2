package com.example.friendly_words.therapist.ui.configuration.save

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.friendly_words.therapist.ui.components.InfoDialog
import com.example.shared.data.another.ConfigurationLearningState
import com.example.shared.data.another.ConfigurationMaterialState
import com.example.shared.data.another.ConfigurationReinforcementState
import com.example.friendly_words.therapist.ui.configuration.settings.ConfigurationSettingsEvent
import com.example.shared.data.another.ConfigurationTestState
import com.example.friendly_words.therapist.ui.theme.DarkBlue
import com.example.friendly_words.therapist.ui.theme.White


fun Boolean.toYesNo(): String = if (this) "Tak" else "Nie"

fun ConfigurationLearningState.hintsSummary(): String {
    val hints = listOfNotNull(
        if (outlineCorrect) "Obramuj poprawną" else null,
        if (animateCorrect) "Porusz poprawną" else null,
        if (scaleCorrect) "Powiększ poprawną" else null,
        if (dimIncorrect) "Wyszarz niepoprawne" else null
    )
    return hints.joinToString("; ").ifEmpty { "-" }
}


@Composable
fun ConfigurationSaveScreen(
    materialState: ConfigurationMaterialState,
    learningState: ConfigurationLearningState,
    reinforcementState: ConfigurationReinforcementState,
    testState: ConfigurationTestState,
    saveState: ConfigurationSaveState,
    onEvent: (ConfigurationSaveEvent) -> Unit,
    onSettingsEvent: (ConfigurationSettingsEvent) -> Unit,
    onBackClick: () -> Unit = {}
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboardController?.show()

        val text = saveState.stepName.text
        onEvent(ConfigurationSaveEvent.SetStepName(
            TextFieldValue(text = text, selection = TextRange(text.length))
        ))
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    focusManager.clearFocus()
                }

        ) {
            Column(
                modifier = Modifier
                    .weight(0.3f)
                    .fillMaxHeight()
                    .padding(top = 80.dp,end = 16.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally

            ) {
                Text(
                    text = "Zapisz jako:",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp),
                    color = DarkBlue
                )

                OutlinedTextField(
                    value = saveState.stepName,
                    onValueChange = { newValue ->
                        onEvent(ConfigurationSaveEvent.SetStepName(newValue))
                    },
                    label = { Text("Wpisz nazwę kroku") },
                    modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            keyboardController?.hide()
                        }
                    ),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = DarkBlue,
                        unfocusedLabelColor = Color.Gray,
                        focusedLabelColor = DarkBlue,
                        cursorColor = Color.Black
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        onEvent(ConfigurationSaveEvent.ValidateName)

                        if (saveState.stepName.text.trim().isNotBlank()) {
                            onSettingsEvent(
                                ConfigurationSettingsEvent.Save(
                                    ConfigurationSaveEvent.SaveConfiguration(name = saveState.stepName.text.trim())
                                )
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(backgroundColor = DarkBlue),
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(48.dp)
                ) {
                    Text("Zapisz", color = White, fontSize = 16.sp)
                }
            }

            Column(
                modifier = Modifier
                    .weight(0.7f)
                    .verticalScroll(rememberScrollState())
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 55.dp, end = 15.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "INFORMACJE O KROKU", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, color = DarkBlue)
                        Spacer(modifier = Modifier.width(110.dp))
                        Text(text = "TRYB NAUKI", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, color = DarkBlue)
                        Text(text = "TRYB TESTU", modifier = Modifier.weight(1f).padding(start = 32.dp), fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, color = DarkBlue)
                    }

                    val learnedWordsCount = materialState.vocabItems.count { item ->
                        item.selectedImages.zip(item.inLearningStates).any { (selected, inLearning) -> selected && inLearning }
                    }

                    val learnedWords = materialState.vocabItems
                        .filter { item ->
                            item.selectedImages.zip(item.inLearningStates).any { (selected, inLearning) -> selected && inLearning }
                        }
                        .joinToString(", ") { it.word }
                        .ifEmpty { "-" }

                    val testWordsCount = materialState.vocabItems.count { item ->
                        item.selectedImages.zip(item.inTestStates).any { (selected, inTest) -> selected && inTest }
                    }

                    val testWords = materialState.vocabItems
                        .filter { item ->
                            item.selectedImages.zip(item.inTestStates).any { (selected, inTest) -> selected && inTest }
                        }
                        .joinToString(", ") { it.word }
                        .ifEmpty { "-" }


                    Spacer(modifier = Modifier.height(8.dp))

                    val fields = listOf(
                        "Liczba uczonych słów" to (
                                learnedWordsCount.toString() to testWordsCount.toString()
                                ),
                        "Uczone słowa" to (
                                learnedWords to testWords
                                ),
                        "Liczba wyświetlanych zdjęć" to (
                                learningState.imageCount.toString() to testState.imageCount.toString()
                                ),
                        "Liczba powtórzeń dla każdego słowa" to (
                                learningState.repetitionCount.toString() to testState.repetitionCount.toString()
                                ),
                        "Rodzaj polecenia" to (
                                learningState.selectedPrompt to testState.selectedPrompt
                                ),
                        "Podpisy pod obrazkami" to (
                                learningState.captionsEnabled.toYesNo() to testState.captionsEnabled.toYesNo()
                                ),
                        "Czytanie polecenia" to (
                                learningState.readingEnabled.toYesNo() to testState.readingEnabled.toYesNo()
                                ),
                        "Pokaż podpowiedź po" to (
                                "${learningState.timeCount} s" to "X"
                                ),
                        "Podpowiedzi" to (
                                learningState.hintsSummary() to "X"
                                ),
                        "Pochwały słowne" to (
                                reinforcementState.praiseStates
                                    .filterValues { it }
                                    .keys
                                    .joinToString(", ")
                                    .ifEmpty { "-" } to "X"
                                ),
                        "Animacje" to (
                                reinforcementState.animationsEnabled.toYesNo() to "X"
                                )
                    )

                    fields.forEach { (label, values) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = label, modifier = Modifier.weight(1f))
                            Spacer(modifier = Modifier.width(110.dp))
                            Text(text = values.first, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                            Text(text = values.second.toString(), modifier = Modifier.weight(1f).padding(start = 32.dp), textAlign = TextAlign.Center)
                        }
                        Divider()
                    }
                }
            }
        }
    }

    InfoDialog(
        show = saveState.showEmptyNameDialog,
        message = "Nazwa kroku nie może być pusta!",
        onDismiss = { onEvent(ConfigurationSaveEvent.DismissEmptyNameDialog) }
    )
    InfoDialog(
        show = saveState.showDuplicateNameDialog,
        message = "Krok uczenia o takiej nazwie już istnieje.",
        onDismiss = { onEvent(ConfigurationSaveEvent.DismissDuplicateNameDialog) }
    )

}