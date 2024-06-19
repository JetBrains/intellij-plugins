package com.intellij.dts.settings

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project

interface DtsSettingsDisabler {
  companion object {
    private val EP_NAME = ExtensionPointName.create<DtsSettingsDisabler>("com.intellij.dts.settings.disabler")

    internal fun shouldBeDisabled(project: Project): Boolean = EP_NAME.extensionList.any { it.disableSettings(project) }
  }

  fun disableSettings(project: Project): Boolean
}
