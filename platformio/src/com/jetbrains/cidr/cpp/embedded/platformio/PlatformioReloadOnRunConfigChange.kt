package com.jetbrains.cidr.cpp.embedded.platformio

import com.intellij.execution.RunManagerListener
import com.intellij.execution.RunnerAndConfigurationSettings
import com.intellij.openapi.components.service
import com.intellij.openapi.components.serviceAsync
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskType
import com.intellij.openapi.externalSystem.service.internal.ExternalSystemProcessingManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.jetbrains.cidr.cpp.embedded.platformio.project.ID
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi

class PlatformioReloadOnRunConfigChange : ProjectActivity {

  override suspend fun execute(project: Project) {
    val platformioService = project.serviceAsync<PlatformioService>()
    project.messageBus.connect(platformioService).subscribe(RunManagerListener.TOPIC, PlatformioReloadOnRunConfigChangeListener(project))
  }

  @OptIn(ExperimentalAtomicApi::class)
  private class PlatformioReloadOnRunConfigChangeListener(val project: Project) : RunManagerListener {

    /** [runConfigurationChanged] can get called multiple times in one update, but we want to refresh the project only once. */
    private val pioConfigChangedInUpdate = AtomicBoolean(false)

    override fun beginUpdate() {
      pioConfigChangedInUpdate.store(false)
    }

    override fun runConfigurationChanged(settings: RunnerAndConfigurationSettings) {
      if (settings.configuration is PlatformioDebugConfiguration) {
        pioConfigChangedInUpdate.store(true)
      }
    }

    override fun endUpdate() {
      if (pioConfigChangedInUpdate.load()) {
        refreshProject()
      }
    }

    override fun runConfigurationSelected(settings: RunnerAndConfigurationSettings?) {
      if (settings?.configuration is PlatformioDebugConfiguration) {
        refreshProject()
      }
    }

    private fun refreshProject() {
      // The external system by default cancels the newer reload request and lets the previous run to completion.
      // Here we reload because something has changed in the configuration, so we cancel the by now old request, and reload from scratch.
      cancelPreviousReload()
      project.service<PlatformioService>().refreshProject(true)
    }

    private fun cancelPreviousReload() {
      val processingManager = ExternalSystemProcessingManager.getInstance()
      val existingTask = processingManager.findTask(ExternalSystemTaskType.RESOLVE_PROJECT, ID, project.basePath!!)
      existingTask?.cancel()
    }
  }
}