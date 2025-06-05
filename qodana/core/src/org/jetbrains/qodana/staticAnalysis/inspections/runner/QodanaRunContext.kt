package org.jetbrains.qodana.staticAnalysis.inspections.runner

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NotNullLazyValue
import com.intellij.toolWindow.ToolWindowHeadlessManagerImpl
import com.intellij.ui.content.ContentManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withContext
import org.jetbrains.qodana.staticAnalysis.StaticAnalysisDispatchers
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaConfig
import org.jetbrains.qodana.staticAnalysis.inspections.coverageData.CoverageStatisticsData
import org.jetbrains.qodana.staticAnalysis.inspections.coverageData.QodanaCoverageComputationState
import org.jetbrains.qodana.staticAnalysis.inspections.runner.startup.LoadedProfile
import org.jetbrains.qodana.staticAnalysis.profile.QodanaInspectionProfile
import org.jetbrains.qodana.staticAnalysis.profile.QodanaProfile
import org.jetbrains.qodana.staticAnalysis.scopes.QodanaAnalysisScope
import java.nio.file.Path

/**
 * Qodana Context for non-incremental analysis use-case
 */
open class QodanaRunContext(
  val project: Project,
  val loadedProfile: LoadedProfile,
  val scope: QodanaAnalysisScope,
  val qodanaProfile: QodanaProfile,
  val config: QodanaConfig,
  val runCoroutineScope: CoroutineScope,
  val messageReporter: QodanaMessageReporter,
) {
  protected val contentManagerProvider: NotNullLazyValue<ContentManager> = NotNullLazyValue.lazy {
    val mockContentManager = ToolWindowHeadlessManagerImpl.MockToolWindow(project).contentManager
    mockContentManager
  }

  open suspend fun createGlobalInspectionContext(
    outputPath: Path = config.resultsStorage,
    profile: QodanaProfile = qodanaProfile,
    coverageComputationState: QodanaCoverageComputationState = QodanaCoverageComputationState.DEFAULT
  ): QodanaGlobalInspectionContext {
    return withContext(StaticAnalysisDispatchers.IO) {
      QodanaGlobalInspectionContext(
        project,
        contentManagerProvider,
        config,
        outputPath,
        profile,
        runCoroutineScope,
        CoverageStatisticsData(coverageComputationState, project, emptyMap())
      )
    }
  }
}

val QodanaRunContext.baseProfile: QodanaInspectionProfile
  get() = loadedProfile.profile

val QodanaRunContext.projectPath: Path
  get() = config.projectPath
