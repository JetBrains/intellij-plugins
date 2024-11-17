package org.jetbrains.qodana.jvm.coverage

import com.intellij.coverage.CoverageDataManager
import com.intellij.coverage.CoverageEngine
import com.intellij.coverage.CoverageSuitesBundle
import com.intellij.coverage.JavaCoverageEngine
import com.intellij.openapi.project.Project
import com.intellij.rt.coverage.data.ProjectData
import kotlinx.coroutines.withContext
import org.jetbrains.qodana.coroutines.QodanaDispatchers
import org.jetbrains.qodana.staticAnalysis.inspections.coverage.CoverageCloudArtifactsProcessor
import org.jetbrains.qodana.coverage.CoverageMetaDataArtifact
import org.jetbrains.qodana.registry.QodanaRegistry.openCoveragePackageLength
import org.jetbrains.qodana.registry.QodanaRegistry.openCoverageSmartFilteringEnabled
import org.jetbrains.qodana.report.ReportMetadata

class ICCoverageArtifactProcessor: CoverageCloudArtifactsProcessor {
  override suspend fun process(artifacts: Map<String, ReportMetadata>, project: Project): CoverageSuitesBundle? {
    val artifact = (artifacts[JVM_COVERAGE] as? CoverageMetaDataArtifact) ?: return null
    val engine = CoverageEngine.EP_NAME.findExtensionOrFail(JavaCoverageEngine::class.java)
    val runner = CoverageCloudArtifactsProcessor.getCoverageRunner(artifact.path) ?: return null
    if (runner.acceptsCoverageEngine(engine)) {
      val fileProvider = ICCoverageFileProvider(artifact.path)
      val suite = withContext(QodanaDispatchers.IO) {
        val noFiltersSuite = engine.createCoverageSuite(artifact.id, project, runner, fileProvider, -1) ?: return@withContext null
        if (openCoverageSmartFilteringEnabled) {
          val covData = noFiltersSuite.getCoverageData(CoverageDataManager.getInstance(project)) ?: return@withContext null
          val commonPackagesFilter = coverageFilterComputation(covData)
          engine.createSuite(runner, artifact.id, fileProvider, commonPackagesFilter, null, -1,
                             false, true, false, project) ?: return@withContext null
        } else {
          noFiltersSuite
        }
      }
      return CoverageSuitesBundle(suite)
    }
    return null
  }

  private fun coverageFilterComputation(covData: ProjectData): Array<String> {
    return covData.classes
      .keys
      .asSequence()
      .map { (it.split('.').dropLast(1).take(openCoveragePackageLength) + "*").joinToString(".") }
      .toSet()
      .toTypedArray<String>()
  }
}