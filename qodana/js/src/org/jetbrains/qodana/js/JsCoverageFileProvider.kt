package org.jetbrains.qodana.js

import com.intellij.openapi.project.Project
import org.jetbrains.qodana.coverage.CoverageEngineType
import org.jetbrains.qodana.staticAnalysis.inspections.coverage.BaseQodanaCoverageFileProvider
import java.nio.file.Path

class JsCoverageFileProvider : BaseQodanaCoverageFileProvider() {
  override val engineType: CoverageEngineType = CoverageEngineType.JestCoverageEngine

  override fun getCoverageFilesPrimaryLocations(project: Project): List<Path> {
    TODO()
  }

  override fun getCoverageFilesSecondaryLocations(project: Project): List<Path> {
    TODO()
  }

}

