package com.jetbrains.lang.makefile

import com.intellij.openapi.components.*

@Service(Service.Level.PROJECT)
@State(name = "Make.Settings", storages = [Storage(StoragePathMacros.WORKSPACE_FILE)])
class MakefileProjectSettings : PersistentStateComponent<MakeSettings> {
  var settings: MakeSettings? = MakeSettings()

  override fun getState() = settings

  override fun loadState(state: MakeSettings) {
    settings = state
  }
}