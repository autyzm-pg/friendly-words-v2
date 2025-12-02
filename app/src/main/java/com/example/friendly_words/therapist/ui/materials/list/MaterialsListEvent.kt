package com.example.friendly_words.therapist.ui.materials.list

import com.example.shared.data.entities.Resource

sealed class MaterialsListEvent {
    data class SelectMaterial(val index: Int) : MaterialsListEvent()
    data class RequestDelete(val index: Int, val resource: Resource) : MaterialsListEvent()
    object ConfirmDelete : MaterialsListEvent()
    object DismissDeleteDialog : MaterialsListEvent()
    object ClearInfoMessage : MaterialsListEvent()
    data class SelectByResourceId(val resourceId: Long) : MaterialsListEvent()
    data class ToggleHideExamples(val hide: Boolean) : MaterialsListEvent()
    data class CopyRequested(val resource: Resource) : MaterialsListEvent()
    data class ConfirmCopy(val resource: Resource) : MaterialsListEvent()
    object DismissCopyDialog : MaterialsListEvent()
    data class ShowUsedInConfigurations(val resource: Resource, val configurations: List<String>) : MaterialsListEvent()
}