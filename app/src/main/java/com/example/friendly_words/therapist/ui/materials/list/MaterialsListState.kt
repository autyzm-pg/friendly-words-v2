package com.example.friendly_words.therapist.ui.materials.list

import com.example.shared.data.entities.Image
import com.example.shared.data.entities.Resource

data class MaterialsListState(
    val materials: List<Resource> = emptyList(),
    val selectedIndex: Int? = 0,
    val showDeleteDialog: Boolean = false,
    val materialToDelete: Pair<Int, Resource>? = null,
    val imagesForSelected: Map<Long, List<Image>> = emptyMap(),
    val pendingSelectId: Long? = null,
    val infoMessage: String? = null,
    val hideExamples: Boolean = false,
    val showCopyDialogFor: Resource? = null,
    val usedInSteps: List<String>? = null,
    val usedInConfigurations: List<String>? = null,
    val showUsedInDialogFor: Resource? = null
)