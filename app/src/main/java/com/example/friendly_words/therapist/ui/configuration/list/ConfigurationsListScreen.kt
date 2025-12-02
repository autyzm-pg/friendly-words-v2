
package com.example.friendly_words.therapist.ui.configuration.list

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import android.content.Intent
import androidx.compose.ui.platform.LocalContext
import com.example.friendly_words.child_app.main.MainActivityChild
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.composables.core.ScrollArea
import com.composables.core.Thumb
import com.composables.core.VerticalScrollbar
import com.composables.core.rememberScrollAreaState
import com.example.shared.data.entities.Configuration
import com.example.friendly_words.therapist.ui.components.YesNoDialogWithName
import com.example.friendly_words.therapist.ui.main.calculateResponsiveFontSize
import com.example.friendly_words.therapist.ui.theme.DarkBlue

@Composable
fun ConfigurationsListScreen(
    navController: NavController,
    onBackClick: () -> Unit,
    onCreateClick: () -> Unit,
    onEditClick: (Long) -> Unit,
    viewModel: ConfigurationViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val lazyListState = rememberLazyListState()
    val stateForScroll = rememberScrollAreaState(lazyListState)
    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        val handle = navController.currentBackStackEntry?.savedStateHandle

        val passedId = handle?.get<Long>("newlyAddedConfigId")
        if (passedId != null) {
            viewModel.onEvent(ConfigurationEvent.SetNewlyAddedId(passedId))
            handle.remove<Long>("newlyAddedConfigId")
        }

        val message = handle?.get<String>("message")
        message?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Short
            )
            handle.remove<String>("message")
        }
    }

    val infoMessage = state.infoMessage
    LaunchedEffect(state.shouldScrollToTop) {
        if (state.shouldScrollToTop) {
            lazyListState.scrollToItem(0)
            viewModel.onEvent(ConfigurationEvent.ScrollToTopHandled)
        }
    }

    LaunchedEffect(infoMessage) {
        infoMessage?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Short
            )
            viewModel.onEvent(ConfigurationEvent.ClearInfoMessage)
        }
    }
    val filteredConfigurations = remember(state.searchQuery, state.configurations, state.hideExamples) {
        state.configurations
            .filter { it.name.contains(state.searchQuery, ignoreCase = true) }
            .filter { if (state.hideExamples) !it.isExample else true }
    }

    LaunchedEffect(state.newlyAddedConfigId, state.configurations.size) {
        val newId = state.newlyAddedConfigId
        if (newId != null && state.configurations.isNotEmpty()) {

            viewModel.onEvent(ConfigurationEvent.SearchChanged(""))
            viewModel.onEvent(ConfigurationEvent.ToggleHideExamples(false))
            kotlinx.coroutines.delay(300)

            val index = state.configurations.indexOfFirst { it.id == newId }

            if (index != -1) {
                lazyListState.scrollToItem(index)
            }

            viewModel.onEvent(ConfigurationEvent.ScrollHandled)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                        Text(
                            "Przyjazne Słowa Ustawienia",
                            fontSize = 30.sp,
                            modifier = Modifier.weight(1f),
                            color = Color.White
                        )
                        Text(
                            "UTWÓRZ",
                            fontSize = 30.sp,
                            color = Color.White,
                            modifier = Modifier.clickable {
                                viewModel.onEvent(ConfigurationEvent.CreateRequested)
                                onCreateClick()
                            }
                        )
                        Spacer(modifier = Modifier.width(15.dp))
                    }
                },
                backgroundColor = DarkBlue
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    OutlinedTextField(
                        value = state.searchQuery,
                        onValueChange = { viewModel.onEvent(ConfigurationEvent.SearchChanged(it)) },
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 56.dp),
                        placeholder = { Text("Wyszukaj", fontSize = calculateResponsiveFontSize(35.sp)) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search Icon", tint = Color.Gray) },
                        singleLine = true,
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
                            cursorColor = Color.Black,
                            focusedLabelColor = DarkBlue,
                            unfocusedLabelColor = Color.Gray
                        ),
                        shape = RoundedCornerShape(10)
                    )

                    Spacer(Modifier.width(12.dp))


                    IconButton(
                        onClick = {
                            val intent = Intent(context, MainActivityChild::class.java).apply {
                                addFlags(
                                    Intent.FLAG_ACTIVITY_NEW_TASK or
                                            Intent.FLAG_ACTIVITY_CLEAR_TASK
                                )

                            }
                            context.startActivity(intent)
                        },
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0B930B).copy(alpha = 0.15f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Uruchom aplikację dziecka",
                            tint = Color(0xFF0B930B),
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Spacer(modifier = Modifier.width(20.dp))
                    var showTooltipName by remember { mutableStateOf(false) }
                    var showTooltipActions by remember { mutableStateOf(false) }

                    Text("NAZWA KROKU UCZENIA", fontSize = 25.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.width(6.dp))
                    Box {
                        Icon(Icons.Default.Info, contentDescription = "Info", tint = Color.Gray,
                            modifier = Modifier.size(40.dp).clickable { showTooltipName = !showTooltipName })
                        if (showTooltipName) {
                            Popup(onDismissRequest = { showTooltipName = false }) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.White)
                                        .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                                        .padding(8.dp)
                                ) {
                                    Text("Zestaw do nauki z dodatkowymi ustawieniami procesu uczenia.", fontSize = 30.sp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.weight(0.7f))
                    Text(
                        "UKRYJ\nPRZYKŁADOWE KROKI",
                        fontSize = 20.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                    )
                    Checkbox(
                        checked = state.hideExamples,
                        onCheckedChange = { viewModel.onEvent(ConfigurationEvent.ToggleHideExamples(it)) },
                        colors = CheckboxDefaults.colors(
                            checkedColor = DarkBlue,
                            uncheckedColor = Color.Gray,
                            checkmarkColor = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text("AKCJE", fontSize = 25.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.width(15.dp))
                    Box {
                        Icon(Icons.Default.Info, contentDescription = "Info", tint = Color.Gray,
                            modifier = Modifier.size(40.dp).clickable { showTooltipActions = !showTooltipActions })
                        if (showTooltipActions) {
                            Popup(onDismissRequest = { showTooltipActions = false }) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.White)
                                        .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                                        .padding(8.dp)
                                ) {
                                    Text("Edytuj, Skopiuj, Usuń", fontSize = 30.sp)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.fillMaxHeight(0.03f))
                ScrollArea(state = stateForScroll) {
                    LazyColumn(
                        state = lazyListState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        items(filteredConfigurations) { config ->
                            val activeConfig = state.activeConfiguration
                            val activeMode =
                                if (activeConfig?.id == config.id) activeConfig.activeMode else null

                            ConfigurationItem(
                                configuration = config,
                                isActive = config.isActive,
                                activeMode = activeMode,
                                onActivate = { mode ->
                                    viewModel.onEvent(
                                        ConfigurationEvent.SetActiveMode(
                                            config,
                                            mode
                                        )
                                    )
                                },
                                onActivateRequest = {
                                    viewModel.onEvent(
                                        ConfigurationEvent.ActivateRequested(
                                            config
                                        )
                                    )
                                },
                                onDeleteRequest = {
                                    viewModel.onEvent(
                                        ConfigurationEvent.DeleteRequested(
                                            config
                                        )
                                    )
                                },
                                onEdit = {
                                    viewModel.onEvent(ConfigurationEvent.EditRequested(config))
                                    onEditClick(config.id)
                                },
                                onCopy = { viewModel.onEvent(ConfigurationEvent.CopyRequested(config)) }
                            )

                            Spacer(modifier = Modifier.height(20.dp))
                        }
                    }
                    VerticalScrollbar(
                        modifier = Modifier.align(Alignment.TopEnd)
                            .fillMaxHeight()
                            .width(8.dp)
                    ) {
                        Thumb(Modifier.background(Color.Gray))
                    }
                }
                state.showDeleteDialogFor?.let { configToDelete ->
                    YesNoDialogWithName(
                        show = true,
                        message = "Czy chcesz usunąć krok uczenia:",
                        name = "${configToDelete.name}?",
                        onConfirm = {
                            viewModel.onEvent(ConfigurationEvent.ConfirmDelete(configToDelete))
                        },
                        onDismiss = { viewModel.onEvent(ConfigurationEvent.DismissDialogs) }
                    )
                }
                state.showCopyDialogFor?.let { configToCopy ->
                    YesNoDialogWithName(
                        show = true,
                        message = "Czy chcesz skopiować krok uczenia:",
                        name = "${configToCopy.name}?",
                        onConfirm = {
                            viewModel.onEvent(ConfigurationEvent.ConfirmCopy(configToCopy))
                        },
                        onDismiss = { viewModel.onEvent(ConfigurationEvent.DismissDialogs) }
                    )
                }
                state.showActivateDialogFor?.let { configToActivate ->
                    YesNoDialogWithName(
                        show = true,
                        info = "Tylko jeden krok uczenia może być aktywny w danym momecnie.",
                        message = "Czy chcesz aktywować krok uczenia:",
                        name = "${configToActivate.name}?",
                        onConfirm = {
                            viewModel.onEvent(ConfigurationEvent.ConfirmActivate(configToActivate))
                        },
                        onDismiss = { viewModel.onEvent(ConfigurationEvent.DismissDialogs) }
                    )
                }
            }
        }
    }
}

