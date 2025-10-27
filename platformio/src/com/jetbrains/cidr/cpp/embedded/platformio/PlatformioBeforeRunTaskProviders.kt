package com.jetbrains.cidr.cpp.embedded.platformio

import com.intellij.execution.BeforeRunTask
import com.intellij.execution.BeforeRunTaskProvider
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.execution.executors.DefaultDebugExecutor
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.MessageType
import com.intellij.openapi.util.Key
import com.intellij.task.ProjectTask
import com.intellij.task.ProjectTaskManager
import com.intellij.util.ModalityUiUtil
import com.jetbrains.cidr.cpp.embedded.platformio.project.PlatformioWorkspace
import com.jetbrains.cidr.cpp.embedded.platformio.project.builds.EXECUTION_TIMEOUT_MS
import com.jetbrains.cidr.cpp.embedded.platformio.project.builds.PlatformioBuildConfiguration
import com.jetbrains.cidr.cpp.embedded.platformio.project.builds.PlatformioTargetTask
import com.jetbrains.cidr.execution.build.CidrBuild
import com.jetbrains.cidr.execution.build.tasks.CidrCleanTaskImpl
import icons.ClionEmbeddedPlatformioIcons
import javax.swing.Icon

abstract class PlatformioBeforeRunTaskProvider : BeforeRunTaskProvider<PlatformioBeforeRunTask>() {
  override fun getIcon(): Icon = ClionEmbeddedPlatformioIcons.LogoPlatformIO

  override fun canExecuteTask(configuration: RunConfiguration, task: PlatformioBeforeRunTask): Boolean =
    PlatformioWorkspace.isPlatformioProject(configuration.project)

  override fun isSingleton(): Boolean = true

  override fun executeTask(context: DataContext,
                           configuration: RunConfiguration,
                           env: ExecutionEnvironment,
                           task: PlatformioBeforeRunTask): Boolean {

    val taskToRun: ProjectTask = task.createTaskToRun(env)
    val result = ProjectTaskManager.getInstance(configuration.project)
      .run(taskToRun)
      .blockingGet(EXECUTION_TIMEOUT_MS)

    if (result == null || result.isAborted || result.hasErrors()) {
      ModalityUiUtil.invokeLaterIfNeeded(ModalityState.nonModal()) {
        CidrBuild.showBuildNotification(configuration.project, MessageType.ERROR,
                                        ClionEmbeddedPlatformioBundle.message("platformio.task.failed", name))
      }
      return false
    }
    return true
  }

  protected fun createPlatformioBeforeRunTask(project: Project, taskGenerator: (env: ExecutionEnvironment) -> ProjectTask): PlatformioBeforeRunTask? {
    if (!PlatformioWorkspace.isPlatformioProject(project)) return null
    return PlatformioBeforeRunTask(id, taskGenerator)
  }
}

class PlatformioBeforeRunTask(id: Key<PlatformioBeforeRunTask>,
                              private val taskGenerator: (env: ExecutionEnvironment) -> ProjectTask) : BeforeRunTask<PlatformioBeforeRunTask>(id) {
  fun createTaskToRun(env: ExecutionEnvironment): ProjectTask = taskGenerator.invoke(env)
}

class PlatformioCleanBeforeRunTaskProvider : PlatformioBeforeRunTaskProvider() {

  private val ID = Key.create<PlatformioBeforeRunTask>("PlatformioCleanBeforeRun")
  override fun getId(): Key<PlatformioBeforeRunTask> = ID

  override fun createTask(runConfiguration: RunConfiguration): PlatformioBeforeRunTask? =
    createPlatformioBeforeRunTask(runConfiguration.project) { CidrCleanTaskImpl(PlatformioBuildConfiguration) }

  override fun getName(): String = ClionEmbeddedPlatformioBundle.message("platformio.clean")

}

class PlatformioUploadBeforeRunTaskProvider : PlatformioBeforeRunTaskProvider() {

  private val ID = Key.create<PlatformioBeforeRunTask>("PlatformioUploadBeforeRun")
  override fun getId(): Key<PlatformioBeforeRunTask> = ID

  override fun createTask(runConfiguration: RunConfiguration): PlatformioBeforeRunTask? =
    createPlatformioBeforeRunTask(runConfiguration.project) { PlatformioTargetTask(name, "run", "-t", "upload") }

  override fun getName(): String = ClionEmbeddedPlatformioBundle.message("platformio.upload")

}

class PlatformioBuildBeforeRunTaskProvider : PlatformioBeforeRunTaskProvider() {

  private val ID = Key.create<PlatformioBeforeRunTask>("PlatformioBuildBeforeRun")
  override fun getId(): Key<PlatformioBeforeRunTask> = ID

  override fun createTask(runConfiguration: RunConfiguration): PlatformioBeforeRunTask? =
    createPlatformioBeforeRunTask(runConfiguration.project) { env ->
      if (env.executor is DefaultDebugExecutor) {
        PlatformioTargetTask(name, "debug")
      }
      else {
        PlatformioTargetTask(name, "run")
      }
    }?.apply { isEnabled = runConfiguration is PlatformioDebugConfiguration } // Add to all PIO run configurations by default

  override fun getName(): String = ClionEmbeddedPlatformioBundle.message("platformio.build")
}
