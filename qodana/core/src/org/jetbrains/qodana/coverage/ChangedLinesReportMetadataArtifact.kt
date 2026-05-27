package org.jetbrains.qodana.coverage

import com.intellij.openapi.diagnostic.Logger.shouldRethrow
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.VirtualFileManager
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import org.jetbrains.qodana.report.ReportMetadata
import org.jetbrains.qodana.report.ReportMetadataArtifactProvider
import org.jetbrains.qodana.staticAnalysis.inspections.config.COVERAGE_OUTPUT_DIR
import java.nio.file.Files
import java.nio.file.Path

const val CHANGED_LINES_ARTIFACT_ID: String = "changedLines"
const val CHANGED_LINES_FILE_NAME: String = "changedLines"

private val LOG = logger<ChangedLinesReportMetadataArtifact>()

@Serializable
data class ChangedLinesArtifactPayload(
  val version: Int = 1,
  val files: Map<String, Set<Int>>,
)

/**
 * Cloud-side representation of the changed-lines sidecar. Wraps the `changedLines` JSON file produced
 * during the scan by [writeChangedLinesArtifact] and consumed IDE-side via [readChangedLinesPayload].
 */
data class ChangedLinesMetaDataArtifact(override val id: String, val path: Path) : ReportMetadata

class ChangedLinesReportMetadataArtifact : ReportMetadataArtifactProvider {
  override val name: String = CHANGED_LINES_ARTIFACT_ID
  override val fileName: String = "$COVERAGE_OUTPUT_DIR/$CHANGED_LINES_FILE_NAME"
  override val presentableFileName: String = CHANGED_LINES_FILE_NAME

  override suspend fun readReport(path: Path): ReportMetadata? {
    if (!Files.isRegularFile(path)) return null
    return ChangedLinesMetaDataArtifact(name, path)
  }
}

@OptIn(ExperimentalSerializationApi::class)
private val JSON: Json = Json {
  ignoreUnknownKeys = true
  encodeDefaults = true
}

/**
 * Convert a `CoverageStatisticsData.exposeChangedRanges` map (VirtualFile.url -> 1-based lines) into the
 * map with file paths relative to project root. Files outside [projectPath] are skipped.
 */
fun buildChangedLinesPayload(urlToLines: Map<String, Set<Int>>, projectPath: Path): ChangedLinesArtifactPayload {
  val base = projectPath.toAbsolutePath().normalize()
  val files = urlToLines
    .filter { (_, lines) -> lines.isNotEmpty() }
    .mapNotNull { (url, lines) -> url.toProjectRelativePath(base)?.to(lines.toSortedSet()) }
    .toMap(LinkedHashMap())
  return ChangedLinesArtifactPayload(files = files)
}

private fun String.toProjectRelativePath(base: Path): String? {
  val localPath = VirtualFileManager.extractPath(this).takeIf(String::isNotBlank) ?: return null
  val absolute = runCatching { Path.of(localPath).toAbsolutePath().normalize() }.getOrNull() ?: return null
  val relative = runCatching { base.relativize(absolute) }.getOrNull() ?: return null
  return FileUtil.toSystemIndependentName(relative.toString())
    .takeUnless { it.isBlank() || it.startsWith("..") }
}

/** Serialize [payload] to `$coveragePath/changedLines` as JSON. Skips when the payload is empty. */
@OptIn(ExperimentalSerializationApi::class)
fun writeChangedLinesArtifact(coveragePath: Path, payload: ChangedLinesArtifactPayload) {
  if (payload.files.isEmpty()) return
  Files.createDirectories(coveragePath)
  val target = coveragePath.resolve(CHANGED_LINES_FILE_NAME)
  try {
    Files.newOutputStream(target).use { JSON.encodeToStream(payload, it) }
  }
  catch (e: Exception) {
    if(shouldRethrow(e)) throw e
    LOG.warn("Failed to write changed-lines coverage sidecar", e)
  }
}

/**
 * Read a previously written sidecar. Returns null when the file is missing or malformed — callers must
 * treat that as "no filtering" rather than an error.
 */
@OptIn(ExperimentalSerializationApi::class)
fun readChangedLinesPayload(path: Path): ChangedLinesArtifactPayload? {
  if (!Files.isRegularFile(path)) return null
  return try {
    Files.newInputStream(path).use { JSON.decodeFromStream<ChangedLinesArtifactPayload>(it) }
  }
  catch (e: Exception) {
    LOG.warn("Failed to parse changed-lines coverage sidecar at ${path.toAbsolutePath()}", e)
    null
  }
}
