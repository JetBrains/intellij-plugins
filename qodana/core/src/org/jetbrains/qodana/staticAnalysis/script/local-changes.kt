package org.jetbrains.qodana.staticAnalysis.script

import com.intellij.codeInspection.InspectionsBundle
import com.intellij.openapi.application.readActionBlocking
import com.intellij.openapi.application.writeAction
import com.intellij.openapi.components.serviceAsync
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.blockingContext
import com.intellij.openapi.progress.runBlockingCancellable
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.FilePath
import com.intellij.openapi.vcs.ProjectLevelVcsManager
import com.intellij.openapi.vcs.VcsBundle
import com.intellij.openapi.vcs.VcsMappingListener
import com.intellij.openapi.vcs.changes.ChangeListManager
import com.intellij.openapi.vcs.changes.VcsPreservingExecutor
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDocumentManager
import com.intellij.util.application
import com.intellij.vcsUtil.VcsUtil
import com.jetbrains.qodana.sarif.model.Run
import com.jetbrains.qodana.sarif.model.SarifReport
import git4idea.config.GitVcsSettings
import kotlinx.coroutines.*
import kotlinx.coroutines.future.asDeferred
import org.jetbrains.qodana.staticAnalysis.StaticAnalysisDispatchers
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaConfig
import org.jetbrains.qodana.staticAnalysis.inspections.coverageData.QodanaCoverageComputationState
import org.jetbrains.qodana.staticAnalysis.inspections.runner.*
import org.jetbrains.qodana.staticAnalysis.inspections.runner.startup.QodanaProjectLoader
import org.jetbrains.qodana.staticAnalysis.inspections.runner.startup.QodanaRunContextFactory
import org.jetbrains.qodana.staticAnalysis.profile.QodanaProfile
import org.jetbrains.qodana.staticAnalysis.scopes.QodanaAnalysisScope
import org.jetbrains.qodana.staticAnalysis.vcs.git.getStatus
import org.jetbrains.qodana.staticAnalysis.vcs.git.restoreTrackedFiles
import java.lang.Runnable
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException
import kotlin.time.Duration.Companion.seconds

internal const val CHANGES_SCRIPT_NAME = "local-changes"
internal val PATHS_ALLOWED_TO_CHANGE = setOf(".shelf")

class LocalChangesScriptFactory : QodanaScriptFactory {
  override val scriptName: String get() = CHANGES_SCRIPT_NAME

  override fun parseParameters(parameters: String): Map<String, String> {
    if (parameters != "") throw QodanaException("The '$scriptName' script does not take parameters")
    return emptyMap()
  }

  override fun createScript(
    config: QodanaConfig,
    messageReporter: QodanaMessageReporter,
    contextFactory: QodanaRunContextFactory,
    parameters: UnvalidatedParameters
  ): QodanaScript = LocalChangesScript(config, messageReporter, contextFactory)
}

