package com.example.friendly_words.therapist.ui.configuration.save

import androidx.compose.ui.text.input.TextFieldValue

data class ConfigurationSaveState(
    val stepName: TextFieldValue = TextFieldValue(""),
    val showNameError: Boolean = false,
    val editingConfigId: Long? = null,
    val showEmptyNameDialog: Boolean = false,
    val showDuplicateNameDialog: Boolean = false
)