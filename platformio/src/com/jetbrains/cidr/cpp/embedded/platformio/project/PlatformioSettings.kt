package com.jetbrains.cidr.cpp.embedded.platformio.project

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.externalSystem.model.settings.ExternalSystemExecutionSettings
import com.intellij.openapi.externalSystem.settings.AbstractExternalSystemSettings
import com.intellij.openapi.externalSystem.settings.ExternalSystemSettingsListener
import com.intellij.openapi.project.Project
import com.intellij.util.messages.Topic
import com.intellij.util.xmlb.annotations.XCollection


@State(name = "PlatformIOSettings")
@Service(Service.Level.PROJECT)
class PlatformioSettings(project: Project) :
  AbstractExternalSystemSettings<PlatformioSettings, PlatformioProjectSettings, PlatformioSettingsListener>(
    PlatformioSettingsListener.TOPIC,
    project),
  PersistentStateComponent<PlatformioSettingsState> {

  override fun subscribe(listener: ExternalSystemSettingsListener<PlatformioProjectSettings>, parentDisposable: Disposable) {
  }

  override fun copyExtraSettingsFrom(settings: PlatformioSettings) {}

  override fun checkSettings(old: PlatformioProjectSettings, current: PlatformioProjectSettings) {
  }

  override fun getState(): PlatformioSettingsState {
    val state = PlatformioSettingsState()
    fillState(state)
    return state
  }

  override fun loadState(state: PlatformioSettingsState) = super.loadState(state)

}

class PlatformioSettingsState : AbstractExternalSystemSettings.State<PlatformioProjectSettings> {
  private var mySettings: MutableSet<PlatformioProjectSettings>? = HashSet()

  @XCollection(elementTypes = [(PlatformioProjectSettings::class)])
  override fun getLinkedExternalProjectsSettings(): Set<PlatformioProjectSettings>? = mySettings

  override fun setLinkedExternalProjectsSettings(settings: MutableSet<PlatformioProjectSettings>?) {
    mySettings = settings
  }
}

interface PlatformioSettingsListener : ExternalSystemSettingsListener<PlatformioProjectSettings> {
  companion object {
    val TOPIC: Topic<PlatformioSettingsListener> = Topic.create("PlatformIO specific settings",
                                                                PlatformioSettingsListener::class.java)
  }
}

class PlatformioExecutionSettings : ExternalSystemExecutionSettings()