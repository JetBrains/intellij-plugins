package org.jetbrains.qodana.python.community

import com.intellij.openapi.application.readAction
import com.intellij.openapi.diagnostic.fileLogger
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.python.pyproject.model.api.SdkConfigurationResult
import com.intellij.python.pyproject.model.api.configureSdkIfNeeded
import com.jetbrains.python.module.PyModuleService
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaConfig
import org.jetbrains.qodana.staticAnalysis.workflow.QodanaWorkflowExtension

private val logger = fileLogger()

internal class QodanaConfigurePythonProjectSdk : QodanaWorkflowExtension {
  override suspend fun configureForQodana(config: QodanaConfig, project: Project) {
    val modules = ModuleManager.getInstance(project).modules
    when (configureSdkFromQodanaPythonPath(project, modules)) {
      QodanaPythonPathConfigurationResult.NoPath -> Unit
      QodanaPythonPathConfigurationResult.Failure,
      QodanaPythonPathConfigurationResult.Success,
        -> return
    }

    val pythonModules = readAction {
      val moduleService = PyModuleService.getInstance(project)
      modules.filter(moduleService::isPythonModule)
    }
    for (module in pythonModules) {
      when (val result = module.configureSdkIfNeeded()) {
        null -> logger.info("No Python SDK auto-configuration candidate found for module '${module.name}'")
        is SdkConfigurationResult.Configured -> Unit
        is SdkConfigurationResult.NotConfigured ->
          logger.warn("Failed to configure Python SDK for module '${module.name}': ${result.reason}")
        is SdkConfigurationResult.ToolNotInstalled ->
          logger.warn("Failed to configure Python SDK for module '${module.name}': ${result.tool}")
        is SdkConfigurationResult.ParentHasNoSdk ->
          logger.warn("Failed to configure Python SDK for module '${module.name}': ${result.reason}")
      }
    }
  }
}
