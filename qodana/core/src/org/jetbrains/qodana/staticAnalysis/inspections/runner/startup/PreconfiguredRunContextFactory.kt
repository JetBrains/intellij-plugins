package org.jetbrains.qodana.staticAnalysis.inspections.runner.startup

import com.intellij.codeInspection.InspectionsBundle
import com.intellij.openapi.project.Project
import kotlinx.coroutines.CoroutineScope
import org.jetbrains.annotations.VisibleForTesting
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaConfig
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaMessageReporter
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaRunContext
import org.jetbrains.qodana.staticAnalysis.scopes.QodanaAnalysisScope
import org.jetbrains.qodana.staticAnalysis.workflow.QodanaWorkflowExtension

@VisibleForTesting
class PreconfiguredRunContextFactory(
  private val config: QodanaConfig,
  private val reporter: QodanaMessageReporter,
  private val project: Project,
  private val loadedProfile: LoadedProfile,
  private val coroutineScope: CoroutineScope
) : QodanaRunContextFactory {

  override suspend fun openRunContext(): QodanaRunContext {
    val analysisScope = QodanaAnalysisScope.fromConfigOrDefault(config, project, onPathNotFound = { notFound ->
      reporter.reportError(InspectionsBundle.message("inspection.application.directory.cannot.be.found", notFound))
    })
    val context = QodanaInIdeRunContextFactory(
      config,
      reporter,
      project,
      loadedProfile,
      analysisScope,
      coroutineScope
    ).openRunContext()

    QodanaWorkflowExtension.callBeforeLaunch(context)
    return context
  }
}
