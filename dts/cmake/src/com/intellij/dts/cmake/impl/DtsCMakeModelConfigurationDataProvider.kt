package com.intellij.dts.cmake.impl

import com.intellij.execution.ExecutionTargetManager
import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project
import com.jetbrains.cidr.cpp.cmake.model.CMakeModelConfigurationData
import com.jetbrains.cidr.cpp.cmake.workspace.CMakeWorkspace
import com.jetbrains.cidr.cpp.execution.CMakeBuildProfileExecutionTarget

interface DtsCMakeModelConfigurationDataProvider {
  companion object {
    private val EP_NAME = ExtensionPointName.create<DtsCMakeModelConfigurationDataProvider>("com.intellij.dts.cmake.configurationDataProvider")

    fun getFirstCMakeModelConfigurationData(project: Project): CMakeModelConfigurationData? {
      for (extension in EP_NAME.extensionList) {
        val data = extension.getCMakeModelConfigurationData(project)
        if (data != null) {
          return data
        }
      }

      return null
    }
  }

  fun getCMakeModelConfigurationData(project: Project): CMakeModelConfigurationData?
}

class DefaultDtsCMakeProfileProvider : DtsCMakeModelConfigurationDataProvider {
  override fun getCMakeModelConfigurationData(project: Project): CMakeModelConfigurationData? {
    val target = ExecutionTargetManager.getInstance(project).activeTarget

    if (target !is CMakeBuildProfileExecutionTarget) {
      return null
    }

    val workspace = CMakeWorkspace.getInstance(project)
    if (!workspace.isInitialized) return null

    val configs = workspace.model?.configurationData ?: return null
    val activeConfig = configs.firstOrNull { it.configName == target.profileName } ?: return null

    return activeConfig
  }
}
