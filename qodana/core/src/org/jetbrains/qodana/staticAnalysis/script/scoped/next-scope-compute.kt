package org.jetbrains.qodana.staticAnalysis.script.scoped

import com.intellij.openapi.components.PathMacroManager
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.jetbrains.qodana.sarif.model.ArtifactLocation
import com.jetbrains.qodana.sarif.model.SarifReport
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToStream
import org.jetbrains.qodana.staticAnalysis.StaticAnalysisDispatchers
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaRunContext
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaRunIncrementalContext
import org.jetbrains.qodana.staticAnalysis.sarif.getOrCreateRun
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import kotlin.io.path.Path

internal suspend fun dumpScopedFile(
  runContext: QodanaRunContext,
  report: SarifReport,
  scopeFile: Path
) {
  val dumpReducedScope = System.getProperty(DUMP_REDUCED_SCOPE_ARG)
  val root = VfsUtil.findFile(runContext.config.projectPath, false)
  if (dumpReducedScope != null) {
    dumpReducedScopeToPath(dumpReducedScope, runContext, report, root)
  }
  else if (runContext is QodanaRunIncrementalContext) {
    dumpFullScopeToPath(scopeFile, runContext, root)
  }
}

private suspend fun dumpReducedScopeToPath(
  dumpReducedScope: String,
  runContext: QodanaRunContext,
  report: SarifReport,
  root: VirtualFile?
) {
  val projectDir = runContext.config.projectPath
  val path = run {
    val p = Path(dumpReducedScope)
    if (p.isAbsolute) p else {
      projectDir.resolve(p)
    }
  }
  val nextScope = computeNextScope(runContext, report, root)
  val extendedFiles = computeScopeExtenders(
    runContext.project, nextScope, runContext.qodanaProfile, root)
  writeNextScope(path, ChangedFiles(nextScope, extendedFiles))
}

private suspend fun dumpFullScopeToPath(
  scopeFilePath: Path,
  runContext: QodanaRunIncrementalContext,
  root: VirtualFile?
) {
  val extendedFiles = extendedFilesToRelativePaths(runContext.scopeExtended, root)
  if (extendedFiles.isNotEmpty()) {
    val nextScope = parseChangedFiles(scopeFilePath).files
    writeNextScope(scopeFilePath, ChangedFiles(nextScope, extendedFiles))
  }
}

internal suspend fun computeNextScope(runContext: QodanaRunContext, report: SarifReport, root: VirtualFile?): List<ChangedFile> {
  return withContext(StaticAnalysisDispatchers.IO) {
    val macroManager = PathMacroManager.getInstance(runContext.project)
    report.getOrCreateRun().results.flatMap { result ->
      result.locations
        ?.mapNotNull {
          it.physicalLocation?.artifactLocation?.uri ?: return@mapNotNull null
          val physicalLocation = it.physicalLocation
          val artifactLocation: ArtifactLocation = physicalLocation.artifactLocation
          val virtualFile = artifactLocation.toVirtualFile(runContext.config.projectPath, macroManager) ?: return@mapNotNull null
          return@mapNotNull if (root == null) virtualFile.path else VfsUtilCore.getRelativePath(virtualFile, root)
        } ?: emptyList()
    }
      .distinct()
      .map { ChangedFile(it, emptyList(), emptyList()) }
  }
}

private fun ArtifactLocation.toVirtualFile(projectPath: Path, macroManager: PathMacroManager): VirtualFile? {
  val path = if (uriBaseId == "SRCROOT") {
    projectPath.resolve(uri)
  } else {
    macroManager.expandPath(uri)?.let { Paths.get(it) } ?: return null
  }
  return LocalFileSystem.getInstance().findFileByNioFile(path)
}

@OptIn(ExperimentalSerializationApi::class)
private suspend fun writeNextScope(path: Path, affectedFiles: ChangedFiles) {
  runInterruptible(StaticAnalysisDispatchers.IO) {
    path.parent.toFile().mkdirs()
    Files.newOutputStream(path, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE).use { os ->
      Json.encodeToStream(affectedFiles, os)
    }
  }
}