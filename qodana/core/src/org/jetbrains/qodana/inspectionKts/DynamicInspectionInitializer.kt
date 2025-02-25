package org.jetbrains.qodana.inspectionKts

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaMessageReporter

/**
 * Implementations of this EP responsible of initialization of so called "dynamic" inspections,
 * those that have to be loaded before loading inspection profile to make it possible to operate
 * with those inspections and their settings
 */
interface DynamicInspectionInitializer {
  companion object {
    val EP_NAME: ExtensionPointName<DynamicInspectionInitializer> = ExtensionPointName("org.intellij.qodana.dynamicInspectionsInitializer")

    internal suspend fun waitForDynamicInspectionsInitialization(project: Project, messageReporter: QodanaMessageReporter) {
      invokeAllInitializers(project, messageReporter)
    }

    private suspend fun invokeAllInitializers(project: Project, messageReporter: QodanaMessageReporter) {
      coroutineScope {
        EP_NAME.extensionList.forEach {
          launch { it.initialize(project, messageReporter) }
        }
      }
    }
  }

  suspend fun initialize(project: Project, messageReporter: QodanaMessageReporter)
}