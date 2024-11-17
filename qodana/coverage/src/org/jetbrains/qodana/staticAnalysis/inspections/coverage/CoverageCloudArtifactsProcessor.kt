package org.jetbrains.qodana.staticAnalysis.inspections.coverage

import com.intellij.coverage.CoverageRunner
import com.intellij.coverage.CoverageSuitesBundle
import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Comparing
import org.jetbrains.qodana.coverage.CoverageLanguage
import org.jetbrains.qodana.report.ReportMetadata
import org.jetbrains.qodana.stats.logCoverageReceivedStats
import java.io.File

/*
 * EP responsible for handling downloaded coverage artifacts. Ideally there should be one processor per every coverage
 * engine, however it depends on the engine's ability to dump and read its results.
 *
 * Note: Newly supported coverage engine types should be added to [CoverageEngineType]
 */
interface CoverageCloudArtifactsProcessor {
  companion object {
    private val EP_NAME = ExtensionPointName.create<CoverageCloudArtifactsProcessor>("com.intellij.qodana.coverage.cloudArtifactsProcessor")

    suspend fun runCoverageProviders(artifacts: Map<String, ReportMetadata>, project: Project): CoverageSuitesBundle? {
      if (artifacts.isNotEmpty()) {
        for (provider in EP_NAME.extensionList) {
          val suite = provider.process(artifacts, project)
          if (suite != null) {
            logCoverageReceivedStats(project, true, listOf(CoverageLanguage.mapEngine(suite.coverageEngine::class.java.simpleName).name))
            return suite
          }
        }
      }
      logCoverageReceivedStats(project, false, listOf(CoverageLanguage.None.name))
      return null
    }

    fun getCoverageRunner(file: File): CoverageRunner? {
      for (runner in CoverageRunner.EP_NAME.extensionList) {
        for (extension in runner.dataFileExtensions) {
          if (Comparing.strEqual(file.extension, extension) && runner.canBeLoaded(file)) return runner
        }
      }
      return null
    }
  }

  suspend fun process(artifacts: Map<String, ReportMetadata>, project: Project): CoverageSuitesBundle?
}