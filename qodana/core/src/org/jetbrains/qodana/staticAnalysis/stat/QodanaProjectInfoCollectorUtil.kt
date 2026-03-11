package org.jetbrains.qodana.staticAnalysis.stat

import com.intellij.openapi.project.Project

object QodanaProjectInfoCollectorUtil {
  fun logOssLicense(project: Project, hasOssLicense: Boolean) {
    QodanaProjectInfoCollector.logOssLicense(project, hasOssLicense)
  }
}
