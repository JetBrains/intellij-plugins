package org.jetbrains.qodana.staticAnalysis.inspections.injections

import com.intellij.openapi.project.Project
import com.intellij.util.SystemProperties
import com.intellij.util.containers.mapInPlace
import org.intellij.plugins.intelliLang.Configuration
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaConfig
import org.jetbrains.qodana.staticAnalysis.workflow.QodanaWorkflowExtension

class QodanaDisableHtmlInJsInjections : QodanaWorkflowExtension {
  companion object {
    private const val USE_DEFAULT_LIST_KEY = "qodana.use.default.injections.list"
  }

  override suspend fun configureForQodana(config: QodanaConfig, project: Project) {
    if (SystemProperties.getBooleanProperty(USE_DEFAULT_LIST_KEY, false)) return

    Configuration.getProjectInstance(project).getInjections("js")
      .firstOrNull { it.displayName == "HTML in JS strings" }
      ?.injectionPlaces?.mapInPlace { place -> place.enabled(false) }
  }
}
