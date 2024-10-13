package org.jetbrains.qodana.staticAnalysis.workflow

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.annotations.ApiStatus
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaConfig
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaRunContext

@ApiStatus.Internal
interface QodanaWorkflowExtension {
  companion object {
    val EP_NAME: ExtensionPointName<QodanaWorkflowExtension> = ExtensionPointName("org.intellij.qodana.workflowExtension")

    suspend fun callBeforeProjectOpened(config: QodanaConfig) = invokeAllWorkflowExtensions { beforeProjectOpened(config) }

    suspend fun callAfterConfiguration(config: QodanaConfig, project: Project) =
      invokeAllWorkflowExtensions { afterConfiguration(config, project) }

    suspend fun callBeforeLaunch(context: QodanaRunContext) = invokeAllWorkflowExtensions { beforeLaunch(context) }

    suspend fun callBeforeProjectClose(project: Project) = invokeAllWorkflowExtensions { beforeProjectClose(project) }

    suspend fun callAfterProjectClosed(config: QodanaConfig) = invokeAllWorkflowExtensions { afterProjectClosed(config) }

    private suspend fun invokeAllWorkflowExtensions(action: suspend QodanaWorkflowExtension.() -> Unit) {
      coroutineScope {
        val isHeadless = ApplicationManager.getApplication().isHeadlessEnvironment
        EP_NAME.extensionList
          .filter { !it.requireHeadless || isHeadless }
          .forEach { launch { it.action() } }
      }
    }
  }

  // Workaround: Some extensions are only applicable when not running from within the IDE,
  // but creating a clear lifecycle for qodana is a large task on its own.
  val requireHeadless: Boolean get() = false

  suspend fun beforeProjectOpened(config: QodanaConfig) {}

  suspend fun afterConfiguration(config: QodanaConfig, project: Project) {}

  suspend fun beforeLaunch(context: QodanaRunContext) {}

  suspend fun beforeProjectClose(project: Project) {}

  suspend fun afterProjectClosed(config: QodanaConfig) {}
}
