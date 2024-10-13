package org.jetbrains.qodana.staticAnalysis.script

import com.intellij.psi.PsiElement
import com.jetbrains.qodana.sarif.model.Run
import com.jetbrains.qodana.sarif.model.SarifReport
import kotlinx.coroutines.withContext
import org.jetbrains.qodana.staticAnalysis.StaticAnalysisDispatchers
import org.jetbrains.qodana.staticAnalysis.inspections.coverageData.CoverageStatisticsData
import org.jetbrains.qodana.staticAnalysis.inspections.coverageData.precomputedCoverageFiles
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaGlobalInspectionContext
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaToolResultDatabase
import org.jetbrains.qodana.staticAnalysis.profile.QodanaProfile
import java.io.File
import java.nio.file.Path

interface QodanaScript {
  suspend fun execute(report: SarifReport, run: Run): QodanaScriptResult
}

data class QodanaScriptResult(
  val profileState: QodanaProfile.QodanaProfileState,
  val outputPath: Path,
  val coverageStats: CoverageStatisticsData?,
  val coverageFiles: List<Path>,
  val inspectionNames: Map<String, String>
) {

  companion object {
    suspend fun create(ctx: QodanaGlobalInspectionContext) = QodanaScriptResult(
      profileState = ctx.profileState,
      outputPath = ctx.outputPath,
      coverageStats = ctx.coverageStatisticsData,
      coverageFiles = ctx.getUserData(precomputedCoverageFiles)
        .orEmpty()
        .map(File::toPath),
      inspectionNames = withContext(StaticAnalysisDispatchers.IO) {
        val nullPsiElement: PsiElement? = null

        QodanaToolResultDatabase.open(ctx.outputPath).use {
          it.selectTriggeredInspectionIds()
            .use { stmt ->
              stmt.executeQuery()
                .associateWith { id -> ctx.effectiveProfile.getInspectionTool(id, nullPsiElement)?.displayName ?: id }
            }
        }
      }
    )
  }
}
