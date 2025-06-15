package org.jetbrains.qodana.staticAnalysis.script

import com.jetbrains.qodana.sarif.model.SarifReport
import org.jetbrains.qodana.staticAnalysis.inspections.coverageData.CoverageStatisticsData
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaGlobalInspectionContext
import org.jetbrains.qodana.staticAnalysis.profile.QodanaProfile
import java.nio.file.Path

interface QodanaScript {
  suspend fun execute(report: SarifReport): QodanaScriptResult
}

data class QodanaScriptResult(
  val profileState: QodanaProfile.QodanaProfileState,
  val outputPath: Path,
  val coverageStats: CoverageStatisticsData?,
) {

  companion object {
    fun create(ctx: QodanaGlobalInspectionContext): QodanaScriptResult = QodanaScriptResult(
      profileState = ctx.profileState,
      outputPath = ctx.outputPath,
      coverageStats = ctx.coverageStatisticsData
    )
  }
}
