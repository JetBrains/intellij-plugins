package org.jetbrains.qodana.js

import com.intellij.openapi.project.Project
import org.jetbrains.qodana.coverage.CoverageEngineType
import org.jetbrains.qodana.staticAnalysis.inspections.coverage.BaseQodanaCoverageFileProvider
import org.jetbrains.qodana.staticAnalysis.inspections.coverage.isLcovReport
import java.nio.file.Path

class JsCoverageFileProvider : BaseQodanaCoverageFileProvider() {
  override val engineType: CoverageEngineType = CoverageEngineType.JestCoverageEngine

  override val canonicalExtension: String = "info"

  override fun isValidCoverageReport(file: Path): Boolean = isLcovReport(file)

  override fun getCoverageFilesPrimaryLocations(project: Project): List<Path> =
    discover(
      project,
      names = listOf("lcov.info", "coverage.info", "*.info"),
      dirs = listOf(".", "coverage", "coverage/*", "*/coverage", "packages/*/coverage", "apps/*/coverage", "test-coverage", "reports"),
    )

  override fun getCoverageFilesSecondaryLocations(project: Project): List<Path> =
    discover(
      project,
      names = listOf("*.dat"),
      dirs = listOf("bazel-out/_coverage"),
    )
}
