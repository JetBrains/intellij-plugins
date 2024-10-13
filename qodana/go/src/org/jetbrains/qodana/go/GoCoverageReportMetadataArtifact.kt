package org.jetbrains.qodana.go

import com.goide.execution.testing.coverage.GoCoverageEngine
import org.jetbrains.qodana.coverage.CoverageEngineType
import org.jetbrains.qodana.coverage.CoverageMetaDataArtifact
import org.jetbrains.qodana.report.ReportMetadataArtifactProvider
import org.jetbrains.qodana.staticAnalysis.inspections.config.COVERAGE_OUTPUT_DIR
import java.nio.file.Path

internal val GO_COVERAGE = CoverageEngineType.GoCoverageEngine.name
private val ENGINE = GoCoverageEngine::class.java.simpleName

class GoCoverageReportMetadataArtifact: ReportMetadataArtifactProvider {
  override val name = GO_COVERAGE
  override val fileName = "$COVERAGE_OUTPUT_DIR/$ENGINE"
  override val presentableFileName: String = "$ENGINE.out"

  override suspend fun readReport(path: Path) = CoverageMetaDataArtifact(name, path.toFile())
}