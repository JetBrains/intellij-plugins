package org.jetbrains.qodana.staticAnalysis.inspections.coverage

import com.intellij.openapi.project.Project
import org.jetbrains.qodana.coverage.CoverageEngineType
import java.nio.file.Path

interface QodanaCoverageFileProvider {
  val engineType: CoverageEngineType

  fun getCoverageFiles(project: Project): List<Path>
}

abstract class BaseQodanaCoverageFileProvider: QodanaCoverageFileProvider {

  override fun getCoverageFiles(project: Project): List<Path> =
    getCoverageFilesPrimaryLocations(project).ifEmpty { getCoverageFilesSecondaryLocations(project) }

  protected abstract fun getCoverageFilesPrimaryLocations(project: Project): List<Path>

  protected abstract fun getCoverageFilesSecondaryLocations(project: Project): List<Path>
}