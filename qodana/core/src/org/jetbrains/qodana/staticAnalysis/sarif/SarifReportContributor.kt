package org.jetbrains.qodana.staticAnalysis.sarif

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project
import com.jetbrains.qodana.sarif.model.Run
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaConfig

interface SarifReportContributor {
  fun contribute(run: Run, project: Project, config: QodanaConfig)

  companion object {
    fun runContributors(run: Run, project: Project, config: QodanaConfig) {
      for (contributor in EP_NAME.extensionList) {
        contributor.contribute(run, project, config)
      }
    }

    val EP_NAME = ExtensionPointName.create<SarifReportContributor>("org.intellij.qodana.sarifReportContributor")
  }
}
