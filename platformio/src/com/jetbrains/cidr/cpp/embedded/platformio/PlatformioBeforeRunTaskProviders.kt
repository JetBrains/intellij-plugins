package com.jetbrains.cidr.cpp.embedded.platformio

import com.intellij.execution.BeforeRunTask
import com.intellij.execution.BeforeRunTaskProvider
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.application.ModalityState
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
  override fun getIcon(): Icon = ClionEmbeddedPlatformioIcons.Platformio

  override fun canExecuteTask(configuration: RunConfiguration, task: PlatformioBeforeRunTask): Boolean =
    PlatformioWorkspace.isPlatformioProject(configuration.project)

  override fun isSingleton(): Boolean = true

  override fun executeTask(context: DataContext,
                           configuration: RunConfiguration,
                           env: ExecutionEnvironment,
                           task: PlatformioBeforeRunTask): Boolean {

    val taskToRun: ProjectTask = task.createTaskToRun()
    val result = ProjectTaskManager.getInstance(configuration.project)
      .run(taskToRun)
      .blockingGet(EXECUTION_TIMEOUT_MS)

    if (result == null || result.isAborted || result.hasErrors()) {
      ModalityUiUtil.invokeLaterIfNeeded(ModalityState.NON_MODAL) {
        CidrBuild.showBuildNotification(configuration.project, MessageType.ERROR,
                                        ClionEmbeddedPlatformioBundle.message("platformio.clean.failed"))
      }
      return false
    }
    return true
  }
}

class PlatformioBeforeRunTask(id: Key<PlatformioBeforeRunTask>,
                              private val taskGenerator: () -> ProjectTask) : BeforeRunTask<PlatformioBeforeRunTask>(id) {
  fun createTaskToRun(): ProjectTask = taskGenerator.invoke()
}

class PlatformioCleanBeforeRunTaskProvider : PlatformioBeforeRunTaskProvider() {

  private val ID = Key.create<PlatformioBeforeRunTask>("PlatformioCleanBeforeRun")
  override fun getId(): Key<PlatformioBeforeRunTask> = ID

  override fun createTask(runConfiguration: RunConfiguration): PlatformioBeforeRunTask =
    PlatformioBeforeRunTask(id) { CidrCleanTaskImpl(PlatformioBuildConfiguration) }

  override fun getName(): String = ClionEmbeddedPlatformioBundle.message("platformio.clean")

}

class PlatformioUploadBeforeRunTaskProvider : PlatformioBeforeRunTaskProvider() {

  private val ID = Key.create<PlatformioBeforeRunTask>("PlatformioUploadBeforeRun")
  override fun getId(): Key<PlatformioBeforeRunTask> = ID

  override fun createTask(runConfiguration: RunConfiguration): PlatformioBeforeRunTask =
    PlatformioBeforeRunTask(id) { PlatformioTargetTask(name, "-t", "upload") }

  override fun getName(): String = ClionEmbeddedPlatformioBundle.message("platformio.upload")

}
