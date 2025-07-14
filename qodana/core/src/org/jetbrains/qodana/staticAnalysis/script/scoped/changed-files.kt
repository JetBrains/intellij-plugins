package org.jetbrains.qodana.staticAnalysis.script.scoped

import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.toNioPathOrNull
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import org.jetbrains.qodana.registry.QodanaRegistry
import org.jetbrains.qodana.staticAnalysis.StaticAnalysisDispatchers
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaConfig
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaException
import org.jetbrains.qodana.staticAnalysis.inspections.runner.collectExtendedFiles
import org.jetbrains.qodana.staticAnalysis.profile.QodanaProfile
import java.nio.file.Path

val LOG = logger<ScopedScriptFactory>()

@Serializable
internal data class ChangedRegion(val firstLine: Int, val count: Int)

@Serializable
@Suppress("unused")
internal data class ChangedFile(val path: String, val added: List<ChangedRegion>, val deleted: List<ChangedRegion>)

@Serializable
internal data class ExtendedFile(val path: String, val extenders: Set<String>)

@Serializable
internal data class ChangedFiles(val files: List<ChangedFile>, val extendedFiles: List<ExtendedFile> = emptyList())

internal fun resolveVirtualFile(filePath: String, basePath: Path?): VirtualFile? {
  val fs = LocalFileSystem.getInstance()
  val path = Path.of(filePath)
  val absolutePath = if (path.isAbsolute) path else basePath?.resolve(path) ?: return null
  return fs.findFileByNioFile(absolutePath)
}

internal fun collectAddedLines(changedFiles: ChangedFiles, config: QodanaConfig): Map<String, Set<Int>> {
  val addedLines = mutableMapOf<String, MutableSet<Int>>()
  changedFiles.files.forEach { file ->
    val virtualFile = resolveVirtualFile(file.path, config.projectPath) ?: return@forEach
    val url = virtualFile.url
    file.added.forEach { region ->
      addedLines.computeIfAbsent(url) { mutableSetOf() }.addAll(region.firstLine until region.firstLine + region.count)
    }
  }
  return addedLines.mapValues { it.value.toSet() }
}

@OptIn(ExperimentalSerializationApi::class)
internal suspend fun parseChangedFiles(path: Path): ChangedFiles {
  val changedFiles = runInterruptible(StaticAnalysisDispatchers.IO) {
    path.toFile().inputStream().use {
      try {
        Json.decodeFromStream<ChangedFiles>(it)
      }
      catch (e: Exception) {
        throw QodanaException("Failed to parse changed files list", e)
      }
    }
  }
  return changedFiles
}

internal suspend fun computeScopeExtenders(project: Project, files: List<ChangedFile>, qodanaProfile: QodanaProfile, root: VirtualFile?): List<ExtendedFile> {
  if (!QodanaRegistry.isScopeExtendingEnabled) return emptyList()
  return withContext(StaticAnalysisDispatchers.Default) {
    val resolvedFiles = files.mapNotNull { file ->
      resolveVirtualFile(file.path, root?.toNioPathOrNull())
    }

    extendedFilesToRelativePaths(collectExtendedFiles(resolvedFiles, qodanaProfile, project), root)
  }
}

internal fun extendedFilesToRelativePaths(
  extendedFilesMap: Map<VirtualFile, Set<String>>,
  projectDir: VirtualFile?,
): List<ExtendedFile> = extendedFilesMap.mapNotNull { (virtualFile, scopeExtenders) ->
  if (projectDir == null) {
    virtualFile.path
  } else {
    VfsUtilCore.getRelativePath(virtualFile, projectDir)
  }?.let { ExtendedFile(it, scopeExtenders) }
}