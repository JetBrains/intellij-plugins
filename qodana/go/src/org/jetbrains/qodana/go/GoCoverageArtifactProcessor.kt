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
      val suite = engine.createCoverageSuite(artifact.id, project, runner, dummyProvider, -1)
      val coverageData = withContext(QodanaDispatchers.IO) {
        ProjectDataLoader.load(artifact.path)
      } ?: return null
      val bundle = remapCoverageFromCloud(suite, coverageData, artifacts) ?: return null
      // The canonical report only carries line data, so we need GoCoverageProjectDataArtifact to fill missing fields
      val goArtifact = artifacts[GO_PROJECT_DATA] as? GoCoverageProjectDataArtifact ?: return bundle
      val lineData = bundle.coverageData ?: return bundle
      QodanaCoverageBundle(suite, buildGoCoverageProjectData(project, lineData, goArtifact))
    }
    return null
  }

  private suspend fun buildGoCoverageProjectData(
    project: Project,
    lineData: ProjectData,
    artifact: GoCoverageProjectDataArtifact,
  ): GoCoverageProjectData {
    val goData = GoCoverageProjectData()
    // Files that still carry live line data; in incremental runs remapCoverageFromCloud nulls out the lines of files
    // outside the changed set, and we must mirror that pruning for the ranges so the coverage tree stays consistent.
    val fileLineData = mutableMapOf<String, Map<Int, LineData?>>()
    lineData.classes.forEach { (name, classData) ->
      @Suppress("UNCHECKED_CAST")
      val lines = classData.lines as Array<LineData?>
      goData.getOrCreateClassData(name).setLines(lines)
      // lines are 1-based
      fileLineData[name] = lines.indices.associateWith { line -> lines[line] }
    }

    val projectDir = project.guessProjectDir()
    val goProjectData = withContext(QodanaDispatchers.IO) { readGoCoverageProjectData(artifact.path) }
    for ((relative, ranges) in goProjectData) {
      val absolute = projectDir?.findFileByRelativePath(FileUtil.toSystemIndependentName(relative))?.path ?: continue
      val lineMap = fileLineData[absolute] ?: continue

      for (range in ranges) {
        // Clip to lines present in lineData
        val clippedStart = maxOf(range.startLine, lineMap.keys.minOrNull() ?: continue)
        val clippedEnd = minOf(range.endLine, lineMap.keys.maxOrNull() ?: continue)

        if (clippedStart <= clippedEnd) {
          // Use actual hits from LineData for covered lines in this range
          val hits = (clippedStart..clippedEnd).sumOf { lineNum ->
            lineMap[lineNum]?.hits ?: 0
          }.toLong()

          goData.addData(
            absolute, clippedStart,
            if (clippedStart == range.startLine) range.startColumn else 0,
            clippedEnd,
            if (clippedEnd == range.endLine) range.endColumn else Int.MAX_VALUE,
            range.statements, hits
          )
        }
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