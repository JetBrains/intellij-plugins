package org.jetbrains.qodana.staticAnalysis.inspections.runner

import com.intellij.openapi.components.serviceAsync
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.withContext
import org.jetbrains.qodana.staticAnalysis.StaticAnalysisDispatchers
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaConfig
import org.jetbrains.qodana.staticAnalysis.inspections.coverageData.CoverageStatisticsData
import org.jetbrains.qodana.staticAnalysis.inspections.coverageData.QodanaCoverageComputationState
import org.jetbrains.qodana.staticAnalysis.inspections.runner.startup.LoadedProfile
import org.jetbrains.qodana.staticAnalysis.profile.QodanaProfile
import org.jetbrains.qodana.staticAnalysis.scopes.QodanaAnalysisScope
import org.jetbrains.qodana.staticAnalysis.script.LocalChangesService
import org.jetbrains.qodana.util.QodanaMessageReporter
import java.nio.file.Path

/**
 * Qodana Context for incremental analysis use-case (scoped-like scripts, teamcity script)
 */
class QodanaRunIncrementalContext private constructor(
  project: Project,
  loadedProfile: LoadedProfile,
  scope: QodanaAnalysisScope,
  qodanaProfile: QodanaProfile,
  config: QodanaConfig,
  runCoroutineScope: CoroutineScope,
  messageReporter: QodanaMessageReporter,
  val changes: Map<String, Set<Int>>,
  val scopeExtended: Map<VirtualFile, Set<String>>,
): QodanaRunContext(project, loadedProfile, scope, qodanaProfile, config, runCoroutineScope, messageReporter) {
  companion object {
    suspend fun QodanaRunContext.asIncremental(changes: Map<String, Set<Int>>,
                                               paths: Iterable<Path>,
                                               onFileIncluded: ((VirtualFile) -> Unit)? = null): QodanaRunIncrementalContext {
      val files = resolveVirtualFiles(config.projectPath, paths)
      return createIncrementalContext(changes, files, emptyMap(), onFileIncluded)
    }

    suspend fun QodanaRunContext.createIncrementalContext(
      changes: Map<String, Set<Int>>,
      files: List<VirtualFile>,
      additionalFiles: Map<VirtualFile, Set<String>>,
      onFileIncluded: ((VirtualFile) -> Unit)?
    ): QodanaRunIncrementalContext {
      project.serviceAsync<LocalChangesService>()
        .isIncrementalAnalysis
        .set(true)
      
      return QodanaRunIncrementalContext(
        project,
        loadedProfile,
        scope.externalFileScope(files + additionalFiles.keys, onFileIncluded),
        qodanaProfile,
        config,
        runCoroutineScope,
        messageReporter,
        changes,
        additionalFiles)
    }
  }

  override suspend fun createGlobalInspectionContext(
    outputPath: Path,
    profile: QodanaProfile,
    coverageComputationState: QodanaCoverageComputationState
  ): QodanaGlobalInspectionContext {
    return withContext(StaticAnalysisDispatchers.IO) {
      QodanaGlobalInspectionIncrementalContext(
        project,
        contentManagerProvider,
        config,
        outputPath,
        profile,
        runCoroutineScope,
        CoverageStatisticsData(coverageComputationState, project, changes),
        scopeExtended
      )
    }
  }
}


suspend fun resolveVirtualFiles(projectPath: Path, paths: Iterable<Path>): List<VirtualFile> {
  val fs = LocalFileSystem.getInstance()
  return runInterruptible(StaticAnalysisDispatchers.IO) {
    paths.asSequence()
      .map { if (it.isAbsolute) it else projectPath.resolve(it) }
      .mapNotNull(fs::findFileByNioFile)
      .toList()
  }
}

suspend fun resolveVirtualFile(projectPath: Path, path: Path): VirtualFile? {
  val fs = LocalFileSystem.getInstance()
  return runInterruptible(StaticAnalysisDispatchers.IO) {
    if (path.isAbsolute) fs.findFileByNioFile(path) else fs.findFileByNioFile(projectPath.resolve(path))
  }
}