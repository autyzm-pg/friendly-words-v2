package com.example.friendly_words.therapist.ui.materials.list

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.friendly_words.therapist.ui.theme.DarkBlue
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.ui.text.font.FontWeight
import com.example.friendly_words.therapist.ui.theme.LightBlue
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.FileCopy
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import com.example.friendly_words.therapist.ui.components.YesNoDialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.composables.core.ScrollArea
import com.composables.core.Thumb
import com.composables.core.VerticalScrollbar
import com.composables.core.rememberScrollAreaState

@Composable
fun MaterialsListScreen(
    navController: NavController,
    onBackClick: () -> Unit,
    onCreateClick: () -> Unit,
    onEditClick: (Long) -> Unit,
    viewModel: MaterialsListViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var expandedImagePath by remember { mutableStateOf<String?>(null) }
    var showHideExamplesInfo by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    var searchQuery by remember { mutableStateOf("") }

    val newlySavedId = remember {
        navController.currentBackStackEntry
            ?.savedStateHandle
            ?.get<Long>("newlySavedResourceId")
    }.also {
        navController.currentBackStackEntry
            ?.savedStateHandle
            ?.remove<Long>("newlySavedResourceId")
    }
    var newIdConsumed by remember { mutableStateOf(false) }
    LaunchedEffect(newlySavedId, state.materials) {
        if (!newIdConsumed && newlySavedId != null) {
            val exists = state.materials.any { it.id == newlySavedId }
            if (exists) {
                viewModel.onEvent(MaterialsListEvent.SelectByResourceId(newlySavedId))
                newIdConsumed = true
            }
        }
    }

    LaunchedEffect(Unit) {
        val message = navController.currentBackStackEntry
            ?.savedStateHandle
            ?.get<String>("message")
        message?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Short
            )
            navController.currentBackStackEntry
                ?.savedStateHandle
                ?.remove<String>("message")
        }
    }

    LaunchedEffect(state.infoMessage) {
        state.infoMessage?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Short
            )
            viewModel.onEvent(MaterialsListEvent.ClearInfoMessage)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                backgroundColor = DarkBlue,
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }

                        Text(
                            "Materiały edukacyjne",
                            fontSize = 30.sp,
                            color = Color.White,
                            modifier = Modifier.weight(1f)
                        )

                        Text(
                            "DODAJ",
                            fontSize = 30.sp,
                            color = Color.White,
                            modifier = Modifier.clickable { onCreateClick() }
                        )
                        Spacer(modifier = Modifier.width(15.dp))
                    }
                }
            )
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState
            ) { snackbarData ->
                Snackbar(
                    modifier = Modifier
                        .padding(16.dp)
                        .height(80.dp),
                    backgroundColor = Color.DarkGray,
                    contentColor = Color.White
                ) {
                    Text(
                        text = snackbarData.message,
                        fontSize = 28.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

    ) { padding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                }
        ) {

            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
                    .background(DarkBlue.copy(alpha = 0.1f))
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Szukaj materiału...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            keyboardController?.hide()
                            focusManager.clearFocus()
                        }
                    ),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = DarkBlue,
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = DarkBlue,
                        unfocusedLabelColor = Color.Gray,
                        cursorColor = Color.Black
                    ),
                    shape = RoundedCornerShape(10)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Checkbox(
                        checked = state.hideExamples,
                        onCheckedChange = { checked ->
                            viewModel.onEvent(MaterialsListEvent.ToggleHideExamples(checked))
                        },
                        colors = CheckboxDefaults.colors(
                            checkedColor = DarkBlue,
                            uncheckedColor = Color.Gray,
                            checkmarkColor = Color.White
                        )
                    )
                    Text(
                        text = "UKRYJ PRZYKŁADOWE\n MATERIAŁY",
                        fontSize = 17.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(35.dp))
                    IconButton(
                        onClick = { showHideExamplesInfo = true },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Informacja",
                            tint = Color.Gray
                        )
                    }
                }

                if (showHideExamplesInfo) {
                    AlertDialog(
                        onDismissRequest = { showHideExamplesInfo = false },
                        title = { Text("Ukryj przykładowe materiały") },
                        text = {
                            Text(
                                "Zaznacz tę opcję, aby na liście nie wyświetlały się materiały oznaczone jako przykładowe. Podczas dodawania materiałów do kroku uczenia również nie będzie ich widać."
                            )
                        },
                        confirmButton = {
                            TextButton(onClick = { showHideExamplesInfo = false }) {
                                Box(
                                    modifier = Modifier
                                        .clickable(
                                            indication = null,
                                            interactionSource = remember { MutableInteractionSource() }
                                        ) {
                                            showHideExamplesInfo = false
                                        }
                                ) {
                                    Text(
                                        text = "OK",
                                        color = DarkBlue,
                                        fontSize = 18.sp,
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
                            }
                        }
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Text("NAZWA MATERIAŁU", fontSize = 20.sp, color = Color.Gray, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Text("AKCJE", fontSize = 20.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                }

                val listState = rememberLazyListState()
                val scrollAreaState = rememberScrollAreaState(listState)

                LaunchedEffect(state.selectedIndex, searchQuery, state.hideExamples) {
                    state.selectedIndex?.let { selectedIdx ->
                        if (selectedIdx >= 0) {
                            val selectedMaterial = state.materials.getOrNull(selectedIdx)
                            selectedMaterial?.let { material ->
                                val filteredMaterials = state.materials.filter {
                                    (it.name.contains(searchQuery, ignoreCase = true) ||
                                            it.category.contains(searchQuery, ignoreCase = true)) &&
                                            (!state.hideExamples || !it.isExample)
                                }
                                val filteredIndex = filteredMaterials.indexOf(material)

                                if (filteredIndex >= 0) {
                                    listState.animateScrollToItem(filteredIndex)
                                }
                            }
                        }
                    }
                }

                ScrollArea(state = scrollAreaState) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            val filteredMaterials = state.materials.filter {
                                (it.name.contains(searchQuery, ignoreCase = true) ||
                                        it.category.contains(searchQuery, ignoreCase = true)) &&
                                        (!state.hideExamples || !it.isExample)
                            }
                            itemsIndexed(filteredMaterials) { _, material ->
                                val originalIndex = state.materials.indexOf(material)
                                val isSelected = state.selectedIndex == originalIndex

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(if (isSelected) LightBlue.copy(alpha = 0.3f) else Color.Transparent)
                                        .clickable {
                                            viewModel.onEvent(
                                                MaterialsListEvent.SelectMaterial(originalIndex)
                                            )
                                        }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp, horizontal = 12.dp)
                                            .height(55.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.weight(1f),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                material.name,
                                                fontSize = 28.sp,
                                                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
                                            )
                                            if (material.category.isNotBlank()) {
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    "(${material.category})",
                                                    fontSize = 22.sp,
                                                    color = Color.Gray,
                                                    fontWeight = FontWeight.Normal
                                                )
                                            }
                                        }

                                        IconButton(onClick = {
                                            viewModel.onEvent(MaterialsListEvent.CopyRequested(material))
                                        }) {
                                            Icon(
                                                Icons.Default.FileCopy,
                                                contentDescription = "Kopiuj",
                                                tint = DarkBlue,
                                                modifier = Modifier.size(35.dp)
                                            )
                                        }

                                        if (!material.isExample) {
                                            IconButton(onClick = {
                                                onEditClick(material.id)
                                            }) {
                                                Icon(
                                                    Icons.Default.Edit,
                                                    contentDescription = "Edytuj",
                                                    tint = DarkBlue,
                                                    modifier = Modifier.size(35.dp)
                                                )
                                            }
                                        }

                                        if (!material.isExample) {
                                            IconButton(onClick = {
                                                viewModel.onEvent(
                                                    MaterialsListEvent.RequestDelete(
                                                        originalIndex,
                                                        material
                                                    )
                                                )
                                            }) {
                                                Icon(
                                                    Icons.Default.Delete,
                                                    contentDescription = "Usuń",
                                                    tint = DarkBlue,
                                                    modifier = Modifier.size(35.dp)
                                                )
                                            }
                                        }
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

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(2f)
                    .padding(16.dp)
            ) {
                if (state.selectedIndex != null && state.selectedIndex in state.materials.indices) {
                    val word = state.materials[state.selectedIndex!!]
                    val images = state.imagesForSelected[word.id] ?: emptyList()

                    Column(modifier = Modifier.fillMaxSize()) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        ) {
                            items(images) { image ->
                                val painter = rememberAsyncImagePainter(model = image.path)
                                Image(
                                    painter = painter,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(1f)
                                        .clickable { expandedImagePath = image.path }
                                )
                            }
                        }
                    }
                } else {
                    if (state.materials.isEmpty()) {
                        Text(
                            "Brak materiałów do wyświetlenia. Dodaj nowy materiał klikając przycisk DODAJ.",
                            fontSize = 25.sp,
                            color = Color.Black
                        )
                    } else {
                        Text(
                            "Wybierz materiał z listy",
                            fontSize = 25.sp,
                            color = Color.Black
                        )
                    }
                }
            }
        }
    }

    if (expandedImagePath != null) {
        androidx.activity.compose.BackHandler(enabled = true) {
            expandedImagePath = null
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.95f))
                .clickable { expandedImagePath = null },
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = rememberAsyncImagePainter(expandedImagePath),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(16.dp)
                    .clickable { expandedImagePath = null },
                alignment = Alignment.Center,
                contentScale = androidx.compose.ui.layout.ContentScale.Fit
            )
        }
    }

    val materialToDelete = state.materialToDelete
    if (state.showDeleteDialog && materialToDelete != null) {
        materialToDelete.let { (_, name) ->
            YesNoDialog(
                show = true,
                message = "Czy na pewno chcesz usunąć materiał: ${name.name}?",
                onConfirm = { viewModel.onEvent(MaterialsListEvent.ConfirmDelete) },
                onDismiss = { viewModel.onEvent(MaterialsListEvent.DismissDeleteDialog) }
            )
        }
    }
    val material = state.showUsedInDialogFor
    val configs = state.usedInConfigurations

    if (material != null && configs != null) {
        val configList = configs.joinToString(", ")

        YesNoDialog(
            show = true,
            message = "Ten materiał jest wykorzystywany w tych konfiguracjach: $configList.\n\nCzy na pewno chcesz go usunąć?",
            onConfirm = { viewModel.onEvent(MaterialsListEvent.ConfirmDelete) },
            onDismiss = { viewModel.onEvent(MaterialsListEvent.DismissDeleteDialog) }
        )
    }

    state.showCopyDialogFor?.let { material ->
        YesNoDialog(
            show = true,
            message = "Czy na pewno chcesz skopiować materiał: ${material.name}?",
            onConfirm = { viewModel.onEvent(MaterialsListEvent.ConfirmCopy(material)) },
            onDismiss = { viewModel.onEvent(MaterialsListEvent.DismissCopyDialog) }
        )
    }
}