open class LocalChangesScript(
  private val config: QodanaConfig,
  private val messageReporter: QodanaMessageReporter,
  contextFactory: QodanaRunContextFactory
) : ComparingScript(config, messageReporter, contextFactory, AnalysisKind.INCREMENTAL) {
  private val isMappingLoaded = CompletableFuture<Unit>()
  private lateinit var scope: QodanaAnalysisScope // available after "runBefore" stage

  override suspend fun setUpAll(runContext: QodanaRunContext) {
    LocalChangesService.getInstance(runContext.project).isIncrementalAnalysis.set(true)
  }

  override suspend fun tearDownAll(runContext: QodanaRunContext) {
    LocalChangesService.getInstance(runContext.project).isIncrementalAnalysis.set(false)
  }

  override suspend fun setUpBefore(runContext: QodanaRunContext) {
    runContext.project.messageBus.connect()
      .subscribe<VcsMappingListener>(
        ProjectLevelVcsManager.VCS_CONFIGURATION_CHANGED,
        VcsMappingListener { isMappingLoaded.complete(Unit) })
  }

  override suspend fun runBefore(report: SarifReport, run: Run, runContext: QodanaRunContext) {
    val outPathBefore = config.resultsStorage.resolve("before")
    withContext(StaticAnalysisDispatchers.IO) { outPathBefore.toFile().mkdir() }

    val beforeQodanaProfile = QodanaProfile.create(runContext.project, runContext.baseProfile,
                                                   QodanaInspectionProfileLoader(runContext.project),
                                                   config, sanity = false, promo = false)

    val beforeContext = runContext.createGlobalInspectionContext(outPathBefore, beforeQodanaProfile, QodanaCoverageComputationState.SKIP_COMPUTE)
    scope = try {
      runAnalysisOnCodeWithoutChanges(runContext.project) {
        QodanaProjectLoader(runContext.messageReporter).configureProjectWithConfigurators(runContext.config, runContext.project)
        runContext.writeProjectDescriptionBeforeWork(outPathBefore)
        val scope = getSearchScopeFromChangedFiles(runContext) // we need to calculate scope after project configuration
        runInspections(scope, run, beforeContext, runContext)
      }
    } finally {
      withContext(NonCancellable) {
        beforeContext.closeQodanaContext()
      }
    }
  }

  /**
   * Returns list of paths updated by restore process
   */
  private suspend fun restoreChangedFilesByBeforeRun(changedFiles: List<FilePath>, project: Project): List<FilePath> {
    val allChangedRoots = getStatus(project)
    val changedRoots = allChangedRoots.filter { entry -> PATHS_ALLOWED_TO_CHANGE.none { entry.path.startsWith(it, true) } }
    if (changedRoots.isEmpty()) return emptyList()
    messageReporter.reportMessage(0, "Some files were changed during the first stage of analysis:")

    changedRoots.forEach {
      messageReporter.reportMessage(0, "  modified file: ${it.path}")
    }

    val conflictsOnUntrackedFiles = changedRoots
      .filter { it.status == "??" }
      .map { it.filePath }
      .filter { changedFiles.contains(it) }

    messageReporter.reportMessage(0, "Restore files to before state")
    restoreTrackedFiles(project)
    conflictsOnUntrackedFiles.forEach {
      it.virtualFile?.delete(this)
    }
    messageReporter.reportMessage(0, "Restore files successfully finished")

    return changedRoots.map { it.filePath }
  }

  override suspend fun runAfter(report: SarifReport, run: Run, runContext: QodanaRunContext): QodanaScriptResult {
    val inspectionContext = runContext.createGlobalInspectionContext(config.resultsStorage, runContext.qodanaProfile, QodanaCoverageComputationState.SKIP_REPORT)
    val scriptResult = try {
      runContext.writeProfiles(inspectionContext.profile)


      QodanaProjectLoader(messageReporter).configureProjectWithConfigurators(config, runContext.project)
      runContext.writeProjectDescriptionBeforeWork(config.outPath)
      runInspections(scope, run, inspectionContext, runContext)

      QodanaScriptResult.create(inspectionContext)
    } finally {
      withContext(NonCancellable) {
        inspectionContext.closeQodanaContext()
      }
    }
    return scriptResult
  }

  private suspend fun runAnalysisAfterShelvingSync(
    project: Project,
    files: List<FilePath>,
    progressIndicator: ProgressIndicator,
    afterShelve: suspend () -> Unit
  ) {
    val versionedRoots = files.map { ProjectLevelVcsManager.getInstance(project).getVcsRootFor(it) }.toSet()
    val message = VcsBundle.message("searching.for.code.smells.freezing.process")
    val vcsSettings = GitVcsSettings.getInstance(project)
    val oldPolicy = vcsSettings.saveChangesPolicy

    val afterShelveRunnable = Runnable {
      runBlockingCancellable { afterShelve.invoke() }
    }
    try {
      vcsSettings.saveChangesPolicy = LocalChangesService.getInstance(project).getGitPolicy()
      blockingContext {
        VcsPreservingExecutor.executeOperation(project, versionedRoots, message, progressIndicator, afterShelveRunnable)
      }
    } finally {
      vcsSettings.saveChangesPolicy = oldPolicy
    }
  }

  private suspend fun runAnalysisOnCodeWithoutChanges(project: Project, analysisRunner: suspend () -> Unit): QodanaAnalysisScope {
    val timeout = if (application.isUnitTestMode) 1L else 60L
    if (ProjectLevelVcsManager.getInstance(project).allVcsRoots.isEmpty()) {
      try {
        withTimeout(timeout.seconds) {
          isMappingLoaded.asDeferred().await()
        }
      }
      catch (_: TimeoutCancellationException) {
        onFailure(InspectionsBundle.message("inspection.application.cannot.initialize.vcs.mapping"))
      }
      catch (_: ExecutionException) {
        onFailure(InspectionsBundle.message("inspection.application.cannot.initialize.vcs.mapping"))
      }
    }

    // First stage scope is files, changed by user before analysis,
    // some of these files could appear/disappear on disk because of shelving process.
    // Possible changes made by analysisRunner(by import project for example) are not considered as scope and reverted after analysis.
    val toAnalyse = ChangeListManager.getInstance(project).affectedPaths.map {
      VcsUtil.getFilePath(it)
    }
    var modified = emptyList<FilePath>()

    runAnalysisAfterShelvingSync(project, toAnalyse, progressIndicator) {
      syncProject(project, toAnalyse)
      analysisRunner()
      //changes made by analysisRunner should be reverted and synced
      modified = restoreChangedFilesByBeforeRun(toAnalyse, project)
    }

    syncProject(project, toAnalyse + modified)
    // After unshelving we are interested in only existed files changed in terms of vcs
    var files = ChangeListManager.getInstance(project).affectedFiles
    val analysisScope = getAnalysisScopeOrFail(project)
    readActionBlocking {
      files = files.filter { analysisScope.contains(it) }
    }
    for (file in files) {
      messageReporter.reportMessage(1, "modified after unshelving: " + file.path)
    }
    return QodanaAnalysisScope(project, files)
  }

  private suspend fun syncProject(project: Project, changes: List<FilePath>) {
    val virtualFiles = changes.mapNotNull { it.virtualFile }.toTypedArray()
    VfsUtil.markDirtyAndRefresh(false, false, false, *virtualFiles)
    writeAction {
      PsiDocumentManager.getInstance(project).commitAllDocuments()
    }
  }

  private fun onFailure(message: String): Nothing {
    throw QodanaException(message)
  }

  private suspend fun getSearchScopeFromChangedFiles(runContext: QodanaRunContext): QodanaAnalysisScope {
    val files = runContext.project.serviceAsync<ChangeListManager>().changedFilesAfterUpdate()
    val excluded = mutableListOf<VirtualFile>()
    val result = runContext.externalFileScope(
      files,
      onFileIncluded = { messageReporter.reportMessage(0, "modified file: ${it.path}") },
      onFileExcluded = excluded::add
    )
    if (excluded.any()) {
      messageReporter.reportMessage(0, "Some changed files were excluded from " +
                                       "the analysis scope according to settings applied:")
      excluded.forEach { messageReporter.reportMessage(0, "  not analyzed changed file: ${it.path}") }
    }
    return result
  }

  private suspend fun ChangeListManager.changedFilesAfterUpdate() = suspendCancellableCoroutine { cont ->
    invokeAfterUpdateWithModal(false, null) {
      cont.resumeWith(runCatching { affectedFiles })
    }
  }

  private fun getAnalysisScopeOrFail(project: Project) =
    QodanaAnalysisScope.fromConfigOrDefault(config, project) { notFound ->
      throw QodanaException(InspectionsBundle.message("inspection.application.directory.cannot.be.found", notFound))
    }
}
