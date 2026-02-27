package org.intellij.qodana.rust

import com.intellij.openapi.application.EDT
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.components.serviceAsync
import com.intellij.openapi.project.Project
import kotlinx.coroutines.CoroutineScope
import com.intellij.openapi.diagnostic.logger
import kotlinx.coroutines.flow.firstOrNull
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.project.waitForSmartMode
import com.intellij.openapi.rd.util.lifetime
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.openapi.startup.StartupActivity
import com.intellij.platform.backend.observation.ActivityTracker
import com.intellij.util.PlatformUtils
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.rust.cargo.project.model.CargoProject
import org.rust.cargo.project.model.CargoProjectsService
import org.rust.cargo.project.model.cargoProjects
import org.rust.cargo.project.model.guessAndSetupRustProject
import org.rust.cargo.project.rustcPrivate.RsRustcPrivateTracker
import org.rust.lang.core.macros.MacroExpansionTaskListener
import org.rust.taskQueue
import kotlin.concurrent.atomics.ExperimentalAtomicApi

val CargoProject.UpdateStatus.humanString: String get() = when (this) {
  is CargoProject.UpdateStatus.UpToDate -> "ok"
  is CargoProject.UpdateStatus.NeedsUpdate -> "needs update"
  is CargoProject.UpdateStatus.UpdateFailed -> {
    StringBuilder().apply {
      append("failed ($reason")
      if (message != null) {
        append(": $message")
      }
      append(")")
    }.toString()
  }
}

@OptIn(ExperimentalAtomicApi::class)
@Service(Service.Level.PROJECT)
internal class QodanaRustLoader(private val project: Project, coroutineScope: CoroutineScope) {
  companion object {
    fun getInstance(project: Project): QodanaRustLoader = project.service()
    suspend fun getInstanceAsync(project: Project): QodanaRustLoader = project.serviceAsync()
    internal val log = logger<QodanaRustLoader>()
  }

  private val configureJob = coroutineScope.launch(start = CoroutineStart.LAZY) {
    project.lifetime.usingNested {
      withContext(Dispatchers.EDT) {
        configure()
      }
    }
  }

  internal suspend fun awaitConfiguration() {
    configureJob.join()
  }
  val isRunning get() = configureJob.isActive

  private suspend fun configure() {
    if (!PlatformUtils.isQodana()) {
      log.error("QodanaRustLoader must only run in a Qodana environment")
    }

    val projectRoot = project.guessProjectDir()?.toNioPath()
    if (projectRoot == null) {
      log.error("Unable to guess project root")
      return
    }

    val cargoProjectReload = project.deferredCargoProjectReload()
    val macroExpansion = project.deferredMacroExpansion()

    val cargoProjectsService = project.cargoProjects
    val result = guessAndSetupRustProject(project, explicitRequest = true, createConfigurations = true)
    if (!result) {
      if (cargoProjectsService.hasAtLeastOneValidProject) {
        cargoProjectsService.refreshAllProjects()
      } else {
        log.error("Cargo project model loading failed to start")
        return
      }
    }

    cargoProjectReload.await()

    log.info(StringBuilder().apply {
      appendLine("Finished loading Cargo projects:")
      for (cargoProject in cargoProjectsService.allProjects) {
        appendLine("  - ${cargoProject.presentableName}: ${cargoProject.mergedStatus.humanString}")
      }
    }.toString())

    if (cargoProjectsService.allProjects.any { it.mergedStatus !is CargoProject.UpdateStatus.UpToDate }) log.error(StringBuilder().apply {
      appendLine("Some Cargo projects failed to load:")
      for (cargoProject in cargoProjectsService.allProjects.filter { it.mergedStatus !is CargoProject.UpdateStatus.UpToDate }) {
        appendLine("  - ${cargoProject.presentableName}:")
        appendLine("      - Workspace:    ${cargoProject.workspaceStatus.humanString}")
        appendLine("      - Std. library: ${cargoProject.stdlibStatus.humanString}")
        appendLine("      - Compiler:     ${cargoProject.rustcInfoStatus.humanString}")
        appendLine("      - Build script: ${cargoProject.buildScriptEvaluationStatus.humanString}")
      }
    }.toString())

    macroExpansion.await()

    // Awaits a service that is responsible for attaching extra sources to projects using #![feature(rustc_private)]
    project.service<RsRustcPrivateTracker>().rustcPrivateInitializedInAllPackages.firstOrNull { it }

    project.waitForSmartMode()

    if (!project.taskQueue.isEmpty) {
      // This is a canary for a possible regression in expected behavior.
      log.error("Expected RustRover task queue to be empty")
    }
  }

