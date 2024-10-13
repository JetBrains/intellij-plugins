package org.jetbrains.qodana.staticAnalysis.script.scoped

import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.vfs.LocalFileSystem
import kotlinx.coroutines.runInterruptible
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import org.jetbrains.qodana.staticAnalysis.StaticAnalysisDispatchers
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaConfig
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaException
import java.nio.file.Path

val LOG = logger<ScopedScriptFactory>()

@Serializable
internal data class ChangedRegion(val firstLine: Int, val count: Int)

@Serializable
@Suppress("unused")
internal data class ChangedFile(val path: String, val added: List<ChangedRegion>, val deleted: List<ChangedRegion>)

@Serializable
internal data class ChangedFiles(val files: List<ChangedFile>)

internal fun collectAddedLines(changedFiles: ChangedFiles, config: QodanaConfig): Map<String, Set<Int>> {
  val addedLines = mutableMapOf<String, MutableSet<Int>>()
  val fs = LocalFileSystem.getInstance()
  changedFiles.files.forEach { file ->
    val path = Path.of(file.path)
    val absolutePath = if (path.isAbsolute) path else config.projectPath.resolve(path)
    val url = fs.findFileByNioFile(absolutePath)?.url ?: return@forEach
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
