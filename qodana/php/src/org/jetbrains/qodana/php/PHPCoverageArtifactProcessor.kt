package org.jetbrains.qodana.php

import com.intellij.coverage.CoverageEngine
import com.intellij.coverage.CoverageFileProvider
import com.intellij.coverage.CoverageSuitesBundle
import com.intellij.openapi.project.Project
import com.intellij.rt.coverage.util.ProjectDataLoader
import com.jetbrains.php.phpunit.coverage.PhpUnitCoverageEngine
import kotlinx.coroutines.withContext
import org.jetbrains.qodana.coroutines.QodanaDispatchers
import org.jetbrains.qodana.staticAnalysis.inspections.coverage.CoverageCloudArtifactsProcessor
import org.jetbrains.qodana.coverage.CoverageMetaDataArtifact
import org.jetbrains.qodana.report.ReportMetadata
import org.jetbrains.qodana.staticAnalysis.inspections.coverage.remapCoverageFromCloud

class PHPCoverageArtifactProcessor: CoverageCloudArtifactsProcessor {
  override suspend fun process(artifacts: Map<String, ReportMetadata>, project: Project): CoverageSuitesBundle? {
    val artifact = (artifacts[PHPUNIT_COVERAGE] as? CoverageMetaDataArtifact) ?: return null
    val engine = CoverageEngine.EP_NAME.findExtensionOrFail(PhpUnitCoverageEngine::class.java)
    val runner = CoverageCloudArtifactsProcessor.getCoverageRunner(artifact.path) ?: return null
    if (runner.acceptsCoverageEngine(engine)) {
      return withContext(QodanaDispatchers.IO) {
        val suite = engine.createCoverageSuite(artifact.id, project, runner, dummyProvider, -1) ?: return@withContext null
        val coverageData = ProjectDataLoader.load(artifact.path) ?: return@withContext null
        suite.setCoverageData(coverageData)
        remapCoverageFromCloud(CoverageSuitesBundle(suite))
      }
    }
    return null
  }

  private val dummyProvider: CoverageFileProvider = object : CoverageFileProvider {
    override fun getCoverageDataFilePath() = ""

    override fun ensureFileExists(): Boolean = true

    override fun isValid(): Boolean = true
  }
}