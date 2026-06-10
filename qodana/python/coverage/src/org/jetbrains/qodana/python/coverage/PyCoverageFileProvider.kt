package org.jetbrains.qodana.python.coverage


import com.intellij.openapi.project.Project
import org.jetbrains.qodana.coverage.CoverageEngineType
import org.jetbrains.qodana.staticAnalysis.inspections.coverage.BaseQodanaCoverageFileProvider
import org.jetbrains.qodana.staticAnalysis.inspections.coverage.isCoberturaLikeXmlReport
import java.nio.file.Path

internal object PyCoverageFileProvider : BaseQodanaCoverageFileProvider() {
  override val engineType: CoverageEngineType = CoverageEngineType.PyCoverageEngine

  override val canonicalExtension: String = "xml"

  override fun isValidCoverageReport(file: Path): Boolean = isCoberturaLikeXmlReport(file)

  override fun getCoverageFilesPrimaryLocations(project: Project): List<Path> =
    discover(
      project,
      names = listOf("pytest-coverage.xml"),
      dirs = listOf(".", "coverage", "reports"),
    )

  override fun getCoverageFilesSecondaryLocations(project: Project): List<Path> =
    discover(
      project,
      names = listOf("*.xml"),
      dirs = listOf(
        ".", "coverage", "reports", "coverage-reports", ".coverage-reports",
        "build", "test-results", "target", "artifacts", "apps/*", "packages/*",
      ),
    )
}