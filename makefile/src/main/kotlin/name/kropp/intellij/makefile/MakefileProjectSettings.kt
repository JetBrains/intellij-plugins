package name.kropp.intellij.makefile

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.StoragePathMacros

@State(name = "Make.Settings", storages = arrayOf(Storage(StoragePathMacros.WORKSPACE_FILE)))
class MakefileProjectSettings : PersistentStateComponent<MakeSettings> {
  var settings: MakeSettings? = MakeSettings()

  override fun getState() = settings

  override fun loadState(state: MakeSettings?) {
    settings = state
  }
}