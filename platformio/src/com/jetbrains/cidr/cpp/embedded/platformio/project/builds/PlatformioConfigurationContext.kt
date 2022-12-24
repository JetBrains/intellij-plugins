package com.jetbrains.cidr.cpp.embedded.platformio.project.builds

import com.intellij.execution.BeforeRunTask
import com.intellij.execution.DefaultExecutionTarget
import com.intellij.execution.ExecutionException
import com.intellij.execution.ExecutionTarget
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.task.ProjectTaskManager
import com.intellij.task.impl.ProjectModelBuildTaskImpl
import com.jetbrains.cidr.cpp.embedded.platformio.PlatformioService
import com.jetbrains.cidr.cpp.embedded.platformio.project.ID
import com.jetbrains.cidr.cpp.embedded.platformio.project.PlatformioBuildConfigurationHelper
import com.jetbrains.cidr.cpp.execution.CLionLauncher
import com.jetbrains.cidr.cpp.execution.compound.CidrCompoundConfigurationContextBase
import com.jetbrains.cidr.cpp.toolchains.CPPEnvironment
import com.jetbrains.cidr.cpp.toolchains.CPPToolchains
import com.jetbrains.cidr.execution.CidrBuildConfigurationHelper
import com.jetbrains.cidr.toolchains.EnvironmentProblems
import java.io.File

class PlatformioConfigurationContext : CidrCompoundConfigurationContextBase(PlatformioBuildTarget::class.java) {
  override val id: String
    get() = ID.id


  override fun getProjectBaseDir(project: Project, launcher: CLionLauncher): File? {
    return project.basePath?.let(::File)
  }

  override fun canRunOn(target: ExecutionTarget): Boolean {
    return target != DefaultExecutionTarget.INSTANCE
  }

  @Throws(ExecutionException::class)
  override fun getRunFileAndEnvironment(launcher: CLionLauncher): Pair<File?, CPPEnvironment?> {
    val binary = launcher.project.service<PlatformioService>().targetExecutablePath?.let(::File)
    return binary to CPPToolchains.createCPPEnvironment(launcher.project, launcher.getProjectBaseDir(), null,
                                                        EnvironmentProblems(), false, null)
  }

  override fun executeBuildTask(context: DataContext,
                                configuration: RunConfiguration,
                                env: ExecutionEnvironment,
                                task: BeforeRunTask<*>): Boolean {
    val project = configuration.project

    val result = ProjectTaskManager.getInstance(project)
      .run(ProjectModelBuildTaskImpl(PlatformioBuildConfiguration, false))
      .blockingGet(EXECUTION_TIMEOUT_MS)

    return !(result == null || result.isAborted || result.hasErrors())
  }

  override fun getHelper(project: Project): CidrBuildConfigurationHelper<PlatformioBuildConfiguration, PlatformioBuildTarget> {
    return PlatformioBuildConfigurationHelper(project)
  }
}