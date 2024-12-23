package com.intellij.dts.zephyr

import com.intellij.dts.DtsBundle
import com.intellij.dts.settings.DtsSettings
import com.intellij.dts.util.DtsUtil
import com.intellij.dts.zephyr.binding.*
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.ide.progress.withBackgroundProgress
import com.intellij.platform.util.progress.SequentialProgressReporter
import com.intellij.platform.util.progress.reportSequentialProgress
import com.intellij.util.containers.MultiMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel.Factory.CONFLATED
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.jetbrains.annotations.TestOnly

private fun settings(project: Project): Flow<DtsSettings.State> = channelFlow {
  project.messageBus.connect(this@channelFlow).subscribe(DtsSettings.ChangeListener.TOPIC, DtsSettings.ChangeListener { settings ->
    trySend(settings.state)
  })

  send(DtsSettings.of(project).state)

  awaitClose {}
}.buffer(CONFLATED)

@Service(Service.Level.PROJECT)
internal class DtsZephyrProvider(private val project: Project, scope: CoroutineScope) {
  companion object {
    fun of(project: Project): DtsZephyrProvider = project.service()
  }

  private val state: MutableStateFlow<State?> = MutableStateFlow(null)

  init {
    scope.launch {
      settings(project).collectLatest { settings ->
        withBackgroundProgress(project, DtsBundle.message("background.load_zephyr.title")) {
          reportSequentialProgress { reporter ->
            state.value = update(reporter, settings)
          }
        }
      }
    }
  }

  val root: VirtualFile? get() = state.value?.root

  val board: DtsZephyrBoard? get() = state.value?.board

  val bindings: MultiMap<String, DtsZephyrBinding> get() = state.value?.bindings ?: MultiMap.empty()

  private suspend fun findSdk(reporter: SequentialProgressReporter, root: String): VirtualFile? {
    return if (root.isBlank()) {
      reporter.indeterminateStep(DtsBundle.message("background.load_zephyr.search")) {
        DtsZephyrFileUtil.searchForRoot(project)
      }
    }
    else {
      DtsUtil.findFileAndRefresh(root)
    }
  }

  private suspend fun update(reporter: SequentialProgressReporter, settings: DtsSettings.State): State? {
    val root = findSdk(reporter, settings.zephyrRoot)
    if (root == null) return null

    val board = DtsUtil.findFileAndRefresh(settings.zephyrBoard)?.let(::DtsZephyrBoard)

    val bundledBindings = DtsZephyrBundledBindings.getInstance().getSource()
    val defaultBinding = bundledBindings?.files?.get(DtsZephyrBundledBindings.DEFAULT_BINDING)

    val bindings = reporter.indeterminateStep(DtsBundle.message("background.load_zephyr.bindings")) {
      val files = loadExternalBindings(root)
      parseExternalBindings(BindingSource(files, defaultBinding))
    }

    return State(root, board, bindings)
  }

  @TestOnly
  suspend fun awaitInit() {
    state.first { it != null }
  }

  private data class State(
    val root: VirtualFile?,
    val board: DtsZephyrBoard?,
    val bindings: MultiMap<String, DtsZephyrBinding>,
  )
}
