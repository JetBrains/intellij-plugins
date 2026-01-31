package com.jetbrains.cidr.cpp.embedded.platformio.project

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.StoragePathMacros
import com.intellij.openapi.externalSystem.settings.AbstractExternalSystemLocalSettings
import com.intellij.openapi.project.Project
import java.io.Serializable

@State(name = "PlatformioLocalSettings",
       storages = [(Storage(StoragePathMacros.WORKSPACE_FILE))])
@Service(Service.Level.PROJECT)
class PlatformioLocalSettings(val project: Project) :
  AbstractExternalSystemLocalSettings<PlatformioLocalSettingsState>(ID, project, PlatformioLocalSettingsState()),
  PersistentStateComponent<PlatformioLocalSettingsState> {

  override fun loadState(state: PlatformioLocalSettingsState) {
  }
}

class PlatformioLocalSettingsState : AbstractExternalSystemLocalSettings.State(), Serializable