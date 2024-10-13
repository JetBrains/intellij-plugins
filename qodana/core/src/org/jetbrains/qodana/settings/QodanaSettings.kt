// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.qodana.settings

import com.intellij.openapi.application.Application
import com.intellij.openapi.components.*
import com.intellij.openapi.project.Project
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

internal fun Application.qodanaSettings(): QodanaSettings {
  return service()
}

internal fun Project.qodanaSettings(): QodanaPerProjectSettings {
  return service()
}

@State(name = "QodanaSettings", storages = [Storage("qodana.settings.xml", roamingType = RoamingType.DISABLED)])
@Service(Service.Level.APP)
internal class QodanaSettings : PersistentStateComponent<QodanaSettingsState> {
  @Volatile
  var showPromo: Boolean = true

  override fun getState(): QodanaSettingsState {
    val state = QodanaSettingsState()
    state.showPromo = this.showPromo
    return state
  }

  override fun loadState(state: QodanaSettingsState) {
    this.showPromo = state.showPromo
  }
}

internal class QodanaSettingsState : BaseState() {
  var showPromo by property(true)
}


@State(name = "QodanaSettings", storages = [Storage(value = StoragePathMacros.WORKSPACE_FILE, roamingType = RoamingType.DEFAULT)])
@Service(Service.Level.PROJECT)
internal class QodanaPerProjectSettings(@Suppress("UNUSED_PARAMETER") project: Project) : PersistentStateComponent<QodanaPerProjectSettingsState> {
  private val _loadMatchingCloudReportAutomatically = MutableStateFlow(false)
  val loadMatchingCloudReportAutomatically: StateFlow<Boolean> = _loadMatchingCloudReportAutomatically.asStateFlow()

  fun setLoadMatchingCloudReportAutomatically(doSet: Boolean) {
    _loadMatchingCloudReportAutomatically.value = doSet
  }

  override fun getState(): QodanaPerProjectSettingsState {
    val state = QodanaPerProjectSettingsState()
    state.autoLoadCloudReport = this.loadMatchingCloudReportAutomatically.value
    return state
  }

  override fun loadState(state: QodanaPerProjectSettingsState) {
    this._loadMatchingCloudReportAutomatically.value = state.autoLoadCloudReport
  }
}

internal class QodanaPerProjectSettingsState : BaseState() {
  var autoLoadCloudReport by property(false)
}