package org.jetbrains.qodana.python

import com.intellij.python.pro.coverage.PyCoverageEngine
import org.jetbrains.qodana.coverage.CoverageEngineType
import org.jetbrains.qodana.coverage.CoverageMetaDataArtifact
import org.jetbrains.qodana.report.ReportMetadataArtifactProvider
import org.jetbrains.qodana.staticAnalysis.inspections.config.COVERAGE_OUTPUT_DIR
import java.nio.file.Path

internal val PY_COVERAGE = CoverageEngineType.PyCoverageEngine.name
private val ENGINE = PyCoverageEngine::class.java.simpleName

class PyCoverageReportMetadataArtifact: ReportMetadataArtifactProvider {
  override val name = PY_COVERAGE
  override val fileName = "$COVERAGE_OUTPUT_DIR/$ENGINE"
  override val presentableFileName: String = "$ENGINE.xml"

  override suspend fun readReport(path: Path) = CoverageMetaDataArtifact(name, path.toFile())
}