package com.example.friendly_words.therapist.ui.configuration.material

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.composables.core.ScrollArea
import com.composables.core.Thumb
import com.composables.core.VerticalScrollbar
import com.composables.core.rememberScrollAreaState
import com.example.friendly_words.therapist.ui.components.YesNoDialogWithName
import com.example.friendly_words.therapist.ui.theme.DarkBlue
import com.example.friendly_words.therapist.ui.theme.LightBlue
import com.example.friendly_words.therapist.ui.theme.White
import com.example.shared.data.another.ConfigurationMaterialState

@Composable
fun ImageSelectionWithCheckbox(
    images: List<String>,
    selectedImages: List<Boolean>,
    onImageSelectionChanged: (List<Boolean>) -> Unit,
    onLearningTestChanged: (index: Int, inLearning: Boolean, inTest: Boolean) -> Unit,
    inLearningStates: List<Boolean>,
    inTestStates: List<Boolean>
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .background(color = White)
    ) {
        images.chunked(3).forEachIndexed { chunkIndex, chunk ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = White)
            ) {
                chunk.forEachIndexed { innerIndex, resId ->
                    val index = chunkIndex * 3 + innerIndex
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Checkbox(
                            checked = selectedImages[index],
                            onCheckedChange = {
                                val newSelectedImages = selectedImages.toMutableList()
                                    .also { it[index] = !it[index] }
                                onImageSelectionChanged(newSelectedImages)

                                val checked = newSelectedImages[index]
                                onLearningTestChanged(index, checked, checked)
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor = DarkBlue,
                                uncheckedColor = Color.Gray,
                                checkmarkColor = Color.White
                            )
                        )
                        Box(
                            modifier = Modifier
                                .height(200.dp)
                                .aspectRatio(1f)
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter(model = resId),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Checkbox(
                                checked = inLearningStates[index],
                                onCheckedChange = { newLearning ->
                                    val currentTest = inTestStates[index]
                                    val shouldSelectImage = newLearning || currentTest
                                    val newSelectedImages = selectedImages.toMutableList()
                                        .also { it[index] = shouldSelectImage }
                                    onImageSelectionChanged(newSelectedImages)
                                    onLearningTestChanged(index, newLearning, currentTest)
                                },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = LightBlue,
                                    uncheckedColor = Color.Gray,
                                    checkmarkColor = Color.White
                                )
                            )
                            Spacer(modifier = Modifier.width(35.dp))
                            Checkbox(
                                checked = inTestStates[index],
                                onCheckedChange = { newTest ->
                                    val currentLearning = inLearningStates[index]
                                    val shouldSelectImage = newTest || currentLearning
                                    val newSelectedImages = selectedImages.toMutableList()
                                        .also { it[index] = shouldSelectImage }
                                    onImageSelectionChanged(newSelectedImages)
                                    onLearningTestChanged(index, currentLearning, newTest)
                                },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = LightBlue,
                                    uncheckedColor = Color.Gray,
                                    checkmarkColor = Color.White
                                )
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Spacer(modifier = Modifier.width(5.dp))
                            Text("Uczenie")
                            Spacer(modifier = Modifier.width(35.dp))
                            Text("Test")
                            Spacer(modifier = Modifier.width(17.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ConfigurationMaterialScreen(
    state: ConfigurationMaterialState,
    hideExamples: Boolean,
    onEvent: (ConfigurationMaterialEvent) -> Unit,
    onBackClick: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var showHideExamplesInfo by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(DarkBlue.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Column(modifier = Modifier.fillMaxHeight()) {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf("SŁOWO", "W UCZENIU", "W TEŚCIE", "USUŃ")
                            .forEachIndexed { i, label ->
                                Text(
                                    label,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Gray,
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(end = if (i == 3) 13.dp else 0.dp),
                                    textAlign = when (i) {
                                        0 -> TextAlign.Start
                                        3 -> TextAlign.End
                                        else -> TextAlign.Center
                                    }
                                )
                            }
                    }

                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        items(state.vocabItems) { item ->
                            val index = state.vocabItems.indexOf(item)
                            val isSelected = state.selectedWordIndex == index

                            val hasLearning = item.selectedImages.zip(item.inLearningStates)
                                .any { (isSelectedImage, inLearning) -> isSelectedImage && inLearning }

                            val hasTest = item.selectedImages.zip(item.inTestStates)
                                .any { (isSelectedImage, inTest) -> isSelectedImage && inTest }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        if (isSelected) LightBlue.copy(alpha = 0.3f)
                                        else Color.Transparent
                                    )
                                    .padding(horizontal = 4.dp, vertical = 8.dp)
                                    .clickable {
                                        onEvent(ConfigurationMaterialEvent.WordSelected(index))
                                    }
                            ) {
                                Box(modifier = Modifier.weight(1f)) {
                                    Text(
                                        item.learnedWord,
                                        fontSize = 30.sp,
                                        fontWeight = FontWeight.Bold,
                                    )
                                }
                                Row(modifier = Modifier.weight(2f)) {
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = if (hasLearning)
                                                Icons.Default.Check
                                            else
                                                Icons.Default.Close,
                                            contentDescription = null,
                                            tint = if (hasLearning)
                                                Color(0xFF4CAF50)
                                            else
                                                Color.Red,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = if (hasTest)
                                                Icons.Default.Check
                                            else
                                                Icons.Default.Close,
                                            contentDescription = null,
                                            tint = if (hasTest)
                                                Color(0xFF4CAF50)
                                            else
                                                Color.Red,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                                Box(
                                    modifier = Modifier.weight(1f),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    IconButton(onClick = {
                                        onEvent(ConfigurationMaterialEvent.WordDeleted(index))
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Usuń",
                                            tint = DarkBlue,
                                            modifier = Modifier.size(40.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    val indexToDelete = state.wordIndexToDelete
                    if (
                        state.showDeleteDialog &&
                        indexToDelete != null &&
                        indexToDelete in state.vocabItems.indices
                    ) {
                        YesNoDialogWithName(
                            show = true,
                            message = "Czy chcesz usunąć z kroku uczenia słowo:",
                            name = "${state.vocabItems[indexToDelete].learnedWord}?",
                            onConfirm = {
                                onEvent(ConfigurationMaterialEvent.ConfirmDelete(indexToDelete))
                            },
                            onDismiss = {
                                onEvent(ConfigurationMaterialEvent.CancelDelete)
                            }
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Button(
                            onClick = {
                                onEvent(ConfigurationMaterialEvent.ShowAddDialog)
                            },
                            colors = ButtonDefaults.buttonColors(backgroundColor = DarkBlue),
                            modifier = Modifier
                                .width(200.dp)
                                .height(48.dp)
                        ) {
                            Text(
                                "DODAJ",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        IconButton(
                            onClick = { showHideExamplesInfo = true },
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .size(30.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Informacja o ukrytych materiałach",
                                tint = Color.Gray,
                                modifier = Modifier.size(44.dp)
                            )
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(color = White)
                    .padding(16.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    val selectedIndex = state.selectedWordIndex
                    if (selectedIndex in state.vocabItems.indices) {
                        val item = state.vocabItems[selectedIndex]
                        val images = item.imagePaths

                        if (images.isEmpty()) {
                            Text(
                                "Ten materiał nie zawiera żadnych zdjęć. Aby je dodać, przejdź do sekcji 'MATERIAŁY EDUKACYJNE'.",
                                fontSize = 20.sp
                            )
                        } else {
                            ImageSelectionWithCheckbox(
                                images = images,
                                selectedImages = item.selectedImages,
                                inLearningStates = item.inLearningStates,
                                inTestStates = item.inTestStates,
                                onImageSelectionChanged = {
                                    onEvent(
                                        ConfigurationMaterialEvent.ImageSelectionChanged(it)
                                    )
                                },
                                onLearningTestChanged = { i, learning, test ->
                                    onEvent(
                                        ConfigurationMaterialEvent.LearningTestChanged(
                                            i,
                                            learning,
                                            test
                                        )
                                    )
                                }
                            )
                        }
                    } else {
                        Text(
                            "Do kroku uczenia nie dodano jeszcze żadnych materiałów. Aby to zrobić, kliknij przycisk 'DODAJ'. ",
                            fontSize = 20.sp
                        )
                    }
                }
            }
        }

        if (state.showAddDialog) {
            val dialogListState = rememberLazyListState()
            val dialogScrollAreaState = rememberScrollAreaState(dialogListState)

            val focus = focusManager
            val kb = keyboardController

            val closeDialog = {
                searchQuery = ""
                kb?.hide()
                focus.clearFocus()
                onEvent(ConfigurationMaterialEvent.HideAddDialog)
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        closeDialog()
                    },
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .fillMaxHeight(0.8f)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            kb?.hide()
                            focus.clearFocus()
                        },
                    elevation = 8.dp,
                    color = Color.White,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Wybierz materiał, który chcesz dodać do kroku uczenia:",
                            fontSize = 26.sp,
                            fontStyle = FontStyle.Italic,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        if (hideExamples) {
                            Text(
                                text = "Przykładowe materiały są ukryte. Aby je zobaczyć, odznacz opcję „Ukryj przykładowe materiały” w sekcji „Materiały edukacyjne”.",
                                color = Color.Red,
                                fontSize = 14.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp)
                            )
                        }

                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            label = { Text("Szukaj...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            keyboardOptions = KeyboardOptions.Default.copy(
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    kb?.hide()
                                    focus.clearFocus()
                                }
                            ),
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                textColor = Color.Black,
                                cursorColor = DarkBlue,
                                focusedBorderColor = DarkBlue,
                                unfocusedBorderColor = Color.Gray,
                                focusedLabelColor = DarkBlue,
                                unfocusedLabelColor = Color.Gray
                            )
                        )

                        val filteredWords = state.availableWordsToAdd
                            .filter {
                                (it.name.contains(searchQuery, ignoreCase = true) ||
                                        it.category.contains(searchQuery, ignoreCase = true)) &&
                                        (!hideExamples || !it.isExample)
                            }
                            .sortedBy { it.name.lowercase() }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            ScrollArea(state = dialogScrollAreaState) {
                                LazyColumn(
                                    state = dialogListState,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(end = 12.dp)
                                ) {
                                    if (filteredWords.isEmpty()) {
                                        item {
                                            Text(
                                                "BRAK WYNIKÓW\nDODAJ MATERIAŁY W SEKCJI MATERIAŁY EDUKACYJNE",
                                                fontSize = 16.sp,
                                                color = Color.Gray,
                                                modifier = Modifier.padding(vertical = 8.dp)
                                            )
                                        }
                                    } else {
                                        items(filteredWords) { resource ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        closeDialog()
                                                        onEvent(
                                                            ConfigurationMaterialEvent.AddWord(
                                                                resource.id
                                                            )
                                                        )
                                                    }
                                                    .padding(vertical = 8.dp)
                                            ) {
                                                Text(
                                                    text = resource.name,
                                                    fontSize = 18.sp,
                                                    fontWeight = FontWeight.Normal,
                                                    color = Color.Black
                                                )
                                                if (resource.category.isNotBlank()) {
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        text = "(${resource.category})",
                                                        fontSize = 16.sp,
                                                        color = Color.Gray
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                VerticalScrollbar(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .fillMaxHeight()
                                        .width(8.dp)
                                ) {
                                    Thumb(Modifier.background(Color.Gray))
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Button(
                                onClick = closeDialog,
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = DarkBlue,
                                    contentColor = Color.White
                                )
                            ) {
                                Text("ANULUJ")
                            }
                        }
                    }
                }
            }
        }
    }

    if (showHideExamplesInfo) {
        AlertDialog(
            onDismissRequest = { showHideExamplesInfo = false },
            title = { Text("Kolejny krok") },
            text = {
                Text(
                    "Możesz dodać kolejne materiały poprzez przycisk DODAJ lub przejść do kolejnej zakładki klikając wyżej przycisk z napisem UCZENIE."
                )
            },
            confirmButton = {
                Box(
                    modifier = Modifier
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            showHideExamplesInfo = false
                        }
                        .padding(8.dp)
                ) {
                    Text(
                        text = "OK",
                        color = DarkBlue,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        )
    }
}