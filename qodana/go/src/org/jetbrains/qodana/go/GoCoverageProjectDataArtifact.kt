package org.jetbrains.qodana.go

import com.goide.execution.testing.coverage.GoCoverageProjectData
import com.intellij.openapi.diagnostic.Logger.shouldRethrow
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.util.io.FileUtil
import org.jetbrains.qodana.report.ReportMetadata
import org.jetbrains.qodana.report.ReportMetadataArtifactProvider
import org.jetbrains.qodana.staticAnalysis.inspections.config.COVERAGE_OUTPUT_DIR
import java.io.DataInputStream
import java.io.DataOutputStream
import java.nio.file.Files
import java.nio.file.Path

internal const val GO_PROJECT_DATA = "GoCoverageProjectData"
internal const val GO_PROJECT_DATA_FILE_NAME = "GoCoverageProjectData"
private const val GO_PROJECT_DATA_VERSION = 1

private val LOG = logger<GoCoverageProjectDataReportMetadataArtifact>()

/**
 * A single Go coverage range, mirroring the six fields of [GoCoverageProjectData.RangeData]
 */
data class GoCoverageData(
  val startLine: Int,
  val startColumn: Int,
  val endLine: Int,
  val endColumn: Int,
  val statements: Int,
  val hits: Long,
)

/**
 * Cloud-side representation of the Go project-data sidecar. Wraps the binary file produced during the scan by
 * [writeGoCoverageProjectData] and consumed IDE-side via [readGoCoverageProjectData].
 */
data class GoCoverageProjectDataArtifact(override val id: String, val path: Path) : ReportMetadata

class GoCoverageProjectDataReportMetadataArtifact : ReportMetadataArtifactProvider {
  override val name: String = GO_PROJECT_DATA
  override val fileName: String = "$COVERAGE_OUTPUT_DIR/$GO_PROJECT_DATA_FILE_NAME"
  override val presentableFileName: String = GO_PROJECT_DATA_FILE_NAME

  override suspend fun readReport(path: Path): ReportMetadata? {
    if (!Files.isRegularFile(path)) return null
    return GoCoverageProjectDataArtifact(name, path)
  }
}

private fun projectRelativePath(projectPath: Path, absolute: String): String? {
  val relative = runCatching { projectPath.relativize(Path.of(absolute)).toString() }.getOrNull() ?: return null
  return FileUtil.toSystemIndependentName(relative).takeUnless { it.isBlank() || it.startsWith("..") }
}

fun writeGoCoverageProjectData(coveragePath: Path, data: GoCoverageProjectData, projectPath: Path) {
  val byFile = LinkedHashMap<String, List<GoCoverageData>>()
  data.processFiles { fileData ->
    val relative = projectRelativePath(projectPath, fileData.myFilePath)
    if (relative != null) {
      byFile[relative] = fileData.myRangesData.values.map { range ->
        GoCoverageData(range.startLine, range.startColumn, range.endLine, range.endColumn, range.statements, range.hits)
      }
    }
    true
  }
  if (byFile.isEmpty()) return
  Files.createDirectories(coveragePath)
  val target = coveragePath.resolve(GO_PROJECT_DATA_FILE_NAME)
  try {
    DataOutputStream(Files.newOutputStream(target).buffered()).use { out ->
      out.writeInt(GO_PROJECT_DATA_VERSION)
      out.writeInt(byFile.size)
      for ((path, ranges) in byFile) {
        out.writeUTF(path)
        out.writeInt(ranges.size)
        for (range in ranges) {
          out.writeInt(range.startLine)
          out.writeInt(range.startColumn)
          out.writeInt(range.endLine)
          out.writeInt(range.endColumn)
          out.writeInt(range.statements)
          out.writeLong(range.hits)
        }
      }
    }
  }
  catch (e: Exception) {
    if (shouldRethrow(e)) throw e
    LOG.warn("Failed to write Go coverage project data information", e)
  }
}

/**
 * Returns an empty map when the file is missing or malformed
 */
fun readGoCoverageProjectData(path: Path): Map<String, List<GoCoverageData>> {
  if (!Files.isRegularFile(path)) return emptyMap()
  return try {
    DataInputStream(Files.newInputStream(path).buffered()).use { input ->
      val version = input.readInt()
      if (version != GO_PROJECT_DATA_VERSION) {
        LOG.warn("Unsupported Go coverage project-data version $version at ${path.toAbsolutePath()}")
        return emptyMap()
      }
      val fileCount = input.readInt()
      val result = LinkedHashMap<String, List<GoCoverageData>>(fileCount)
      repeat(fileCount) {
        val filePath = input.readUTF()
        val rangeCount = input.readInt()
        val ranges = ArrayList<GoCoverageData>(rangeCount)
        repeat(rangeCount) {
          val startLine = input.readInt()
          val startColumn = input.readInt()
          val endLine = input.readInt()
          val endColumn = input.readInt()
          val statements = input.readInt()
          val hits = input.readLong()
          ranges.add(GoCoverageData(startLine, startColumn, endLine, endColumn, statements, hits))
        }
        result[filePath] = ranges
      }
      result
    }
  }
  catch (e: Exception) {
    if (shouldRethrow(e)) throw e
    LOG.warn("Failed to parse Go coverage project data information at ${path.toAbsolutePath()}", e)
    emptyMap()
  }
}
