package org.jetbrains.qodana.extensions

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project

interface QodanaHighlightingSupportInfoProvider {
  companion object {
    private val EP_NAME: ExtensionPointName<QodanaHighlightingSupportInfoProvider> =
      ExtensionPointName.create("org.intellij.qodana.qodanaHighlightingSupportInfoProvider")

    suspend fun getEnabledInspections(project: Project): Set<String> {
      val result = mutableSetOf<String>()
      for (e in EP_NAME.extensionList) {
        result.addAll(e.getEnabledInspections(project))
      }
      return result
    }

    fun getPrecedingPassesIds() : List<Int> {
      val result = mutableListOf<Int>()
      for (e in EP_NAME.extensionList) {
        result.addAll(e.getPrecedingPassesIds())
      }
      return result
    }
  }

  suspend fun getEnabledInspections(project: Project): Set<String>

  fun getPrecedingPassesIds(): List<Int>
}