@Composable
fun ConfigurationItem(
    configuration: Configuration,
    isActive: Boolean,
    activeMode: String?,
    onActivate: (String) -> Unit,
    onActivateRequest: () -> Unit,
    onDeleteRequest: () -> Unit,
    onEdit: () -> Unit,
    onCopy: () -> Unit
) {
    var switchChecked by remember { mutableStateOf(activeMode == "test") }

    LaunchedEffect(activeMode) {
        switchChecked = activeMode == "test"
    }

    var showExampleInfo by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .background(DarkBlue.copy(alpha = 0.2f)),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1.8f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = isActive,
                        onCheckedChange = {
                            if (!isActive) onActivateRequest()
                        },
                        colors = CheckboxDefaults.colors(
                            checkedColor = DarkBlue,
                            uncheckedColor = Color.Gray,
                            checkmarkColor = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Spacer(modifier = Modifier.height(13.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                configuration.name,
                                fontSize = 30.sp,
                                modifier = Modifier.clickable {
                                    if (!isActive) {
                                        onActivateRequest()
                                    }
                                }
                            )
                            if (configuration.isExample) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Box {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = "Informacja o przykładzie",
                                        tint = Color.Gray,
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clickable { showExampleInfo = !showExampleInfo }
                                    )
                                    if (showExampleInfo) {
                                        Popup(
                                            onDismissRequest = { showExampleInfo = false }
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(Color.White)
                                                    .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                                                    .padding(10.dp)
                                            ) {
                                                Text(
                                                    text = "Edycja przykładowego kroku uczenia nie jest możliwa.\n W celu zobaczenia co jest w środku, zrób kopię przyciskiem obok.",
                                                    fontSize = 20.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            if (isActive) "(aktywny krok w trybie: ${activeMode?.let { if (it == "test") "test dla ucznia" else it }})" else "(krok nieaktywny)",
                            fontSize = 20.sp
                        )

                    }
                }
            }

            Column(
                modifier = Modifier.weight(1.25f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text("Uczenie", fontSize = 18.sp)
                    Switch(
                        checked = switchChecked,
                        onCheckedChange = {
                            switchChecked = it
                            if (isActive) {
                                onActivate(if (it) "test" else "uczenie")
                            }
                        },
                        enabled = isActive,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = DarkBlue,
                            uncheckedThumbColor = DarkBlue
                        )
                    )
                    Text("Test dla ucznia", fontSize = 18.sp)
                }
            }

            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                if (!configuration.isExample) {
                    Spacer(modifier = Modifier.width(20.dp))
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = DarkBlue,
                            modifier = Modifier.size(65.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(15.dp))
                }
                IconButton(onClick = onCopy) {
                    Icon(
                        imageVector = Icons.Default.FileCopy,
                        contentDescription = "Copy",
                        tint = DarkBlue,
                        modifier = Modifier.size(65.dp)
                    )
                }
                if (!configuration.isExample) {
                    Spacer(modifier = Modifier.width(10.dp))
                    IconButton(onClick = onDeleteRequest) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = DarkBlue,
                            modifier = Modifier.size(65.dp)
                        )
                    }
                }
            }
        }
    }
}