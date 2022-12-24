package com.jetbrains.cidr.cpp.embedded.platformio.project

import com.intellij.execution.ExecutionTarget
import com.intellij.execution.ExecutionTargetProvider
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.jetbrains.cidr.cpp.embedded.platformio.PlatformioService

class PlatformioExecutionTargetProvider : ExecutionTargetProvider() {

  override fun getTargets(project: Project, configuration: RunConfiguration): List<ExecutionTarget> =
    project.service<PlatformioService>().envs

}