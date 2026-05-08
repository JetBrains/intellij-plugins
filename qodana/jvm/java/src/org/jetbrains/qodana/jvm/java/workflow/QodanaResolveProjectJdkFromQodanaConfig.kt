package org.jetbrains.qodana.jvm.java.workflow

import com.intellij.openapi.components.service
import org.jetbrains.qodana.jvm.java.QodanaConfigJdkService
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaConfig
import org.jetbrains.qodana.staticAnalysis.workflow.QodanaWorkflowExtension

class QodanaResolveProjectJdkFromQodanaConfig : QodanaWorkflowExtension {
  override suspend fun beforeProjectOpened(config: QodanaConfig) {
    service<QodanaConfigJdkService>().configureJdk(config)
  }
}