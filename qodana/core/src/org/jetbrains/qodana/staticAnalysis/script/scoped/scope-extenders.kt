package org.jetbrains.qodana.staticAnalysis.script.scoped

import com.intellij.codeInspection.ex.ToolsImpl
import com.intellij.openapi.application.readAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import kotlinx.coroutines.withContext
import org.jetbrains.qodana.registry.QodanaRegistry
import org.jetbrains.qodana.staticAnalysis.StaticAnalysisDispatchers
import org.jetbrains.qodana.staticAnalysis.inspections.runner.resolveVirtualFile
import org.jetbrains.qodana.staticAnalysis.profile.QodanaProfile
import org.jetbrains.qodana.staticAnalysis.scopes.InspectionToolScopeExtender
import org.jetbrains.qodana.staticAnalysis.scopes.QodanaScopeExtenderProvider
import org.jetbrains.qodana.staticAnalysis.scopes.collectExtendedFiles
import java.nio.file.Path

internal suspend fun computeScopeExtenders(project: Project, files: List<ChangedFile>, qodanaProfile: QodanaProfile, root: VirtualFile): List<ExtendedFile> {
  if (!QodanaRegistry.isScopeExtendingEnabled) return emptyList()
  return withContext(StaticAnalysisDispatchers.Default) {
    val resolvedFiles = files.mapNotNull { file ->
      resolveVirtualFile(root.toNioPath(), Path.of(file.path))
    }

    extendedFilesToRelativePaths(collectExtendedFiles(resolvedFiles, qodanaProfile, project), root)
  }
}

internal fun extendedFilesToRelativePaths(
  extendedFilesMap: Map<VirtualFile, Set<String>>,
  projectDir: VirtualFile,
): List<ExtendedFile> = extendedFilesMap.mapNotNull { (virtualFile, scopeExtenders) ->
  VfsUtilCore.getRelativePath(virtualFile, projectDir)?.let { ExtendedFile(it, scopeExtenders) }
}

internal suspend fun collectExtendedFiles(files: List<VirtualFile>, qodanaProfile: QodanaProfile, project: Project): Map<VirtualFile, Set<String>> {
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