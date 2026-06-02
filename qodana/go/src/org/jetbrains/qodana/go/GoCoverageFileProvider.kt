package org.jetbrains.qodana.go

import com.intellij.openapi.project.Project
import org.jetbrains.qodana.coverage.CoverageEngineType
import org.jetbrains.qodana.staticAnalysis.inspections.coverage.BaseQodanaCoverageFileProvider
import java.nio.file.Path


class GoCoverageFileProvider : BaseQodanaCoverageFileProvider() {
  override val engineType: CoverageEngineType = CoverageEngineType.GoCoverageEngine

  override fun getCoverageFilesPrimaryLocations(project: Project): List<Path> {
    TODO()
  }

  override fun getCoverageFilesSecondaryLocations(project: Project): List<Path> {
    TODO()
  }

}