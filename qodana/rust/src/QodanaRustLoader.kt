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
import com.intellij.openapi.project.configuration.HeadlessLogging
import com.intellij.openapi.rd.util.lifetime
import com.intellij.platform.backend.observation.ActivityTracker
import com.intellij.util.PlatformUtils
import kotlinx.coroutines.CancellationException
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaTimeoutException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import org.rust.cargo.project.model.CargoProject
import org.rust.cargo.project.model.CargoProjectsService
import org.rust.cargo.project.model.cargoProjects
import org.rust.cargo.project.model.guessAndSetupRustProject
import org.rust.cargo.project.rustcPrivate.RsRustcPrivateTracker
import org.rust.lang.core.macros.MacroExpansionTaskListener
import org.rust.taskQueue
import kotlin.concurrent.atomics.ExperimentalAtomicApi

val CargoProject.UpdateStatus.humanString: String
    get() = when (this) {
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

    // Subscribes early (at service creation time, before configure() runs) to avoid a race
    // where MacroExpansionManager.stateLoaded() triggers expansion from cached .idea state
    // before configure() gets a chance to subscribe. CompletableDeferred.complete() is idempotent.
    private val macroExpansionCompleted = CompletableDeferred<Unit>()
    init {
        project.messageBus.connect(project).subscribe(
            MacroExpansionTaskListener.MACRO_EXPANSION_TASK_TOPIC,
            object : MacroExpansionTaskListener {
                override fun onMacroExpansionTaskFinished() {
                    macroExpansionCompleted.complete(Unit)
                }
            }
        )
    }

    private val configureDeferred: Deferred<Unit> = coroutineScope.async(start = CoroutineStart.LAZY) {
        project.lifetime.usingNested {
            withContext(Dispatchers.EDT) {
                configure()
            }
        }
    }

    val isConfigurationDone: Boolean get() = configureDeferred.isCompleted

    internal suspend fun awaitConfiguration() {
        configureDeferred.start()
        configureDeferred.await()
    }

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

        macroExpansionCompleted.await()

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
     *
     * Race guard: if Cargo projects are already loaded from cached `.idea` state before we subscribe,
     * the deferred is completed immediately to prevent a hang.
     */
    private fun Project.deferredCargoProjectReload(): CompletableDeferred<Unit> {
        val connection = messageBus.connect(this)
        val refreshFinished = CompletableDeferred<Unit>()
        connection.subscribe(
            CargoProjectsService.CARGO_PROJECTS_REFRESH_TOPIC,
            object : CargoProjectsService.CargoProjectsRefreshListener {
                override fun onRefreshStarted(isImplicitReload: Boolean) {
                    log.info("Cargo project model loading...")
                }

                override fun onRefreshFinished(
                    status: CargoProjectsService.CargoRefreshStatus,
                    projects: List<CargoProject>,
                    isImplicitReload: Boolean,
                ) {
                    log.info("Cargo project model loading finished: $status")
                    refreshFinished.complete(Unit)  // idempotent
                }
            }
        )

        // Race guard: if projects already fully loaded before we subscribed
        if (cargoProjects.allProjects.isNotEmpty() &&
            cargoProjects.allProjects.all { it.mergedStatus is CargoProject.UpdateStatus.UpToDate }) {
            refreshFinished.complete(Unit)  // idempotent
        }

        return refreshFinished
    }

}

class QodanaRustLoaderActivityTracker : ActivityTracker {
    override val presentableName: String = "Qodana for Rust startup awaiter"

    override suspend fun isInProgress(project: Project): Boolean {
        if (!PlatformUtils.isRustRover() || !PlatformUtils.isQodana()) return false
        return !QodanaRustLoader.getInstanceAsync(project).isConfigurationDone
    }

    override suspend fun awaitConfiguration(project: Project) {
        if (!PlatformUtils.isRustRover() || !PlatformUtils.isQodana()) return
        val loader = QodanaRustLoader.getInstanceAsync(project)
        try {
            withQodanaTimeout(project, "qd.rust.configuration.timeout.minutes", QodanaRustRegistry.configurationTimeout) {
                loader.awaitConfiguration()
            }
        } catch (e: QodanaTimeoutException) {
            // withQodanaTimeout already logs and reports to HeadlessLogging before throwing.
            throw e
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            QodanaRustLoader.log.error("Rust startup failed", e)
            HeadlessLogging.logFatalError(e.message ?: e.toString())
            throw e
        }
    }
}
