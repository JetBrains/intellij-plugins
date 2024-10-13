package org.jetbrains.qodana.jvm.java

import com.intellij.ide.CommandLineInspectionProjectConfigurator
import com.intellij.openapi.application.writeAction
import com.intellij.openapi.components.service
import com.intellij.openapi.progress.runBlockingCancellable
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import org.jetbrains.qodana.QodanaBundle

class JdkConfigurator : CommandLineInspectionProjectConfigurator {
  override fun getName(): String {
    return "qodanaProjectJdkConfigurator"
  }

  override fun getDescription(): String {
    return QodanaBundle.message("progress.message.qodana.jdk.configuring")
  }

  override fun preConfigureProject(project: Project, context: CommandLineInspectionProjectConfigurator.ConfiguratorContext) {
    runBlockingCancellable {
      val jdk = service<QodanaConfigJdkService>().getJdk()
      if (jdk != null) {
        writeAction {
          ProjectRootManager.getInstance(project).projectSdk = jdk
        }
      }
    }
  }
}