  /**
   * Returns a CompletableDeferred that completes when the Cargo project has been refreshed.
   * This call does not start a refresh.
   */
  private fun Project.deferredCargoProjectReload(): CompletableDeferred<Unit> {
    val connection = messageBus.connect()
    val refreshStarted = CompletableDeferred<Unit>()
    val refreshFinished = CompletableDeferred<Unit>()
    connection.subscribe(
      CargoProjectsService.CARGO_PROJECTS_REFRESH_TOPIC,
      object : CargoProjectsService.CargoProjectsRefreshListener {
        override fun onRefreshStarted(isImplicitReload: Boolean) {
          log.info("Cargo project model loading...")
          refreshStarted.complete(Unit)
        }

        override fun onRefreshFinished(
          status: CargoProjectsService.CargoRefreshStatus,
          projects: List<CargoProject>,
          isImplicitReload: Boolean,
        ) {
          if (refreshStarted.isCompleted) {
            log.info("Cargo project model loading finished: $status")
            refreshFinished.complete(Unit)
          }
        }
      }
    )

    return refreshFinished
  }

  /**
   * Returns a CompletableDeferred that completes when the Cargo project has finished the macro expansion task.
   * This call does not request the macro expansion task to start.
   */
  private fun Project.deferredMacroExpansion(): CompletableDeferred<Unit> {
    val connection = messageBus.connect()
    val result = CompletableDeferred<Unit>()
    connection.subscribe(
      MacroExpansionTaskListener.MACRO_EXPANSION_TASK_TOPIC,
      object : MacroExpansionTaskListener {
        override fun onMacroExpansionTaskFinished() {
          result.complete(Unit)
        }
      }
    )

    return result
  }
}

internal class QodanaRustLoaderProjectActivity : ProjectActivity {
  override suspend fun execute(project: Project) {
    if (!PlatformUtils.isRustRover()) {
      QodanaRustLoader.log.debug("QodanaRustLoader will not run: not a RustRover IDE")
      return
    }
    if (!PlatformUtils.isQodana()) {
      QodanaRustLoader.log.debug("QodanaRustLoader will not run: not a Qodana environment")
      return
    }
    QodanaRustLoader.getInstanceAsync(project).awaitConfiguration()
  }
}

class QodanaRustLoaderActivityTracker : ActivityTracker {
  override val presentableName: String = "Qodana for Rust startup awaiter"

  override suspend fun isInProgress(project: Project): Boolean {
    return QodanaRustLoader.getInstanceAsync(project).isRunning
  }

  override suspend fun awaitConfiguration(project: Project) {
    val activity = StartupActivity.POST_STARTUP_ACTIVITY.findExtension(QodanaRustLoaderProjectActivity::class.java)
    if (activity == null) {
      QodanaRustLoader.log.error("QodanaRustLoaderProjectActivity extension not found")
      return
    }

    val timeout = QodanaRustRegistry.configurationTimeout
    val completed = withTimeoutOrNull(timeout) {
      activity.execute(project)
    }
    if (completed == null) {
      QodanaRustLoader.log.error("Qodana Rust configuration timed out after $timeout")
    }
  }
}
