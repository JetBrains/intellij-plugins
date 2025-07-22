package org.jetbrains.qodana.staticAnalysis.script

import com.jetbrains.qodana.sarif.model.SarifReport
import org.jetbrains.qodana.staticAnalysis.inspections.coverageData.CoverageStatisticsData
import org.jetbrains.qodana.staticAnalysis.inspections.coverageData.precomputedCoverageFiles
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaGlobalInspectionContext
import org.jetbrains.qodana.staticAnalysis.profile.QodanaProfile
import java.nio.file.Path
import kotlin.collections.orEmpty

interface QodanaScript {
  suspend fun execute(report: SarifReport): QodanaScriptResult
  val analysisKind: AnalysisKind
}

data class QodanaScriptResult(
  val profileState: QodanaProfile.QodanaProfileState,
  val outputPath: Path,
  val coverageStats: CoverageStatisticsData?,
  val coverageFilesPresent: Boolean = false,
) {

  companion object {
    fun create(ctx: QodanaGlobalInspectionContext): QodanaScriptResult = QodanaScriptResult(
      profileState = ctx.profileState,
      outputPath = ctx.outputPath,
      coverageStats = ctx.coverageStatisticsData,
      coverageFilesPresent = ctx.getUserData(precomputedCoverageFiles)
        .orEmpty()
        .any()
    )
  }
}
