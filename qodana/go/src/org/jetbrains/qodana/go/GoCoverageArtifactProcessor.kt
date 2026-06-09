package org.jetbrains.qodana.go

import com.goide.execution.testing.coverage.GoCoverageEngine
import com.goide.execution.testing.coverage.GoCoverageProjectData
import com.intellij.coverage.CoverageEngine
import com.intellij.coverage.CoverageFileProvider
import com.intellij.coverage.CoverageSuitesBundle
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.util.io.FileUtil
import com.intellij.rt.coverage.data.LineData
import com.intellij.rt.coverage.data.ProjectData
import com.intellij.rt.coverage.util.ProjectDataLoader
import kotlinx.coroutines.withContext
import org.jetbrains.qodana.coroutines.QodanaDispatchers
import org.jetbrains.qodana.coverage.CoverageMetaDataArtifact
import org.jetbrains.qodana.report.ReportMetadata
import org.jetbrains.qodana.staticAnalysis.inspections.coverage.CoverageCloudArtifactsProcessor
import org.jetbrains.qodana.staticAnalysis.inspections.coverage.QodanaCoverageBundle
import org.jetbrains.qodana.staticAnalysis.inspections.coverage.remapCoverageFromCloud

class GoCoverageArtifactProcessor: CoverageCloudArtifactsProcessor {
  override suspend fun process(artifacts: Map<String, ReportMetadata>, project: Project): CoverageSuitesBundle? {
    val artifact = (artifacts[GO_COVERAGE] as? CoverageMetaDataArtifact) ?: return null
    val engine = CoverageEngine.EP_NAME.findExtensionOrFail(GoCoverageEngine::class.java)
    val runner = CoverageCloudArtifactsProcessor.getCoverageRunner(artifact.path) ?: return null
    if (runner.acceptsCoverageEngine(engine)) {
      return withContext(QodanaDispatchers.IO) {
        val suite = engine.createCoverageSuite(artifact.id, project, runner, dummyProvider, -1) ?: return@withContext null
        val coverageData = ProjectDataLoader.load(artifact.path) ?: return@withContext null
        val bundle = remapCoverageFromCloud(suite, coverageData, artifacts) ?: return@withContext null
        // The canonical report only carries line data, so we need GoCoverageProjectDataArtifact to fill missing fields
        val goArtifact = artifacts[GO_PROJECT_DATA] as? GoCoverageProjectDataArtifact ?: return@withContext bundle
        val lineData = bundle.coverageData ?: return@withContext bundle
        QodanaCoverageBundle(suite, buildGoCoverageProjectData(project, lineData, goArtifact))
      }
    }
    return null
  }

  /**
   * Rebuild a [GoCoverageProjectData] from the already remapped/filtered [lineData]
   * plus the per-range statements/hits read from the [artifact], remapped back to absolute IDE paths.
   */
  private fun buildGoCoverageProjectData(
    project: Project,
    lineData: ProjectData,
    artifact: GoCoverageProjectDataArtifact,
  ): GoCoverageProjectData {
    val goData = GoCoverageProjectData()
    // Files that still carry live line data; in incremental runs remapCoverageFromCloud nulls out the lines of files
    // outside the changed set, and we must mirror that pruning for the ranges so the coverage tree stays consistent.
    val filesWithLines = HashSet<String>()
    lineData.classes.forEach { (name, classData) ->
      @Suppress("UNCHECKED_CAST")
      val lines = classData.lines as Array<LineData?>
      goData.getOrCreateClassData(name).setLines(lines)
      if (lines.any { it != null }) filesWithLines.add(name)
    }
    val projectDir = project.guessProjectDir()
    for ((relative, ranges) in readGoCoverageProjectData(artifact.path)) {
      val absolute = projectDir?.findFileByRelativePath(FileUtil.toSystemIndependentName(relative))?.path ?: continue
      if (absolute !in filesWithLines) continue
      for (range in ranges) {
        goData.addData(absolute, range.startLine, range.startColumn, range.endLine, range.endColumn, range.statements, range.hits)
      }
    }
    return goData
  }

  private val dummyProvider: CoverageFileProvider = object : CoverageFileProvider {
    override fun getCoverageDataFilePath() = ""

    override fun ensureFileExists(): Boolean = true

    override fun isValid(): Boolean = true
  }
}