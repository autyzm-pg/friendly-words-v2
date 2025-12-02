package com.example.friendly_words.therapist.ui.configuration.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.friendly_words.therapist.ui.components.NewConfigurationTopBar
import com.example.friendly_words.therapist.ui.components.NewConfigurationTopTabs
import com.example.friendly_words.therapist.ui.components.YesNoDialog
import com.example.friendly_words.therapist.ui.configuration.learning.ConfigurationLearningScreen
import com.example.friendly_words.therapist.ui.configuration.material.ConfigurationMaterialScreen
import com.example.friendly_words.therapist.ui.configuration.reinforcement.ConfigurationReinforcementScreen
import com.example.friendly_words.therapist.ui.configuration.save.ConfigurationSaveScreen
import com.example.friendly_words.therapist.ui.configuration.test.ConfigurationTestEvent
import com.example.friendly_words.therapist.ui.configuration.test.ConfigurationTestScreen

@Composable
fun ConfigurationSettingsScreen(
    navController: NavController,
    onBackClick: () -> Unit,
    configId: Long? = null,
    viewModel: ConfigurationSettingsViewModel = hiltViewModel()
) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.navigateToList) {
        if (configId != null) {
            viewModel.loadConfiguration(configId)
        }
        if (state.navigateToList) {
            navController.previousBackStackEntry?.savedStateHandle?.let { handle ->
                state.lastSavedConfigId?.let { id ->
                    handle["newlyAddedConfigId"] = id
                }
                state.message?.let { msg ->
                    handle["message"] = msg
                }
            }
            onBackClick()
            viewModel.onEvent(ConfigurationSettingsEvent.ResetNavigation)
        }
    }

    if (state.showExitDialog) {
        YesNoDialog(
            show = true,
            message = "Czy chcesz wyjść bez zapisywania?",
            onConfirm = {
                viewModel.onEvent(ConfigurationSettingsEvent.ConfirmExitDialog)
                onBackClick()
            },
            onDismiss = { viewModel.onEvent(ConfigurationSettingsEvent.CancelExitDialog) }
        )
    }

    Scaffold(
        topBar = {
            NewConfigurationTopBar(
                title = "Nowy krok uczenia: ${state.saveState.stepName.text}",
                onBackClick = { viewModel.onEvent(ConfigurationSettingsEvent.ShowExitDialog) }
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            NewConfigurationTopTabs(
                selectedTabIndex = selectedTabIndex,
                onTabSelected = { selectedTabIndex = it }
            )

            when (selectedTabIndex) {
                0 -> ConfigurationMaterialScreen(
                    state = state.materialState,
                    hideExamples = state.hideExamples,
                    onEvent = { viewModel.onEvent(ConfigurationSettingsEvent.Material(it)) },
                    onBackClick = onBackClick
                )

                1 -> {
                    val material = state.materialState

                    val availableForLearning = material.vocabItems
                        .count { item -> item.inLearningStates.any { it == true } }
                        .coerceAtMost(6)

                    ConfigurationLearningScreen(
                        state = state.learningState,
                        availableImagesForLearning = availableForLearning,
                        onEvent = { viewModel.onEvent(ConfigurationSettingsEvent.Learning(it)) },
                        onBackClick = onBackClick
                    )
                }

                2 -> ConfigurationReinforcementScreen(
                    state = state.reinforcementState,
                    onEvent = { viewModel.onEvent(ConfigurationSettingsEvent.Reinforcement(it)) },
                    onBackClick = onBackClick
                )

                3 -> {
                    val material = state.materialState
                    val testState = state.testState
                    val learningState = state.learningState

                    val availableForLearning = material.vocabItems
                        .count { item -> item.inLearningStates.any { it == true } }
                        .coerceAtMost(6)

                    val availableForTest = material.vocabItems
                        .count { item -> item.inTestStates.any { it == true } }
                        .coerceAtMost(6)

                    LaunchedEffect(Unit) {
                        if (!testState.testEditEnabled) {
                            viewModel.onEvent(
                                ConfigurationSettingsEvent.Test(
                                    ConfigurationTestEvent.SetEditEnabled(false)
                                )
                            )
                        }
                    }

                    ConfigurationTestScreen(
                        state = testState,
                        availableImagesForTest = availableForTest,
                        availableImagesForLearning = availableForLearning,
                        learningImageCount = learningState.imageCount,
                        onEvent = { viewModel.onEvent(ConfigurationSettingsEvent.Test(it)) },
                        onBackClick = onBackClick
                    )
                }

                4 -> ConfigurationSaveScreen(
                    materialState = state.materialState,
                    learningState = state.learningState,
                    reinforcementState = state.reinforcementState,
                    testState = state.testState,
                    saveState = state.saveState,
                    onEvent = { viewModel.onEvent(ConfigurationSettingsEvent.Save(it)) },
                    onSettingsEvent = { viewModel.onEvent(it) },
                    onBackClick = onBackClick
                )
            }
        }
    }
}