package org.jetbrains.qodana.python.coverage


import com.intellij.openapi.project.Project
import org.jetbrains.qodana.coverage.CoverageEngineType
import org.jetbrains.qodana.staticAnalysis.inspections.coverage.BaseQodanaCoverageFileProvider
import java.nio.file.Path

class PyCoverageFileProvider : BaseQodanaCoverageFileProvider() {
  override val engineType: CoverageEngineType = CoverageEngineType.PyCoverageEngine

  override fun getCoverageFilesPrimaryLocations(project: Project): List<Path> {
    TODO("not implemented")
  }

  override fun getCoverageFilesSecondaryLocations(project: Project): List<Path> {
    TODO("not implemented")
  }
}