package org.jetbrains.qodana.staticAnalysis.sarif

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project
import com.jetbrains.qodana.sarif.model.Run
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaConfig
import org.jetbrains.qodana.util.QodanaMessageReporter

interface SarifReportContributor {
  fun contribute(run: Run, project: Project, config: QodanaConfig, messageReporter: QodanaMessageReporter)

  companion object {
    fun runContributors(run: Run, project: Project, config: QodanaConfig, messageReporter: QodanaMessageReporter) {
      EP_NAME.forEachExtensionSafe { contributor ->
        contributor.contribute(run, project, config, messageReporter)
      }
    }

    private val EP_NAME = ExtensionPointName.create<SarifReportContributor>("org.intellij.qodana.sarifReportContributor")
  }
}
