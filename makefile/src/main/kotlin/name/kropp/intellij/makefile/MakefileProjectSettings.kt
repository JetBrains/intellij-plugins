package name.kropp.intellij.makefile

import com.intellij.openapi.components.*

@State(name = "Make.Settings", storages = [Storage(StoragePathMacros.WORKSPACE_FILE)])
class MakefileProjectSettings : PersistentStateComponent<MakeSettings> {
  var settings: MakeSettings? = MakeSettings()

  override fun getState() = settings

  override fun loadState(state: MakeSettings) {
    settings = state
  }
}