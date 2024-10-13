package org.jetbrains.qodana.php

import com.intellij.openapi.project.Project
import com.intellij.platform.backend.observation.trackActivity
import org.jetbrains.qodana.staticAnalysis.QodanaLinterProjectActivity
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaActivityKey

class QodanaPhpProjectActivity : QodanaLinterProjectActivity() {
  override suspend fun run(project: Project) {
    project.trackActivity(QodanaActivityKey) {
      configurePhpProject(project)
    }
  }
}