package org.jetbrains.qodana.cpp

import com.intellij.clion.radler.core.projectmodel.RadProjectModelHost
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.project.configuration.HeadlessLogging
import com.intellij.openapi.application.EDT
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.components.serviceAsync
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManagerListener
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.waitForSmartMode
import com.intellij.openapi.rd.createNestedDisposable
import com.intellij.openapi.rd.util.lifetime
import com.jetbrains.cidr.lang.workspace.OCResolveConfiguration
import com.jetbrains.cidr.lang.workspace.OCWorkspace
import com.jetbrains.cidr.project.workspace.CidrWorkspace
import com.jetbrains.cidr.project.workspace.CidrWorkspaceListener
import com.jetbrains.cidr.project.workspace.CidrWorkspaceManager
import com.jetbrains.cidr.project.workspace.CidrWorkspaceState
import com.jetbrains.cidr.util.InitializationWaiter
import com.jetbrains.rd.platform.util.TimeoutTracker
import com.jetbrains.rd.util.lifetime.Lifetime
import com.jetbrains.rd.util.threading.coroutines.nextNotNullValue
import com.jetbrains.rd.util.threading.coroutines.nextTrueValue
import com.jetbrains.rider.protocol.IProtocolHostWithBackend
import com.jetbrains.rider.protocol.protocolHostIfExists
import com.jetbrains.rider.model.projectModelTasks
import com.jetbrains.rider.model.radProjectModel
import com.jetbrains.rider.projectView.solution
import com.jetbrains.rider.services.RiderProjectModelWaiter
import com.jetbrains.rider.solutionAnalysis.SolutionAnalysisHost
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaException
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaTimeoutException
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.coroutines.resume
import kotlin.io.path.div
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.measureTime

@Service(Service.Level.PROJECT)
class QodanaCppStartupManager(private val project: Project, private val coroutineScope: CoroutineScope) {
  companion object {
    fun getInstance(project: Project): QodanaCppStartupManager = project.service()
    suspend fun getInstanceAsync(project: Project): QodanaCppStartupManager = project.serviceAsync()
    private val log = logger<QodanaCppStartupManager>()
  }

  private val tasksInProgress = HashSet<Task>()
  private val lockTasksOperations = ReentrantLock()
  private val completableDeferred = CompletableDeferred<Unit>()
  @Volatile private var currentPhase = "initializing"

  private val startupDeferred: Deferred<Unit> = coroutineScope.async(start = CoroutineStart.LAZY) {
    project.lifetime.usingNested { lt ->
      withContext(Dispatchers.EDT) {
        doAwaitStartup(lt)
      }
    }
  }

  val isStartupDone: Boolean get() = startupDeferred.isCompleted

  suspend fun awaitStartup() {
    startupDeferred.start()
    startupDeferred.await()
  }

  private suspend fun doAwaitStartup(lifetime: Lifetime) {
    monitoringBackendHealth {
      subscribeToProgress(lifetime.createNestedDisposable())

      currentPhase = "awaiting solution analysis host"
      awaitSolutionAnalysisHost()

      currentPhase = "awaiting backend startup"
      withQodanaTimeout(project, "qd.cpp.workspace.ready.timeout.seconds", QodanaCppRegistry.workspaceReadyTimeout) {
        awaitBackendStartup()
        log.info("CLion backend started")

        coroutineScope {
          launch { awaitBackendReady() }
          launch { awaitSmartMode() }
        }

        // we have to await sync points before considering project as open
        awaitSyncPoints(lifetime)
      }

      currentPhase = "awaiting build tools"
      log.info("Await for synced project")
      awaitCidrWorkspacesReadyOrDetectStuck(lifetime)

      // IMPORTANT: "workspaces ready" does NOT mean the build system succeeded.
      // Per CidrWorkspaceState docs, even CMake errors result in state=Loaded (isReady=true).
      // We must explicitly check the CMake exit code before proceeding to OC resolve,
      // which would hang waiting for configurations that will never appear.
      val cmakeOutput = CppQodanaCMakeGenerationStepListener.lastOutput
      if (cmakeOutput != null && cmakeOutput.exitCode != 0) {
        printCMakeConfigureOutput()
        failWithBuildSystemError("CMake configuration failed (exit code ${cmakeOutput.exitCode})")
      }

      if (!CidrWorkspaceManager.getInstance(project).hasAnyLoadedModels) {
        printCMakeConfigureOutput()
        failWithBuildSystemError("workspace has no loaded models")
      }
      log.info("Build system workspace loaded")

      afterCidrWorkspacesReady()
      printCMakeConfigureOutput()

      // Check if any OC resolve configurations exist before waiting for more.
      // If the build system failed to produce ANY configurations, waiting would hang forever.
      currentPhase = "awaiting OC resolve configurations"
      val existingConfigs = OCWorkspace.getInstance(project).configurations
      if (existingConfigs.isEmpty()) {
        failWithBuildSystemError("no resolve configurations were produced")
      }

      withQodanaTimeout(project, "qd.cpp.oc.resolve.timeout.ms", QodanaCppRegistry.ocResolveTimeout) {
        awaitOCResolveConfiguration()
      }

      currentPhase = "awaiting background tasks"
      withQodanaTimeout(project, "qd.cpp.progress.manager.timeout.seconds", QodanaCppRegistry.progressManagerTimeout) {
        checkProgressManagerAwait()
      }
      log.info("All background tasks completed")
    }
  }

