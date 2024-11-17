package org.jetbrains.qodana.staticAnalysis.inspections.injections

import com.intellij.openapi.project.Project
import com.intellij.util.containers.mapInPlace
import org.intellij.plugins.intelliLang.Configuration
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaConfig
import org.jetbrains.qodana.staticAnalysis.workflow.QodanaWorkflowExtension

class CodeInjectionManager: QodanaWorkflowExtension {

  private val DISABLED_INJECTIONS_LIST = listOf(Injection("js", "HTML in JS strings"))
  private val USE_DEFAULT_LIST_KEY = "qodana.use.default.injections.list"

  override suspend fun afterConfiguration(config: QodanaConfig, project: Project) {
    disableInjections(project)
  }

  private fun disableInjections(project: Project) {
    if (System.getProperty(USE_DEFAULT_LIST_KEY, "false").toBoolean()) return
    val injectionService = Configuration.getProjectInstance(project)
    val injectionToBeDisabled = DISABLED_INJECTIONS_LIST
      .mapNotNull { injectionService.getInjections(it.injectorId).firstOrNull { injection -> injection.displayName == it.displayName } }
    injectionToBeDisabled.forEach { injection ->
      injection.injectionPlaces.mapInPlace { place -> place.enabled(false) }
    }
  }

  private data class Injection(val injectorId: String, val displayName: String)

}
