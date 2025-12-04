package com.example.friendly_words.therapist.ui.configuration.settings

import com.example.friendly_words.therapist.ui.configuration.learning.ConfigurationLearningEvent
import com.example.friendly_words.therapist.ui.configuration.material.ConfigurationMaterialEvent
import com.example.friendly_words.therapist.ui.configuration.reinforcement.ConfigurationReinforcementEvent
import com.example.friendly_words.therapist.ui.configuration.save.ConfigurationSaveEvent
import com.example.friendly_words.therapist.ui.configuration.test.ConfigurationTestEvent

sealed class ConfigurationSettingsEvent {
    data class Material(val event: ConfigurationMaterialEvent) : ConfigurationSettingsEvent()
    data class Learning(val event: ConfigurationLearningEvent) : ConfigurationSettingsEvent()
    data class Reinforcement(val event: ConfigurationReinforcementEvent) :
        ConfigurationSettingsEvent()

    data class Test(val event: ConfigurationTestEvent) : ConfigurationSettingsEvent()
    data class Save(val event: ConfigurationSaveEvent) : ConfigurationSettingsEvent()
    data object ShowExitDialog : ConfigurationSettingsEvent()
    data object CancelExitDialog : ConfigurationSettingsEvent()
    data object ConfirmExitDialog : ConfigurationSettingsEvent()
    object ResetNavigation : ConfigurationSettingsEvent()
}