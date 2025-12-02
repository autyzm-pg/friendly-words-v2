package com.example.friendly_words.therapist.ui.configuration.settings

import com.example.shared.data.another.ConfigurationLearningState
import com.example.shared.data.another.ConfigurationMaterialState
import com.example.shared.data.another.ConfigurationReinforcementState
import com.example.friendly_words.therapist.ui.configuration.save.ConfigurationSaveState
import com.example.shared.data.another.ConfigurationTestState

data class ConfigurationSettingsState (
    val materialState: ConfigurationMaterialState = ConfigurationMaterialState(),
    val learningState: ConfigurationLearningState = ConfigurationLearningState(),
    val reinforcementState: ConfigurationReinforcementState = ConfigurationReinforcementState(),
    val testState: ConfigurationTestState = ConfigurationTestState(),
    val saveState: ConfigurationSaveState = ConfigurationSaveState(),
    val showExitDialog: Boolean = false,
    val message: String? = null,
    val navigateToList: Boolean = false,
    val lastSavedConfigId: Long? = null,
    val hideExamples: Boolean = false
)