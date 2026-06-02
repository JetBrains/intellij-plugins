package org.jetbrains.qodana.php

import com.intellij.openapi.project.Project
import org.jetbrains.qodana.coverage.CoverageEngineType
import org.jetbrains.qodana.staticAnalysis.inspections.coverage.BaseQodanaCoverageFileProvider
import java.nio.file.Path


class PhpCoverageFileProvider : BaseQodanaCoverageFileProvider() {
  override val engineType: CoverageEngineType = CoverageEngineType.PhpUnitCoverageEngine

  override fun getCoverageFilesPrimaryLocations(project: Project): List<Path> {
    TODO("not implemented")
  }

  override fun getCoverageFilesSecondaryLocations(project: Project): List<Path> {
    TODO("not implemented")
  }

}