package org.jetbrains.qodana.go

import com.intellij.openapi.project.Project
import com.intellij.openapi.diagnostic.logger
import org.jetbrains.qodana.coverage.CoverageEngineType
import org.jetbrains.qodana.staticAnalysis.inspections.coverage.BaseQodanaCoverageFileProvider
import java.io.IOException
import java.nio.file.Path
import kotlin.io.path.useLines


internal object GoCoverageFileProvider : BaseQodanaCoverageFileProvider() {
  override val engineType: CoverageEngineType = CoverageEngineType.GoCoverageEngine

  override val canonicalExtension: String = "out"

  override fun isValidCoverageReport(file: Path): Boolean = isGoCoverProfile(file)

  override fun getCoverageFilesLocations(project: Project): List<Path> =
    discover(
      project,
      names = listOf("coverage.out", "cover.out", "c.out", "profile.txt", "coverage.txt", "cov.txt", "profile.cov", "coverage.cov"),
      dirs = listOf(".", "coverage", ".coverage", "build", "reports", "test-results", "artifacts", "bin", "target"),
    )
}

private val logger = logger<GoCoverageFileProvider>()

/** Go cover profile header: the file's first non-empty line must be exactly `mode: set|count|atomic`. */
private val GO_COVER_HEADER_REGEX = Regex("""^mode:\s+(set|count|atomic)\s*$""")

/** Go cover profile check: the first non-empty line is a `mode: set|count|atomic` header. */
private fun isGoCoverProfile(path: Path): Boolean {
  return try {
    path.useLines { lines ->
      val header = lines.map { it.trim() }.firstOrNull { it.isNotEmpty() } ?: return@useLines false
      GO_COVER_HEADER_REGEX.matches(header)
    }
  }
  catch (e: IOException) {
    logger.warn("Failed to read coverage report $path", e)
    false
  }
}