  private suspend fun afterCidrWorkspacesReady() {
    QodanaCppHeadlessStartupExtension().afterCidrWorkspacesReadyImpl(project)
  }

  /** Reports a build system configuration failure with a user-facing message and throws [QodanaException]. */
  private fun failWithBuildSystemError(reason: String): Nothing {
    val ideaLogPath = PathManager.getLogDir() / "idea.log"
    val selectedName = selectedWorkspaceComponentName
    logDiagnostics()

    if (selectedName != null) {
      val buildSystem = selectedName.removeSuffix("Workspace")
      log.warn("Build system failure ($buildSystem): $reason")
      println("Failed to configure project as a $buildSystem workspace.")
      println("  Note: To select another build system, change \"cpp/buildSystem\" field in qodana.yaml.")
    }
    else {
      log.warn("No build system detected: $reason")
      println("Could not auto-detect the build system used by this project.")
      println("  Note: To specify a build system, set \"cpp/buildSystem\" field in qodana.yaml. Supported build systems are:")
      for (name in supportedBuildSystems) {
        println("    - $name")
      }
    }

    println("  Detailed logs are available at $ideaLogPath")
    val message = if (selectedName != null) {
      "Failed to configure project as a ${selectedName.removeSuffix("Workspace")} workspace: $reason"
    } else {
      "Could not auto-detect the build system: $reason"
    }
    HeadlessLogging.logFatalError(message)
    throw QodanaException(message)
  }

  /** Prints CMake output to stdout for user visibility. This is informational only — it does not
   *  affect the startup flow. Callers must separately check [CppQodanaCMakeGenerationStepListener.lastOutput]
   *  for error handling. Consumes [lastOutput][CppQodanaCMakeGenerationStepListener.lastOutput] (sets it to null). */
  private fun printCMakeConfigureOutput() {
    val output = CppQodanaCMakeGenerationStepListener.consumeLastOutput() ?: return
    val text = output.output.toString().trim()
    if (text.isEmpty()) return

    if (output.exitCode != 0) {
      println("CMake configure failed (exit code ${output.exitCode}):")
    }
    else {
      println("CMake configure output:")
    }
    println(text)
  }

  /** Logs diagnostic information to idea.log in pseudo-YAML format. */
  fun logDiagnostics() {
    val diag = buildString {
      appendLine("Qodana C++ startup diagnostics:")
      appendLine("  phase: $currentPhase")
      try {
        val tasks = lockTasksOperations.withLock { tasksInProgress.map { it.javaClass.name } }
        if (tasks.isEmpty()) {
          appendLine("  active-tasks: []")
        } else {
          appendLine("  active-tasks:")
          tasks.forEach { appendLine("    - $it") }
        }
      }
      catch (_: Exception) {
        appendLine("  active-tasks: <unavailable>")
      }
      try {
        val wsStates = CidrWorkspaceManager.getInstance(project).workspaces.entries
        appendLine("  workspace-states:")
        wsStates.forEach { (ws, state) -> appendLine("    - ${ws.javaClass.simpleName}: $state") }
      }
      catch (_: Exception) {
        appendLine("  workspace-states: <unavailable>")
      }
    }
    log.warn(diag)
  }

