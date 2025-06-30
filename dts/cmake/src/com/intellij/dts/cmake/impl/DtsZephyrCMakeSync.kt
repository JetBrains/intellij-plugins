package com.intellij.dts.cmake.impl

import com.intellij.dts.settings.DtsSettings
import com.intellij.execution.ExecutionTarget
import com.intellij.execution.ExecutionTargetListener
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.jetbrains.cidr.cpp.cmake.workspace.CMakeWorkspaceListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@FlowPreview
class DtsZephyrCMakeSync(
  val project: Project,
  parentScope: CoroutineScope,
) : CMakeWorkspaceListener, DtsSettings.ChangeListener, ExecutionTargetListener {
  companion object {
    private const val ZEPHYR_BOARD_PATH_VARIABLE = "BOARD_DIR"
    private const val ZEPHYR_ROOT_PATH_VARIABLE = "ZEPHYR_BASE"

    private val logger = Logger.getInstance(DtsZephyrCMakeSync::class.java)
  }

  private val alarm = MutableSharedFlow<Unit>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

  init {
    parentScope.launch(Dispatchers.IO) {
      alarm.debounce(300.milliseconds).collectLatest {
        syncSettings()
      }
    }
  }

  private fun syncSettings() {
    val settings = DtsSettings.of(project)
    if (!settings.zephyrCMakeSync) return

    val activeConfig = DtsCMakeModelConfigurationDataProvider.getFirstCMakeModelConfigurationData(project)
    if (activeConfig == null) {
      return
    }

    val cache = try {
      activeConfig.getCacheConfigurator()
    }
    catch (e: Exception) {
      logger.debug("failed to get cmake cache configurator", e)
      return
    }

    val boardPath = cache.findVariable(ZEPHYR_BOARD_PATH_VARIABLE)?.value
    val rootPath = cache.findVariable(ZEPHYR_ROOT_PATH_VARIABLE)?.value

    // do not update settings if nothing changed, prevents infinite loop
    if (settings.zephyrRoot == rootPath && settings.zephyrBoard == boardPath) return

    settings.update {
      zephyrRoot = rootPath ?: ""
      zephyrBoard = boardPath ?: ""
    }
  }

  override fun reloadingFinished(canceled: Boolean) {
    if (canceled) return

    alarm.tryEmit(Unit)
  }

  override fun settingsChanged(settings: DtsSettings) {
    alarm.tryEmit(Unit)
  }

  override fun activeTargetChanged(newTarget: ExecutionTarget) {
    alarm.tryEmit(Unit)
  }
}
