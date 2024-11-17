package org.jetbrains.qodana.staticAnalysis

import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.util.PlatformUtils
import com.intellij.util.application

abstract class QodanaLinterProjectActivity : ProjectActivity {
  final override suspend fun execute(project: Project) {
    if (application.isHeadlessEnvironment && PlatformUtils.isQodana()) {
      run(project)
    }
  }

  abstract suspend fun run(project: Project)
}