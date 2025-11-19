package com.example.friendly_words.therapist.ui.materials.creating_new

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.CheckboxDefaults
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.OutlinedTextField
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.friendly_words.R
import com.example.friendly_words.therapist.ui.components.InfoDialog
import com.example.friendly_words.therapist.ui.components.NewConfigurationTopBar
import com.example.friendly_words.therapist.ui.components.YesNoDialog
import com.example.friendly_words.therapist.ui.main.NavRoutes
import com.example.friendly_words.therapist.ui.theme.DarkBlue
import com.example.friendly_words.therapist.ui.theme.LightBlue
import com.example.shared.data.entities.Image
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import android.graphics.Bitmap
import android.util.Log
import com.composables.core.ScrollArea
import com.composables.core.Thumb
import com.composables.core.VerticalScrollbar
import com.composables.core.rememberScrollAreaState

private sealed class GridTile {
    data class Photo(val image: Image) : GridTile()
    data object Placeholder : GridTile()
}

@Composable
private fun rememberHideKeyboard(): () -> Unit {
    val focusManager      = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    return {
        focusManager.clearFocus(force = true)
        keyboardController?.hide()
    }
}

@Composable
fun MaterialsCreatingNewMaterialScreen(
    navController: NavController,
    viewModel: MaterialsCreatingNewMaterialViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
    onSaveClick: (Long) -> Unit,
    //resourceId: Long?

) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val hideKeyboard = rememberHideKeyboard()

    // ⬇️ stan listy i scrollbara dla listy obrazków (prawa strona)
    val imagesListState = rememberLazyListState()
    val imagesScrollAreaState = rememberScrollAreaState(imagesListState)

    // ⬇️ stan scrolla i scrollbara dla lewej kolumny z polami
    val leftScrollState = rememberScrollState()
    val leftScrollAreaState = rememberScrollAreaState(leftScrollState)

    LaunchedEffect(state.saveCompleted) {
        if (state.saveCompleted) {
            hideKeyboard()
            state.newlySavedResourceId?.let { newId ->

                // 1) wypisz current/previous entry i ich klucze
                val currentEntry = navController.currentBackStackEntry
                val prevEntry    = navController.previousBackStackEntry
                Log.d("NavDebug", "▶▶ current route = ${currentEntry?.destination?.route}, " +
                        "handleKeys = ${currentEntry?.savedStateHandle?.keys()}")
                Log.d("NavDebug", "▶▶ previous route = ${prevEntry?.destination?.route}, " +
                        "handleKeys = ${prevEntry?.savedStateHandle?.keys()}")

                // 2) wypisz klucze handle'u dla ekranu listy po jego ROUTE
                val listEntry = navController.getBackStackEntry(NavRoutes.MATERIALS)
                Log.d("NavDebug", "▶▶ MATERIALS handle keys before set = ${listEntry.savedStateHandle.keys()}")

                // 3) zapis nowego ID i wiadomości pod tym handle'em
                listEntry.savedStateHandle["newlySavedResourceId"] = newId
                listEntry.savedStateHandle["message"]            =
                    if (state.isEditing) "Pomyślnie zaktualizowano materiał"
                    else                   "Pomyślnie dodano materiał"

                // 4) wróć do ekranu listy
                navController.popBackStack()
            }
            viewModel.onEvent(MaterialsCreatingNewMaterialEvent.ResetSaveCompleted)
        }
    }

    LaunchedEffect(state.exitWithoutSaving) {
        if (state.exitWithoutSaving) {
            hideKeyboard()
            onBackClick()
            viewModel.onEvent(MaterialsCreatingNewMaterialEvent.ResetExitWithoutSaving)
        }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    // Launcher do wyboru obrazu z galerii
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let {
                viewModel.onEvent(MaterialsCreatingNewMaterialEvent.AddImage(it))
            }
        }
    )

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview(),
        onResult = { bitmap ->
            bitmap?.let {
                val filename = "photo_${System.currentTimeMillis()}.jpg"
                val file = File(context.filesDir, filename)

                try {
                    FileOutputStream(file).use { out ->
                        it.compress(Bitmap.CompressFormat.JPEG, 100, out)
                    }

                    val image = Image(
                        path = file.absolutePath
                    )

                    viewModel.onEvent(MaterialsCreatingNewMaterialEvent.ImageTakenFromCamera(image))

                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    )

    Scaffold(
        topBar = {
            NewConfigurationTopBar(
                title = if (state.isEditing) {
                    "Edycja: ${state.resourceName}"
                } else {
                    "Nowy materiał: ${state.resourceName}"
                },
                onBackClick = {
                    hideKeyboard()
                    viewModel.onEvent(MaterialsCreatingNewMaterialEvent.ShowExitDialog)
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.White)
                .padding(24.dp)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    focusManager.clearFocus()
                }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                // ⬅️ Lewa kolumna ze ScrollArea + VerticalScrollbar
                Box(
                    modifier = Modifier.weight(1f)
                ) {
                    ScrollArea(state = leftScrollAreaState) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .verticalScroll(leftScrollState),
                                verticalArrangement = Arrangement.Top,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Spacer(modifier = Modifier.height(40.dp))//bylo 110

                                Text(
                                    "Uczone pojęcie (słowo)",
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DarkBlue
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = state.learnedWord,
                                    onValueChange = {
                                        viewModel.onEvent(MaterialsCreatingNewMaterialEvent.LearnedWordChanged(it))
                                    },
                                    label = { Text("Wpisz słowo") },
                                    modifier = Modifier
                                        .fillMaxWidth(0.8f)
                                        .focusRequester(focusRequester),
                                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                                    keyboardActions = KeyboardActions(
                                        onDone = {
                                            keyboardController?.hide()
                                        }
                                    ),
                                    colors = TextFieldDefaults.outlinedTextFieldColors(
                                        backgroundColor = Color.White,
                                        disabledTextColor = Color.DarkGray,
                                        disabledBorderColor = Color.Gray,
                                        focusedBorderColor = DarkBlue,
                                        unfocusedBorderColor = Color.Gray,
                                        focusedLabelColor = DarkBlue,
                                        unfocusedLabelColor = Color.DarkGray,
                                        cursorColor = Color.Black
                                    )
                                )

                                Spacer(modifier = Modifier.height(16.dp))
                                Text("Kategoria", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = DarkBlue)
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = state.category, // <-- wymaga pola 'category' w stanie
                                    onValueChange = { viewModel.onEvent(MaterialsCreatingNewMaterialEvent.CategoryChanged(it)) },
                                    label = { Text("Wpisz kategorię (np. zwierzę)") },
                                    modifier = Modifier
                                        .fillMaxWidth(0.8f),
                                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                                    keyboardActions = KeyboardActions(
                                        onDone = { keyboardController?.hide() }
                                    ),
                                    colors = TextFieldDefaults.outlinedTextFieldColors(
                                        backgroundColor = Color.White,
                                        disabledTextColor = Color.DarkGray,
                                        disabledBorderColor = Color.Gray,
                                        focusedBorderColor = DarkBlue,
                                        unfocusedBorderColor = Color.Gray,
                                        focusedLabelColor = DarkBlue,
                                        unfocusedLabelColor = Color.DarkGray,
                                        cursorColor = Color.Black
                                    )
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                // Checkbox: czy zezwolić na edycję "Nazwa zasobu"
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth(0.8f)
                                ) {
                                    androidx.compose.material.Checkbox(
                                        checked = state.allowEditingResourceName,
                                        onCheckedChange = {
                                            viewModel.onEvent(
                                                MaterialsCreatingNewMaterialEvent.ToggleAllowEditingResourceName(
                                                    it
                                                )
                                            )
                                        },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = DarkBlue,
                                            uncheckedColor = Color.Gray,
                                            checkmarkColor = Color.White
                                        )

                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Inna nazwa materiału", fontSize = 16.sp)
                                }

                                Spacer(modifier = Modifier.height(24.dp))
                                Text(
                                    "Nazwa materiału",
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (state.allowEditingResourceName) DarkBlue else Color.Gray
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = state.resourceName,
                                    onValueChange = {
                                        if (state.allowEditingResourceName) {
                                            viewModel.onEvent(
                                                MaterialsCreatingNewMaterialEvent.ResourceNameChanged(
                                                    it
                                                )
                                            )
                                        }
                                    },
                                    label = { Text("Wpisz nazwę") },
                                    enabled = state.allowEditingResourceName,
                                    modifier = Modifier.fillMaxWidth(0.8f),
                                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                                    keyboardActions = KeyboardActions(
                                        onDone = {
                                            keyboardController?.hide()
                                        }
                                    ),
                                    colors = TextFieldDefaults.outlinedTextFieldColors(
                                        backgroundColor = Color.White,
                                        disabledTextColor = Color.DarkGray,
                                        disabledBorderColor = Color.Gray,
                                        focusedBorderColor = DarkBlue,
                                        unfocusedBorderColor = Color.Gray,
                                        focusedLabelColor = DarkBlue,
                                        unfocusedLabelColor = Color.DarkGray,
                                        cursorColor = Color.Black
                                    )
                                )
                                Spacer(modifier = Modifier.height(80.dp))
                                Button(
                                    onClick = {
                                        viewModel.onEvent(MaterialsCreatingNewMaterialEvent.SaveClicked)
                                    },
                                    colors = ButtonDefaults.buttonColors(backgroundColor = DarkBlue),
                                    modifier = Modifier
                                        .fillMaxWidth(0.5f)
                                        .height(48.dp)
                                ) {
                                    Text("Zapisz", color = Color.White, fontSize = 16.sp)
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

                // ➡️ Prawa kolumna
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            "Dodane obrazki",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkBlue
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        val tiles = remember(state.images) {
                            state.images.map { GridTile.Photo(it) } + GridTile.Placeholder
                        }
                        val groupedTiles = tiles.chunked(3)

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(500.dp)
                                .padding(horizontal = 8.dp)
                        ) {
                            ScrollArea(state = imagesScrollAreaState) {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    LazyColumn(
                                        state = imagesListState,
                                        verticalArrangement = Arrangement.spacedBy(12.dp),
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        items(groupedTiles) { group ->
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                group.forEach { tile ->
                                                    when (tile) {
                                                        is GridTile.Photo -> {
                                                            Box(
                                                                modifier = Modifier
                                                                    .weight(1f)
                                                                    .aspectRatio(1f)
                                                                    .padding(top = 8.dp),
                                                                contentAlignment = Alignment.TopEnd
                                                            ) {
                                                                val painter =
                                                                    rememberAsyncImagePainter(model = tile.image.path)
                                                                Image(
                                                                    painter = painter,
                                                                    contentDescription = null,
                                                                    contentScale = ContentScale.Fit,
                                                                    modifier = Modifier.fillMaxSize()
                                                                )
                                                                Box(
                                                                    modifier = Modifier
                                                                        .align(Alignment.TopEnd)
                                                                        .padding(6.dp)
                                                                        .size(38.dp)
                                                                        .background(
                                                                            LightBlue,
                                                                            shape = MaterialTheme.shapes.small
                                                                        ),
                                                                    contentAlignment = Alignment.Center
                                                                ) {
                                                                    Icon(
                                                                        imageVector = Icons.Filled.Clear,
                                                                        contentDescription = "Usuń zdjęcie",
                                                                        tint = Color.White,
                                                                        modifier = Modifier
                                                                            .size(14.dp)
                                                                            .clickable {
                                                                                viewModel.onEvent(
                                                                                    MaterialsCreatingNewMaterialEvent.RequestImageDeletion(
                                                                                        tile.image
                                                                                    )
                                                                                )
                                                                            }
                                                                    )
                                                                }
                                                            }
                                                        }

                                                        GridTile.Placeholder -> {
                                                            Box(
                                                                modifier = Modifier
                                                                    .weight(1f)
                                                                    .aspectRatio(1f)
                                                                    .padding(top = 8.dp)
                                                                    .background(
                                                                        color = LightBlue.copy(alpha = 0.08f),
                                                                        shape = MaterialTheme.shapes.small
                                                                    )
                                                                    .border(
                                                                        width = 1.dp,
                                                                        color = LightBlue,
                                                                        shape = MaterialTheme.shapes.small
                                                                    ),
                                                                contentAlignment = Alignment.Center
                                                            ) {
                                                                Text(
                                                                    text = "Jeśli chcesz dodać obrazek,\nużyj przycisków poniżej",
                                                                    color = DarkBlue,
                                                                    fontSize = 14.sp,
                                                                    lineHeight = 18.sp,
                                                                    textAlign = TextAlign.Center,
                                                                    modifier = Modifier.fillMaxWidth()
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                                // wypełnij wiersz pustymi miejscami, jeśli ma < 3 elementy
                                                repeat(3 - group.size) {
                                                    Spacer(modifier = Modifier.weight(1f))
                                                }
                                            }
                                        }
                                    }

                                    VerticalScrollbar(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .fillMaxHeight()
                                            .width(4.dp)
                                    ) {
                                        Thumb(Modifier.background(Color.Gray))
                                    }
                                }
                            }
                        }
                    }

                    // 🔽 Przyciski na dole
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = { galleryLauncher.launch("image/*") },
                            colors = ButtonDefaults.buttonColors(backgroundColor = DarkBlue),
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp)
                                .height(48.dp)
                        ) {
                            Text("Z galerii", color = Color.White)
                        }

                        Button(
                            onClick = { cameraLauncher.launch() },
                            colors = ButtonDefaults.buttonColors(backgroundColor = DarkBlue),
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp)
                                .height(48.dp)
                        ) {
                            Text("Zrób zdjęcie", color = Color.White)
                        }
                    }
                }
            }
        }

        if (state.showExitConfirmation) {
            YesNoDialog(
                show = true,
                message = "Czy na pewno chcesz wyjść bez zapisania zmian?",
                onConfirm = {
                    viewModel.onEvent(MaterialsCreatingNewMaterialEvent.DismissExitDialog)
                    onBackClick()
                },
                onDismiss = {
                    viewModel.onEvent(MaterialsCreatingNewMaterialEvent.DismissExitDialog)
                }
            )
        }
        if (state.showDuplicateNameConfirmation) {
            YesNoDialog(
                show = true,
                message = "Materiał o tej nazwie już istnieje. Czy na pewno chcesz zapisać nowy materiał z tą samą nazwą?",
                onConfirm = {
                    viewModel.onEvent(MaterialsCreatingNewMaterialEvent.ConfirmSaveDespiteDuplicate)
                },
                onDismiss = {
                    viewModel.onEvent(MaterialsCreatingNewMaterialEvent.DismissDuplicateNameDialog)
                }
            )
        }
        if (state.imageToConfirmDelete != null) {
            YesNoDialog(
                show = true,
                message = "Czy na pewno chcesz usunąć wybrane zdjęcie?",
                onConfirm = {
                    viewModel.onEvent(MaterialsCreatingNewMaterialEvent.ConfirmImageDeletion)
                },
                onDismiss = {
                    viewModel.onEvent(MaterialsCreatingNewMaterialEvent.CancelImageDeletion)
                }
            )
        }
        if (state.showEmptyTextFieldsDialog) {
            InfoDialog(
                show = true,
                message = "'Uczone pojęcie (słowo)' oraz 'Nazwa materiału' nie mogą być puste!",
                onDismiss = {
                    viewModel.onEvent(MaterialsCreatingNewMaterialEvent.DismissEmptyFieldsDialog)
                }
            )
        }
    }
}
