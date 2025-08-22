package com.jetbrains.cidr.cpp.embedded.platformio

import com.intellij.execution.RunManager
import com.intellij.execution.RunManagerListener
import com.intellij.execution.RunnerAndConfigurationSettings
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.UI
import com.intellij.openapi.application.asContextElement
import com.intellij.openapi.components.service
import com.intellij.openapi.components.serviceAsync
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskType
import com.intellij.openapi.externalSystem.service.internal.ExternalSystemProcessingManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.util.asSafely
import com.jetbrains.cidr.cpp.embedded.platformio.project.ID
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi

class PlatformioReloadOnRunConfigChange : ProjectActivity {

  override suspend fun execute(project: Project) {
    val platformioService = project.serviceAsync<PlatformioService>()
    project.messageBus.connect(platformioService).subscribe(RunManagerListener.TOPIC, PlatformioReloadOnRunConfigChangeListener(project, platformioService.cs))
  }


  /** We reload on two occasions:
   * 1. When we change selection from a PIO RC to another PIO RC.
   *     - Changing from non-PIO to PIO and vice versa is handled by the [ExecutionTargetListener] in [PlatformioManager].
   * 2. When the currently selected RC is PIO RC and the configuration is changed.
   *
   * We reload only when necessary, as defined in [PlatformioDebugConfiguration.shouldChangeCauseReload] based on the change in RC.
   */
  @OptIn(ExperimentalAtomicApi::class)
  private class PlatformioReloadOnRunConfigChangeListener(val project: Project, cs: CoroutineScope) : RunManagerListener {

    private val currentRc = AtomicReference<PlatformioDebugConfiguration?>(null)
    private val nextRcFlow = MutableSharedFlow<PlatformioDebugConfiguration?>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    init {
      // We collect the next RC in non-modal state to dedupe multiple Applies in one run config dialog.
      cs.launch(Dispatchers.UI
                + ModalityState.nonModal().asContextElement()
                + CoroutineName("PlatformIO Reload on Run Configuration Change")) {
        nextRcFlow.collect { next ->
          val current = currentRc.exchange(next)
          if (shouldReload(current, next)) {
            refreshProject()
          }
        }
      }
    }

    override fun endUpdate(){
      val selectedConfig =  RunManager.getInstance(project)
        .selectedConfiguration
        ?.configuration
        ?.asSafely<PlatformioDebugConfiguration>()

      nextRcFlow.tryEmit(selectedConfig?.clone())
    }

    override fun runConfigurationSelected(settings: RunnerAndConfigurationSettings?) {
      val newConfig = settings?.configuration as? PlatformioDebugConfiguration
      nextRcFlow.tryEmit(newConfig?.clone())
    }

    private fun shouldReload(prevConfig: PlatformioDebugConfiguration?, newConfig: PlatformioDebugConfiguration?): Boolean {
      return prevConfig != null && newConfig != null && prevConfig.shouldChangeCauseReload(newConfig)
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