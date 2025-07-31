package org.jetbrains.qodana.cpp

import com.intellij.openapi.project.Project
import com.jetbrains.cidr.lang.workspace.OCWorkspace
import kotlinx.coroutines.channels.Channel
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaConfig
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaException
import org.jetbrains.qodana.staticAnalysis.workflow.QodanaWorkflowExtension

class QodanaCppWorkflow : QodanaWorkflowExtension {
  companion object {
    private val delayedErrors = Channel<QodanaException>(Channel.CONFLATED)

    fun failLater(message: String) {
      failLater(QodanaException(message))
    }

    fun failLater(exception: QodanaException) {
      delayedErrors.trySend(exception)
    }
  }

  override suspend fun afterConfiguration(config: QodanaConfig, project: Project) {
    delayedErrors.close()
    for (err in delayedErrors) {
      throw err
    }

    if (OCWorkspace.getInstance(project).configurations.isEmpty()) {
      // Headless startup may exit with a warning and no OC workspace configurations
      throw QodanaException("Failed to calculate analysis scope from build configuration.")
    }
  }
}