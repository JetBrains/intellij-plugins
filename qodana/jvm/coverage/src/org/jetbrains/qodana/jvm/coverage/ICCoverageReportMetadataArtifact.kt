package org.jetbrains.qodana.jvm.coverage

import com.intellij.coverage.JavaCoverageEngine
import org.jetbrains.qodana.coverage.CoverageEngineType
import org.jetbrains.qodana.coverage.CoverageMetaDataArtifact
import org.jetbrains.qodana.report.ReportMetadataArtifactProvider
import org.jetbrains.qodana.staticAnalysis.inspections.config.COVERAGE_OUTPUT_DIR
import java.nio.file.Path

internal val JVM_COVERAGE = CoverageEngineType.JavaCoverageEngine.name
private val ENGINE = JavaCoverageEngine::class.java.simpleName

class ICCoverageReportMetadataArtifact : ReportMetadataArtifactProvider {
  override val name = JVM_COVERAGE
  override val fileName = "$COVERAGE_OUTPUT_DIR/$ENGINE"
  override val presentableFileName = "$ENGINE.ic"

  override suspend fun readReport(path: Path) = CoverageMetaDataArtifact(name, path.toFile())
}