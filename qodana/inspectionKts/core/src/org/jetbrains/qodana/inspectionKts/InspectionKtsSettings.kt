package org.jetbrains.qodana.inspectionKts

import com.intellij.openapi.components.BaseState
import com.intellij.openapi.components.SimplePersistentStateComponent
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project

class InspectionKtsSettingsState : BaseState() {
  val disableInspectionKtsResolve: Boolean by property(false)
}

class InspectionKtsSettings : SimplePersistentStateComponent<InspectionKtsSettingsState>(InspectionKtsSettingsState()) {
  companion object {
    fun getInstance(project: Project): InspectionKtsSettings = project.service()
  }

  val disableInspectionKtsResolve: Boolean
    get() = state.disableInspectionKtsResolve
}
