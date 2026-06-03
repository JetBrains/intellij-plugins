package org.jetbrains.qodana.jvm.coverage

import com.intellij.openapi.project.Project
import org.jetbrains.qodana.coverage.CoverageEngineType
import org.jetbrains.qodana.staticAnalysis.inspections.coverage.BaseQodanaCoverageFileProvider
import java.nio.file.Path


class JvmIcCoverageFileProvider : BaseQodanaCoverageFileProvider() {
  override val engineType: CoverageEngineType = CoverageEngineType.JavaCoverageEngine

  override val canonicalExtension: String = "ic"

  // The IntelliJ coverage agent snapshot is binary and identified by the unique `.ic` extension only
  override fun isValidCoverageReport(file: Path): Boolean = true

  override fun getCoverageFilesPrimaryLocations(project: Project): List<Path> =
    discover(
      project,
      names = listOf("report.ic", "*.ic"),
      dirs = listOf("build/kover", "build/reports/kover"),
    )

  override fun getCoverageFilesSecondaryLocations(project: Project): List<Path> = emptyList()
}
