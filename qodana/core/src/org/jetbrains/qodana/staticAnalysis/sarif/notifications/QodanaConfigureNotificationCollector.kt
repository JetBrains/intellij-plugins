package org.jetbrains.qodana.staticAnalysis.sarif.notifications

import com.intellij.openapi.components.serviceAsync
import com.intellij.openapi.project.Project
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaConfig
import org.jetbrains.qodana.staticAnalysis.workflow.QodanaWorkflowExtension

internal class QodanaConfigureNotificationCollector : QodanaWorkflowExtension {
  override suspend fun configureForQodana(config: QodanaConfig, project: Project) {
    project.serviceAsync<RuntimeNotificationCollector>().initializeForRun(config)
  }
}