  /**
   * Runs [block] while monitoring the Radler backend process. If the backend dies during
   * execution, throws [QodanaException] and cancels [block] via structured concurrency.
   */
  private suspend fun monitoringBackendHealth(block: suspend () -> Unit) = coroutineScope {
    val monitor = launch { awaitBackendDeath() }
    block()
    monitor.cancel()
  }

  /** Suspends until the backend process terminates, then throws [QodanaException]. */
  private suspend fun awaitBackendDeath() {
    // Phase 1: Wait for the protocol host to appear (polling — no reactive API available,
    // protocolHostIfExists is backed by UserData, not an observable property).
    var protocolHost: IProtocolHostWithBackend?
    while (true) {
      val host = project.protocolHostIfExists
      if (host == null) {
        log.info("Backend health monitor: protocol host not yet available")
        delay(1.seconds)
        continue
      }
      protocolHost = host as? IProtocolHostWithBackend
      if (protocolHost == null) {
        log.error("Backend health monitor: protocol host is not IProtocolHostWithBackend")
        return
      }
      break
    }

    val process = protocolHost.resharperProcess ?: run {
      log.error("Backend health monitor: protocol host has no process")
      return
    }

    // Phase 2: Reactively wait for the backend process to terminate (no polling).
    // onTermination fires the callback immediately if the lifetime is already terminated.
    suspendCancellableCoroutine { cont ->
      process.backendProcessLifetime.onTermination { cont.resume(Unit) }
    }

    val exitCode = try {
      process.resharperProcessHandler.exitCode
    } catch (e: UninitializedPropertyAccessException) {
      log.error("Backend health monitor: failed to retrieve backend process exit code", e)
      null
    }
    throw QodanaException("CLion backend process terminated unexpectedly (exit code ${exitCode ?: "unknown"})")
  }

  private suspend fun logAction(message: String, action: suspend () -> Unit) {
    log.info("${message}...")
    try {
      val elapsedTime = measureTime {
        action()
      }
      log.info("${message}... done (${elapsedTime})")
    }
    catch (ex: TimeoutCancellationException) {
      log.warn("${message}... timeout")
      throw ex
    }
    catch (ex: CancellationException) {
      log.warn("${message}... cancelled")
      throw ex
    }
    catch (ex: Exception) {
      log.warn("${message}... failed: ${ex.javaClass.simpleName}")
      throw ex
    }
  }

  private suspend fun awaitSolutionAnalysisHost() = logAction("Awaiting solution analysis host ready") {
    SolutionAnalysisHost.getInstance(project).model.hostReady.nextTrueValue()
  }

  private suspend fun awaitBackendStartup() = logAction("Awaiting backend startup") {
    project.solution.solutionLifecycle.fullStartupFinished.nextNotNullValue()
  }

  private suspend fun awaitSmartMode() = logAction("Awaiting smart mode") {
    project.waitForSmartMode()
  }

  private suspend fun awaitBackendReady() = logAction("Awaiting backend to finish configuration") {
    RadProjectModelHost.getInstanceAsync(project).withSyncedProject {
      awaitProjectModel()
      awaitRadCaches()
    }
  }

  private suspend fun awaitProjectModel() = logAction("Awaiting backend project model") {
    val timeoutTracker = TimeoutTracker(QodanaCppRegistry.projectModelTimeout)
    try {
      RiderProjectModelWaiter.waitForProjectModelReadySuspending(project, timeoutTracker, log::info)
    }
    catch (ex: CancellationException) {
      throw ex
    }
    catch (ex: Exception) {
      // RiderProjectModelWaiter throws bare java.lang.Exception for all timeout scenarios
      // (e.g. "Project model wasn't ready in time", "Source generators weren't ready in time").
      // There is no typed exception to narrow this catch — all failures are java.lang.Exception.
      log.warn("Timeout while awaiting project model", ex)
      println("Warning: backend project model timed out. Analysis may proceed with incomplete data.")
    }
  }

