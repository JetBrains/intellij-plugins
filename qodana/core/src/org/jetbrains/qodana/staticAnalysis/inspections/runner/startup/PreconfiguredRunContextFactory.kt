package org.jetbrains.qodana.staticAnalysis.inspections.runner.startup

import com.intellij.codeInspection.InspectionsBundle
import com.intellij.openapi.application.ApplicationManager.getApplication
import com.intellij.openapi.project.Project
import kotlinx.coroutines.CoroutineScope
import org.jetbrains.annotations.VisibleForTesting
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaConfig
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaRunContext
import org.jetbrains.qodana.staticAnalysis.profile.ProfileDescriptionWriter
import org.jetbrains.qodana.staticAnalysis.scopes.QodanaAnalysisScope
import org.jetbrains.qodana.staticAnalysis.workflow.QodanaWorkflowExtension
import org.jetbrains.qodana.staticAnalysis.workflow.callQodanaWorkflowExtensions
import org.jetbrains.qodana.util.QodanaMessageReporter

@VisibleForTesting
class PreconfiguredRunContextFactory(
  private val config: QodanaConfig,
  private val reporter: QodanaMessageReporter,
  private val project: Project,
  private val loadedProfile: LoadedProfile,
) : QodanaRunContextFactory {

  @Suppress("DEPRECATION")
  override suspend fun openRunContext(scope: CoroutineScope): QodanaRunContext {
    val analysisScope = QodanaAnalysisScope.fromConfigOrDefault(config, project, onPathNotFound = { notFound ->
      reporter.reportError(InspectionsBundle.message("inspection.application.directory.cannot.be.found", notFound))
    })
    val context = QodanaInIdeRunContextFactory(
      config,
      reporter,
      project,
      loadedProfile,
      analysisScope,
    ).openRunContext(scope)

    if (getApplication().isHeadlessEnvironment) {
      ProfileDescriptionWriter().writeInspectionsReport(context)
    }
    //Deprecated, kept for backwards compatibility with old plugins
    callQodanaWorkflowExtensions(QodanaWorkflowExtension::beforeLaunch, context)
    return context
  }
}
