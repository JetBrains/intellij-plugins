package org.jetbrains.qodana.staticAnalysis.inspections.runner.startup

import com.intellij.codeInspection.InspectionsBundle
import com.intellij.diagnostic.PluginException
import com.intellij.internal.statistic.service.fus.collectors.FUCounterUsageLogger
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import org.jetbrains.qodana.coroutines.QodanaDispatchers
import org.jetbrains.qodana.global.QodanaConfigJson
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaConfig
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaMessageReporter
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaRunContext
import org.jetbrains.qodana.staticAnalysis.qodanaEnv
import org.jetbrains.qodana.staticAnalysis.stat.UsageCollector
import org.jetbrains.qodana.staticAnalysis.stat.UsageCollector.logPromoGithubConfigPresent
import kotlin.io.path.readText

private val logger = logger<QodanaRunContextFactory>()

fun interface QodanaRunContextFactory {
  suspend fun openRunContext(): QodanaRunContext
}

internal class DefaultRunContextFactory(
  private val reporter: QodanaMessageReporter,
  private val config: QodanaConfig,
  private val scope: CoroutineScope,
  private val projectLoader: QodanaProjectLoader = QodanaProjectLoader(reporter),
) : QodanaRunContextFactory {

  override suspend fun openRunContext(): QodanaRunContext {
    val project = projectLoader.openProject(config)
    projectLoader.configureProject(config, project)
    reporter.reportMessageNoLineBreak(1, InspectionsBundle.message("inspection.application.initializing.project"))

    val loadedProfile = LoadedProfile.load(config, project, reporter)
    reportUsage(loadedProfile, project)
    reporter.reportMessage(1, InspectionsBundle.message("inspection.application.chosen.profile.log.message", loadedProfile.profile.name))
    scope.launch {
      try {
        awaitCancellation()
      }
      finally {
        withContext(NonCancellable) { projectLoader.closeProject(config, project) }
      }
    }
    // Awkward, but the easiest way to share code with tests which need to control parts of the run context
    return PreconfiguredRunContextFactory(config, reporter, project, loadedProfile, scope)
      .openRunContext()
  }

  private fun reportUsage(loadedProfile: LoadedProfile, project: Project) {
    UsageCollector.logEnv(qodanaEnv().QODANA_ENV.value)
    UsageCollector.logConfig(config, loadedProfile.nameForReporting, loadedProfile.pathForReporting)
    UsageCollector.logLicense(config.license)
    scope.launch {
      logPromoGithubConfigPresent(project)
    }

    if (config.yamlFiles.effectiveQodanaYaml != null) {
      UsageCollector.logQodanaYamlPresent()
    }
    if (config.yamlFiles.qodanaConfigJson != null) {
      scope.launch(QodanaDispatchers.IO) {
        val config = Json.decodeFromString<QodanaConfigJson>(
          config.yamlFiles.qodanaConfigJson.readText()
        )

        val source = when {
          config.local != null && config.global != null -> UsageCollector.QodanaConfigSource.LOCAL_AND_GLOBAL
          config.local != null -> UsageCollector.QodanaConfigSource.LOCAL
          config.global != null -> UsageCollector.QodanaConfigSource.GLOBAL
          else -> UsageCollector.QodanaConfigSource.UNKNOWN
        }

        UsageCollector.logQodanaConfigSource(source)
      }
    }
    try {
      FUCounterUsageLogger.getInstance().logRegisteredGroups()
    }
    catch (e: PluginException) {
      logger.error(e)
    }
  }
}
