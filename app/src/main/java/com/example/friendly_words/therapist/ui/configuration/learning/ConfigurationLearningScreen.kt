package com.example.friendly_words.therapist.ui.configuration.learning

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.friendly_words.therapist.ui.components.NumberSelectorForPictures
import com.example.friendly_words.therapist.ui.components.NumberSelector
import com.example.friendly_words.therapist.ui.components.NumberSelectorForPicturesPlain
import com.example.friendly_words.therapist.ui.theme.DarkBlue
import com.example.friendly_words.therapist.ui.theme.White
import com.example.shared.data.another.ConfigurationLearningState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.composables.core.ScrollArea
import com.composables.core.VerticalScrollbar
import com.composables.core.Thumb
import com.composables.core.rememberScrollAreaState

@Composable
fun ConfigurationLearningScreen(
    state: ConfigurationLearningState,
    availableImagesForLearning: Int,
    onEvent: (ConfigurationLearningEvent) -> Unit,
    onBackClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var textFieldSize by remember { mutableStateOf(IntSize.Zero) }
    val options = listOf("{Słowo}", "Gdzie jest {Słowo}", "Pokaż gdzie jest {Słowo}")
    val rightScrollState = rememberScrollState()
    val rightScrollAreaState = rememberScrollAreaState(rightScrollState)
    val leftScrollState = rememberScrollState()
    val leftScrollAreaState = rememberScrollAreaState(leftScrollState)
    val available = availableImagesForLearning.coerceAtLeast(0)
    val minAllowed = if (available == 0) 0 else 1
    val globalMaxAllowed = 6
    val maxAllowed = available

    LaunchedEffect(available) {
        val minAllowed = if (available == 0) 0 else 1
        val maxAllowed = available
        val defaultImageCount = when {
            available == 0 -> 0
            available < 3 -> available
            else -> 3
        }

        val current = state.imageCount
        val corrected = when {
            // brak dostępnych
            available == 0 -> 0

            // przejście z 0 → coś (np. dodano obrazki) → domyślna wartość
            current == 0 -> defaultImageCount

            // za mało → 1
            current < minAllowed -> minAllowed

            // za dużo → max
            current > maxAllowed -> maxAllowed

            // w pozostałych przypadkach – nie zmieniaj
            else -> current
        }

        if (corrected != current) {
            onEvent(ConfigurationLearningEvent.SetImageCount(corrected))
        }
    }

    var blockedDialogMsg by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            // Lewa kolumna
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(16.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                // ⬅️ ScrollArea + scrollbar dla lewej kolumny
                ScrollArea(state = leftScrollAreaState) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(leftScrollState)
                        ) {
                            Text(
                                text = "Ustawienia próby",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkBlue,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(29.dp))

                            // === LICZBA OBRAZKÓW ===
                            if (available == 0) {
                                // 0 dostępnych -> neutralny selector (bez szarzenia label/value)
                                NumberSelectorForPicturesPlain(
                                    label = "Liczba obrazków wyświetlanych na ekranie:",
                                    minValue = 0,
                                    maxValue = 0,
                                    value = state.imageCount, // i tak ustawiany na 0 w LaunchedEffect
                                    enabled = false,          // przyciski off, ale wygląd bez zmian
                                    labelColor = Color.Black
                                )

                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = "Dodaj materiały edukacyjne w zakładce „Materiał”, aby zwiększyć liczbę obrazków.",
                                    fontSize = 12.sp,
                                    color = Color.Red,
                                    fontStyle = FontStyle.Italic,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            } else {
                                // >0 dostępnych -> wersja z popupami i blokadami
                                NumberSelectorForPictures(
                                    label = "Liczba obrazków wyświetlanych na ekranie:",
                                    minValue = minAllowed,
                                    maxValue = maxAllowed,
                                    value = state.imageCount,
                                    enabled = true,
                                    onValueChange = { newValue ->
                                        val clamped = newValue.coerceIn(1, maxAllowed)
                                        if (clamped != state.imageCount) {
                                            onEvent(ConfigurationLearningEvent.SetImageCount(clamped))
                                        }
                                    },
                                    onDisabledDecrementClick = {
                                        blockedDialogMsg =
                                            "Nie można ustawić liczby obrazków na mniejszą niż $minAllowed."
                                    },
                                    onDisabledIncrementClick = {
                                        blockedDialogMsg = if (maxAllowed >= globalMaxAllowed) {
                                            "Nie można ustawić liczby obrazków na większą niż $globalMaxAllowed - jest to maksymalna liczba do wyboru."
                                        } else {
                                            "Nie można ustawić liczby obrazków na większą niż $available - tyle jest dostępnych obrazków w trybie uczenia."
                                        }
                                    }
                                )
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            NumberSelector(
                                label = "Liczba powtórzeń dla każdego słowa:",
                                minValue = 1,
                                maxValue = 3,
                                value = state.repetitionCount,
                                onValueChange = { onEvent(ConfigurationLearningEvent.SetRepetitionCount(it)) }
                            )

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Spacer(modifier = Modifier.height(25.dp))
                                Text(
                                    text = "Rodzaj polecenia:",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.Black
                                )
                                Spacer(modifier = Modifier.height(25.dp))

                                var localExpanded by remember { mutableStateOf(false) }

                                Box(
                                    modifier = Modifier
                                        .onGloballyPositioned { coordinates ->
                                            textFieldSize = coordinates.size
                                        }
                                        .clickable { localExpanded = !localExpanded }
                                ) {
                                    OutlinedTextField(
                                        value = state.selectedPrompt,
                                        onValueChange = {},
                                        readOnly = true,
                                        enabled = false,
                                        textStyle = LocalTextStyle.current.copy(fontSize = 18.sp),
                                        trailingIcon = {
                                            Icon(
                                                imageVector = Icons.Filled.ArrowDropDown,
                                                contentDescription = "Rozwiń",
                                                tint = DarkBlue
                                            )
                                        },
                                        colors = TextFieldDefaults.outlinedTextFieldColors(
                                            disabledTextColor = Color.Black,
                                            disabledBorderColor = DarkBlue,
                                            disabledLabelColor = DarkBlue,
                                            disabledTrailingIconColor = DarkBlue
                                        ),
                                    )

                                    DropdownMenu(
                                        expanded = localExpanded,
                                        onDismissRequest = { localExpanded = false },
                                    ) {
                                        options.forEach { selectionOption ->
                                            DropdownMenuItem(
                                                onClick = {
                                                    onEvent(ConfigurationLearningEvent.SetPrompt(selectionOption))
                                                    localExpanded = false
                                                }
                                            ) {
                                                Text(
                                                    text = selectionOption,
                                                    fontSize = 20.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        VerticalScrollbar(
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .fillMaxHeight()
                                .width(4.dp)
                        ) {
                            Thumb(Modifier.background(Color.Gray))
                        }
                    }
                }
            }


            // Prawa kolumna – dodany scroll + scrollbar
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(16.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                ScrollArea(state = rightScrollAreaState) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rightScrollState),
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {

                            Text(
                                text = "Ustawienia uczenia",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkBlue,
                                textAlign = TextAlign.Center
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.padding(top = 16.dp)
                            ) {
                                Text(
                                    text = "Podpisy pod obrazkami",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.Black
                                )

                                Switch(
                                    checked = state.captionsEnabled,
                                    onCheckedChange = { onEvent(ConfigurationLearningEvent.ToggleCaptions(it)) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = DarkBlue,
                                        checkedTrackColor = DarkBlue.copy(alpha = 0.5f),
                                        uncheckedThumbColor = Color.LightGray,
                                        uncheckedTrackColor = Color.Gray
                                    )
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(
                                    text = "Głosowe odtwarzanie polecenia",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.Black
                                )

                                Switch(
                                    checked = state.readingEnabled,
                                    onCheckedChange = {
                                        onEvent(ConfigurationLearningEvent.ToggleReading(it))
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = DarkBlue,
                                        checkedTrackColor = DarkBlue.copy(alpha = 0.5f),
                                        uncheckedThumbColor = Color.LightGray,
                                        uncheckedTrackColor = Color.Gray
                                    )
                                )
                            }

                            NumberSelector(
                                label = "Pokaż podpowiedź po (sekundach):",
                                minValue = 1,
                                maxValue = 10,
                                value = state.timeCount,
                                onValueChange = { onEvent(ConfigurationLearningEvent.SetTimeCount(it)) }
                            )

                            Text(
                                text = "Wybierz rodzaj podpowiedzi:",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                textAlign = TextAlign.Center
                            )

                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(
                                        checked = state.outlineCorrect,
                                        onCheckedChange = { newValue ->
                                            onEvent(ConfigurationLearningEvent.ToggleOutlineCorrect(newValue))
                                        },
                                        colors = CheckboxDefaults.colors(checkedColor = DarkBlue)
                                    )
                                    Text("Obramuj poprawną", fontSize = 18.sp)
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(
                                        checked = state.animateCorrect,
                                        onCheckedChange = { newValue ->
                                            onEvent(ConfigurationLearningEvent.ToggleAnimateCorrect(newValue))
                                        },
                                        colors = CheckboxDefaults.colors(checkedColor = DarkBlue)
                                    )
                                    Text("Porusz poprawną", fontSize = 18.sp)
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(
                                        checked = state.scaleCorrect,
                                        onCheckedChange = { newValue ->
                                            onEvent(ConfigurationLearningEvent.ToggleScaleCorrect(newValue))
                                        },
                                        colors = CheckboxDefaults.colors(checkedColor = DarkBlue)
                                    )
                                    Text("Powiększ poprawną", fontSize = 18.sp)
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(
                                        checked = state.dimIncorrect,
                                        onCheckedChange = { newValue ->
                                            onEvent(ConfigurationLearningEvent.ToggleDimIncorrect(newValue))
                                        },
                                        colors = CheckboxDefaults.colors(checkedColor = DarkBlue)
                                    )
                                    Text("Wyszarz niepoprawne", fontSize = 18.sp)
                                }
                            }
                        }

                        VerticalScrollbar(
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .fillMaxHeight()
                                .width(4.dp)
                        ) {
                            Thumb(Modifier.background(Color.Gray))
                        }
                    }
                }
            }

        }
    }

    if (available > 0) {
        blockedDialogMsg?.let { msg ->
            AlertDialog(
                onDismissRequest = { blockedDialogMsg = null },
                title = { Text("Informacja") },
                text = { Text(msg) },
                confirmButton = {
                    TextButton(onClick = { blockedDialogMsg = null }) {
                        Text("OK", color = DarkBlue, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    }
}
