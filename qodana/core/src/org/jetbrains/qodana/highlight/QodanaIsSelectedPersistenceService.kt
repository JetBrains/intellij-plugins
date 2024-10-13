package org.jetbrains.qodana.highlight

import com.intellij.openapi.components.*
import com.intellij.openapi.project.Project

// performance reasons
@State(name = "QodanaIsSelectedPersistenceService", storages = [Storage(value = StoragePathMacros.WORKSPACE_FILE)])
@Service(Service.Level.PROJECT)
class QodanaIsSelectedPersistenceService(@Suppress("unused") private val project: Project)
  : SimplePersistentStateComponent<QodanaIsSelectedPersistenceService.State>(State()) {
  companion object {
    fun getInstance(project: Project): QodanaIsSelectedPersistenceService = project.service()
  }

  var isSelectedOrLoading: Boolean
    get() {
      return state.isSelectedOrLoading
    }
    set(value) {
      state.isSelectedOrLoading = value
    }


  class State : BaseState() {
    var isSelectedOrLoading by property(false)
  }
}