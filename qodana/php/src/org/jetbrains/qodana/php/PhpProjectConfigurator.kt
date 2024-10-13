package org.jetbrains.qodana.php

import com.intellij.ide.CommandLineInspectionProjectAsyncConfigurator
import com.intellij.ide.CommandLineInspectionProjectConfigurator
import com.intellij.openapi.project.Project
import org.jetbrains.qodana.QodanaBundle

class PhpProjectConfigurator : CommandLineInspectionProjectAsyncConfigurator {
  override fun getName(): String {
    return "qodanaPhpProjectConfigurator"
  }

  override fun getDescription(): String {
    return QodanaBundle.message("progress.message.qodana.php.composer.configuring")
  }

  override suspend fun configureProjectAsync(project: Project, context: CommandLineInspectionProjectConfigurator.ConfiguratorContext) {
    configurePhpProject(project)
  }
}