  private suspend fun awaitRadCaches() = logAction("Awaiting backend caches") {
    project.solution.radProjectModel.waitForCaches.startSuspending(this.javaClass.simpleName)
  }

  private suspend fun awaitSyncPoints(lifetime: Lifetime) = logAction("Awaiting sync points") {
    val tasks = project.solution.projectModelTasks
    var hasNotReachedSyncPoints = true
    while (hasNotReachedSyncPoints) {
      val notReachedSyncPoints = tasks.getNotReachedSyncPoints.startSuspending(lifetime, Unit)
      hasNotReachedSyncPoints = notReachedSyncPoints.any()
      if (hasNotReachedSyncPoints) {
        log.info("Waiting for sync points ${notReachedSyncPoints.joinToString(", ")}")
        delay(100.milliseconds)
      }
    }
  }

  /**
   * Waits for at least one OC resolve configuration to appear.
   *
   * WARNING: this will hang (until timeout) if the build system failed to configure,
   * because no OC configurations will ever be produced. The caller must verify build
   * system success before calling this method.
   */
  private suspend fun awaitOCResolveConfiguration() = logAction("Awaiting any OC resolve configurations") {
    var configurations: List<OCResolveConfiguration>
    while (true) {
      configurations = OCWorkspace.getInstance(project).configurations
      if (configurations.isNotEmpty()) {
        break
      }
      delay(1.seconds)
    }
    log.info("Found ${configurations.size} configurations: ${configurations.joinToString(", ") { it.displayName }}")
  }

  private fun subscribeToProgress(projectBkTasksListener: Disposable) {
    ApplicationManager
      .getApplication()
      .messageBus
      .connect(projectBkTasksListener)
      .subscribe<ProgressManagerListener>(ProgressManagerListener.TOPIC, object : ProgressManagerListener {
        override fun beforeTaskStart(task: Task, indicator: ProgressIndicator) {
          lockTasksOperations.withLock {
            tasksInProgress.add(task)
            log.info("Start ${task.javaClass.name}")
          }
        }

        override fun afterTaskFinished(task: Task) {
          lockTasksOperations.withLock {
            tasksInProgress.remove(task)
            log.info("Finish ${task.javaClass.name}")
            if (tasksInProgress.isEmpty()) {
              ApplicationManager
                .getApplication()
                .invokeLater {
                  triggerCompleteOnEmptyTasks()
                }
            }
          }
        }
      })
  }

  private suspend fun checkProgressManagerAwait() {
    log.info("Awaiting Progress Manager")
    triggerCompleteOnEmptyTasks()
    completableDeferred.join()
    log.info("Progress Manager has no more active tasks")
  }

  private fun triggerCompleteOnEmptyTasks() {
    lockTasksOperations.withLock {
      if (tasksInProgress.isEmpty()) {
        completableDeferred.complete(Unit)  // idempotent — safe to call multiple times
      }
    }
  }

