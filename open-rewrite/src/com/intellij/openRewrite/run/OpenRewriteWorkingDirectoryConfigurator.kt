package com.intellij.openRewrite.run

import com.intellij.execution.util.ProgramParametersConfigurator
import com.intellij.openapi.externalSystem.ExternalSystemModulePropertyManager
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project

internal class OpenRewriteWorkingDirectoryConfigurator(private val project: Project, private val module: Module?) : ProgramParametersConfigurator() {
  fun getWorkingDirectory(): String? {
    if (module == null ||
        module.isDisposed ||
        ExternalSystemModulePropertyManager.getInstance(module).getExternalSystemId() == null) {
      return getDefaultWorkingDir(project)
    }
    return getDefaultWorkingDir(module) ?: getDefaultWorkingDir(project)
  }
}