package com.jetbrains.cidr.cpp.embedded.platformio.project

import com.intellij.openapi.externalSystem.settings.ExternalProjectSettings

class PlatformioProjectSettings private constructor() : ExternalProjectSettings() {
  override fun clone(): ExternalProjectSettings {
    val copy = PlatformioProjectSettings()
    copyTo(copy)
    return copy
  }

  companion object {
    fun default(): PlatformioProjectSettings = PlatformioProjectSettings().apply { setupNewProjectDefault() }
  }
}