  /**
   * Wait until every workspace reaches a final state (`Loaded` or `NotLoaded`), or detect stuck workspaces.
   *
   * Note: a workspace reaching `Loaded` does NOT mean the build system succeeded.
   * Per [CidrWorkspaceState] docs, even loading errors (e.g. CMake configuration failure)
   * result in `state=Loaded`. The caller must check build system output separately
   * (see [CppQodanaCMakeGenerationStepListener.lastOutput]).
   *
   * A workspace is "stuck" when all ProgressManager tasks have completed but some workspaces
   * are still in an intermediate state (not `Loaded` or `NotLoaded`). This happens when a build
   * system (e.g. Makefile) fails to initialize properly — the import task finishes but the workspace
   * never transitions to a final state.
   *
   * We race two coroutines:
   * 1. The normal workspace-ready wait (via [InitializationWaiter])
   * 2. A stuck detector that polls for the "tasks done but workspaces not ready" condition
   *
   * Falls back to a hard timeout ([QodanaCppRegistry.workspaceReadyTimeout]) as a safety net.
   */
  private suspend fun awaitCidrWorkspacesReadyOrDetectStuck(lifetime: Lifetime) = logAction("Awaiting build tools") {
    val completed = withTimeoutOrNull(QodanaCppRegistry.workspaceReadyTimeout) {
      suspendCancellableCoroutine {
        OnCidrWorkspacesReadyImpl(project).waitForInitialization(lifetime.createNestedDisposable()) {
          it.resume(Unit)
        }
      }
    }
    if (completed != null) return@logAction

    // Timeout — produce a targeted error message based on which workspace is stuck.
    logDiagnostics()
    val selectedName = selectedWorkspaceComponentName
    val wsManager = CidrWorkspaceManager.getInstance(project)
    val selectedState = wsManager.workspaces.entries
      .find { (ws, _) -> ws.javaClass.simpleName == selectedName }?.value
    val buildSystem = selectedName?.removeSuffix("Workspace") ?: "unknown"
    val ideaLogPath = PathManager.getLogDir() / "idea.log"

    if (selectedState != null && !selectedState.isReady) {
      // The selected workspace is stuck — this is the primary failure.
      println("Failed to configure project as a $buildSystem workspace.")
      println("  Note: To select another build system, change \"cpp/buildSystem\" field in qodana.yaml.")
      println("  Detailed logs are available at $ideaLogPath")
      HeadlessLogging.logFatalError("$buildSystem workspace stuck in $selectedState")
      throw QodanaException("$buildSystem workspace stuck in $selectedState")
    }
    else {
      // Selected workspace is ready but something else is blocking — fall back to generic message.
      println(userTimeoutMessage(project, "qd.cpp.workspace.ready.timeout.seconds"))
      HeadlessLogging.logFatalError("qd.cpp.workspace.ready.timeout.seconds timeout reached")
      throw QodanaTimeoutException("qd.cpp.workspace.ready.timeout.seconds timeout reached")
    }
  }
}

private class OnCidrWorkspacesReadyImpl(project: Project) : InitializationWaiter(project) {
  /**
   * Returns true when the selected workspace is ready. If we know which build system was selected
   * (via [selectedWorkspaceComponentName]), we only require that workspace to be ready — irrelevant
   * workspaces (e.g. CMakeWorkspace for a Makefile project) are ignored. Falls back to checking
   * all workspaces if the selection is unknown.
   */
  override fun isInitialized(): Boolean {
    val wsManager = CidrWorkspaceManager.getInstance(project)
    val selectedName = selectedWorkspaceComponentName
    return if (selectedName != null) {
      wsManager.workspaces.all { (ws, state) ->
        // Only wait for the selected workspace to be ready. Irrelevant workspaces
        // (e.g. CMakeWorkspace for a Makefile project) pass unconditionally.
        // Note: != not == — we SKIP irrelevant workspaces, not the selected one.
        ws.javaClass.simpleName != selectedName || state.isReady
      }
    }
    else {
      wsManager.modelsAreReady
    }
  }

  override fun subscribeToInitialization(project: Project, parentDisposable: Disposable, callBack: () -> Unit) {
    project.messageBus.connect(parentDisposable).subscribe(CidrWorkspaceListener.TOPIC, object : CidrWorkspaceListener {
      override fun workspaceStateChanged(
        workspace: CidrWorkspace,
        oldState: CidrWorkspaceState,
        newState: CidrWorkspaceState,
        allWorkspaces: Map<CidrWorkspace, CidrWorkspaceState>,
      ) {
        val selectedName = selectedWorkspaceComponentName
        val ready = if (selectedName != null) {
          allWorkspaces.all { (ws, state) ->
            // Only wait for the selected workspace to be ready. Irrelevant workspaces
            // (e.g. CMakeWorkspace for a Makefile project) pass unconditionally.
            // Note: != not == — we SKIP irrelevant workspaces, not the selected one.
            ws.javaClass.simpleName != selectedName || state.isReady
          }
        }
        else {
          allWorkspaces.all { (_, state) -> state.isReady }
        }
        if (ready) callBack()
      }
    })
  }
}
