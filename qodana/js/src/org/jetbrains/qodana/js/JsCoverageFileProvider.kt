package org.jetbrains.qodana.js

import com.intellij.openapi.project.Project
import org.jetbrains.qodana.coverage.CoverageEngineType
import org.jetbrains.qodana.staticAnalysis.inspections.coverage.BaseQodanaCoverageFileProvider
import org.jetbrains.qodana.staticAnalysis.inspections.coverage.isLcovReport
import java.nio.file.Path

internal object JsCoverageFileProvider : BaseQodanaCoverageFileProvider() {
  override val engineType: CoverageEngineType = CoverageEngineType.JestCoverageEngine

  override val canonicalExtension: String = "info"

  override fun isValidCoverageReport(file: Path): Boolean = isLcovReport(file)

  override fun getCoverageFilesLocations(project: Project): List<Path> {
    val regularReports = discover(
      project,
      names = listOf("lcov.info", "coverage.info", "*.info"),
      dirs = listOf(".", "coverage", "coverage/*", "*/coverage", "packages/*/coverage", "apps/*/coverage", "test-coverage", "reports"),
    )
    val bazelReports = discover(project, names = listOf("*.dat"), dirs = listOf("bazel-out/_coverage"))
    return regularReports + bazelReports
  }
}
