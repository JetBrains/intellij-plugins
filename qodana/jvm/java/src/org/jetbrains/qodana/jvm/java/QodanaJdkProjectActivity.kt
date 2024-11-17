package org.jetbrains.qodana.jvm.java

import com.intellij.openapi.application.writeAction
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.platform.backend.observation.trackActivity
import org.jetbrains.qodana.staticAnalysis.QodanaLinterProjectActivity
import org.jetbrains.qodana.staticAnalysis.inspections.runner.ConsoleLog
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaActivityKey

class QodanaJdkProjectActivity : QodanaLinterProjectActivity() {
  override suspend fun run(project: Project) {
    project.trackActivity(QodanaActivityKey) {
      configureJdk(project)
    }
  }

  private suspend fun configureJdk(project: Project) {
    val sdk = service<QodanaConfigJdkService>().deferredSdk.await() ?: return
    ConsoleLog.info("Setting project JDK ${sdk.name}")
    writeAction {
      ProjectRootManager.getInstance(project).projectSdk = sdk
    }
  }
}