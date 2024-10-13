package org.jetbrains.qodana.js

import com.intellij.javascript.jest.coverage.JestCoverageEngine
import org.jetbrains.qodana.coverage.CoverageEngineType
import org.jetbrains.qodana.coverage.CoverageMetaDataArtifact
import org.jetbrains.qodana.report.ReportMetadataArtifactProvider
import org.jetbrains.qodana.staticAnalysis.inspections.config.COVERAGE_OUTPUT_DIR
import java.nio.file.Path

internal val JEST_COVERAGE = CoverageEngineType.JestCoverageEngine.name
private val ENGINE = JestCoverageEngine::class.java.simpleName

class JestCoverageReportMetadataArtifact: ReportMetadataArtifactProvider {
  override val name = JEST_COVERAGE
  override val fileName = "$COVERAGE_OUTPUT_DIR/$ENGINE"
  override val presentableFileName: String = "$ENGINE.info"

  override suspend fun readReport(path: Path) = CoverageMetaDataArtifact(name, path.toFile())
}