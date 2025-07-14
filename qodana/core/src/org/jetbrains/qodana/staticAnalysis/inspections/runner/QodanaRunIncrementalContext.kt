package org.jetbrains.qodana.staticAnalysis.inspections.runner

import com.intellij.codeInspection.ex.ToolsImpl
import com.intellij.openapi.application.readAction
import com.intellij.openapi.components.serviceAsync
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.withContext
import org.jetbrains.qodana.registry.QodanaRegistry
import org.jetbrains.qodana.staticAnalysis.StaticAnalysisDispatchers
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaConfig
import org.jetbrains.qodana.staticAnalysis.inspections.coverageData.CoverageStatisticsData
import org.jetbrains.qodana.staticAnalysis.inspections.coverageData.QodanaCoverageComputationState
import org.jetbrains.qodana.staticAnalysis.inspections.runner.startup.LoadedProfile
import org.jetbrains.qodana.staticAnalysis.profile.QodanaProfile
import org.jetbrains.qodana.staticAnalysis.scopes.InspectionToolScopeExtender
import org.jetbrains.qodana.staticAnalysis.scopes.QodanaAnalysisScope
import org.jetbrains.qodana.staticAnalysis.scopes.QodanaScopeExtenderProvider
import org.jetbrains.qodana.staticAnalysis.scopes.collectExtendedFiles
import org.jetbrains.qodana.staticAnalysis.script.LocalChangesService
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
                                               computeExtendedScope: Boolean = false,
                                               onFileIncluded: ((VirtualFile) -> Unit)? = null): QodanaRunIncrementalContext {
      val files = resolveVirtualFiles(config.projectPath, paths)
      val additionalFiles = if (computeExtendedScope) collectExtendedFiles(files, qodanaProfile, project) else emptyMap()
      return createIncrementalContext(changes, files, additionalFiles, onFileIncluded)
    }

    suspend fun QodanaRunContext.asIncremental(changes: Map<String, Set<Int>>,
                                               extendedFiles: Map<String, Set<String>>,
                                               paths: Iterable<Path>,
                                               onFileIncluded: ((VirtualFile) -> Unit)? = null): QodanaRunIncrementalContext {
      val files = resolveVirtualFiles(config.projectPath, paths)
      val additionalFiles = extendedFiles
        .mapNotNull { (path, scope) -> resolveVirtualFile(config.projectPath, Path.of(path))?.let { it to scope } }
        .toMap()
      return createIncrementalContext(changes, files, additionalFiles, onFileIncluded)
    }

    private suspend fun QodanaRunContext.createIncrementalContext(
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

suspend fun collectExtendedFiles(files: List<VirtualFile>, qodanaProfile: QodanaProfile, project: Project): Map<VirtualFile, Set<String>> {
  if (!QodanaRegistry.isScopeExtendingEnabled) return emptyMap()
  val toolsWithExtenders = findToolsWithScopeExtenders(qodanaProfile)
  val manager = PsiManager.getInstance(project)
  val fileToExtenders = computeFileToScopeExtenders(files, manager, toolsWithExtenders)
  return if (fileToExtenders.any { it.value.isNotEmpty() }) collectExtendedFiles(project, fileToExtenders) else emptyMap()
}

private fun findToolsWithScopeExtenders(qodanaProfile: QodanaProfile): Map<InspectionToolScopeExtender, ToolsImpl> = qodanaProfile.effectiveProfile.tools
  .mapNotNull { toolsImpl -> QodanaScopeExtenderProvider.getExtender(toolsImpl.tool.shortName) to toolsImpl }
  .mapNotNull { (key, value) -> key?.let { it to value } }
  .toMap()

private suspend fun computeFileToScopeExtenders(
  files: List<VirtualFile>,
  psiManager: PsiManager,
  toolsWithExtenders: Map<InspectionToolScopeExtender, ToolsImpl>
): Map<VirtualFile, List<InspectionToolScopeExtender>> = readAction {
  files
    .mapNotNull { psiManager.findFile(it) }
    .associate { psiFile ->
      val enabledExtenders = toolsWithExtenders.filter { (_, tool) -> tool.getEnabledTool(psiFile) != null }.keys.toList()
      enabledExtenders.let {
        psiFile.virtualFile to enabledExtenders
      }
    }
}

private suspend fun resolveVirtualFiles(projectPath: Path, paths: Iterable<Path>): List<VirtualFile> {
  val fs = LocalFileSystem.getInstance()
  return runInterruptible(StaticAnalysisDispatchers.IO) {
    paths.asSequence()
      .map { if (it.isAbsolute) it else projectPath.resolve(it) }
      .mapNotNull(fs::findFileByNioFile)
      .toList()
  }
}

private suspend fun resolveVirtualFile(projectPath: Path, path: Path): VirtualFile? {
  val fs = LocalFileSystem.getInstance()
  return runInterruptible(StaticAnalysisDispatchers.IO) {
    if (path.isAbsolute) fs.findFileByNioFile(path) else fs.findFileByNioFile(projectPath.resolve(path))
  }
}