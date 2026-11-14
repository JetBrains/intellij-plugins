package org.jetbrains.qodana.staticAnalysis.inspections.runner

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NotNullLazyValue
import com.intellij.toolWindow.ToolWindowHeadlessManagerImpl
import com.intellij.ui.content.ContentManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import org.jetbrains.qodana.staticAnalysis.StaticAnalysisDispatchers
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaConfig
import org.jetbrains.qodana.staticAnalysis.inspections.coverageData.CoverageStatisticsData
import org.jetbrains.qodana.staticAnalysis.inspections.coverageData.QodanaCoverageComputationState
import org.jetbrains.qodana.staticAnalysis.inspections.runner.startup.LoadedProfile
import org.jetbrains.qodana.staticAnalysis.profile.QodanaInspectionProfile
import org.jetbrains.qodana.staticAnalysis.profile.QodanaProfile
import org.jetbrains.qodana.staticAnalysis.scopes.QodanaAnalysisScope
import org.jetbrains.qodana.util.QodanaMessageReporter
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap

/**
 * Qodana Context for non-incremental analysis use-case
 */
open class QodanaRunContext(
  val project: Project,
  val loadedProfile: LoadedProfile,
  val scope: QodanaAnalysisScope,
  //TODO fix
  var qodanaProfile: QodanaProfile,
  val config: QodanaConfig,
  val runCoroutineScope: CoroutineScope,
  val messageReporter: QodanaMessageReporter,
) {
  private val startedProjectDescriptions = ConcurrentHashMap<Path, Deferred<Unit>>()

  /**
   * Starts [describe] in the background once per [path]; [takeStartedProjectDescription] hands the job over.
   * The describers read the project model only, so they can run while the results are still being written.
   */
  internal fun startProjectDescription(path: Path, describe: suspend () -> Unit) {
    startedProjectDescriptions.computeIfAbsent(path) { runCoroutineScope.async(StaticAnalysisDispatchers.Default) { describe() } }
  }

  internal fun takeStartedProjectDescription(path: Path): Deferred<Unit>? = startedProjectDescriptions.remove(path)

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
        CoverageStatisticsData(coverageComputationState, project, null)
      )
    }
  }
}

val QodanaRunContext.baseProfile: QodanaInspectionProfile
  get() = loadedProfile.profile

val QodanaRunContext.projectPath: Path
  get() = config.projectPath
