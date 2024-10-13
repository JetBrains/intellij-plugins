package org.jetbrains.qodana.php

import com.jetbrains.php.phpunit.coverage.PhpUnitCoverageEngine
import org.jetbrains.qodana.coverage.CoverageEngineType
import org.jetbrains.qodana.coverage.CoverageMetaDataArtifact
import org.jetbrains.qodana.report.ReportMetadataArtifactProvider
import org.jetbrains.qodana.staticAnalysis.inspections.config.COVERAGE_OUTPUT_DIR
import java.nio.file.Path

internal val PHPUNIT_COVERAGE = CoverageEngineType.PhpUnitCoverageEngine.name
private val ENGINE = PhpUnitCoverageEngine::class.java.simpleName

class PhpUnitCoverageReportMetadataArtifact: ReportMetadataArtifactProvider {
  override val name = PHPUNIT_COVERAGE
  override val fileName = "$COVERAGE_OUTPUT_DIR/$ENGINE"
  override val presentableFileName: String = "$ENGINE.xml"

  override suspend fun readReport(path: Path) = CoverageMetaDataArtifact(name, path.toFile())
}