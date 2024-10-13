package org.jetbrains.qodana.staticAnalysis.inspections.runner.startup

import com.intellij.openapi.project.Project
import com.intellij.util.awaitCancellationAndInvoke
import kotlinx.coroutines.CoroutineScope
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaConfig
import org.jetbrains.qodana.staticAnalysis.inspections.config.addQodanaAnalysisConfig
import org.jetbrains.qodana.staticAnalysis.inspections.config.removeQodanaAnalysisConfig
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaInspectionProfileLoader
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaMessageReporter
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaRunContext
import org.jetbrains.qodana.staticAnalysis.profile.QodanaProfile
import org.jetbrains.qodana.staticAnalysis.scopes.QodanaAnalysisScope

/**
 * Shared between IDE run and headless run
 */
class QodanaInIdeRunContextFactory(
  private val config: QodanaConfig,
  private val reporter: QodanaMessageReporter,
  private val project: Project,
  private val loadedProfile: LoadedProfile,
  private val analysisScope: QodanaAnalysisScope,
  private val coroutineScope: CoroutineScope
) : QodanaRunContextFactory {
  override suspend fun openRunContext(): QodanaRunContext {
    project.addQodanaAnalysisConfig(config)
    coroutineScope.awaitCancellationAndInvoke {
      project.removeQodanaAnalysisConfig()
    }

    val qodanaProfile = QodanaProfile.create(
      project = project,
      mainInspectionProfile = loadedProfile.profile,
      inspectionProfileLoader = QodanaInspectionProfileLoader(project),
      config = config,
      sanity = !config.disableSanityInspections,
      promo = config.checkRunPromo(loadedProfile.profile)
    )
    val context = QodanaRunContext(
      project,
      loadedProfile,
      analysisScope,
      qodanaProfile,
      config,
      coroutineScope,
      reporter
    )

    return context
  }
}