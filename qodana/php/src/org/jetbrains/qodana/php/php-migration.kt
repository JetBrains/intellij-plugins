package org.jetbrains.qodana.php

import com.intellij.openapi.application.writeAction
import com.intellij.openapi.project.Project
import com.jetbrains.php.config.PhpLanguageLevel
import com.jetbrains.php.config.PhpProjectConfigurationFacade
import com.jetbrains.qodana.sarif.model.Run
import com.jetbrains.qodana.sarif.model.SarifReport
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaConfig
import org.jetbrains.qodana.staticAnalysis.inspections.runner.*
import org.jetbrains.qodana.staticAnalysis.inspections.runner.startup.QodanaRunContextFactory
import org.jetbrains.qodana.staticAnalysis.script.*


private const val FROM_LEVEL_PROPERTY = "fromLevel"
private const val TO_LEVEL_PROPERTY = "toLevel"

class PhpMigrationScriptFactory : QodanaScriptFactory {
  override val scriptName: String get() = "php-migration"

  override fun parseParameters(parameters: String): Map<String, String> {
    val versions = parameters.split("-to-")
    if (versions.size != 2 || versions[0].isEmpty() || versions[1].isEmpty()) {
      throw QodanaException(
        "CLI parameter for php-migration must be passed as '--script php-migration:%fromVersion%-to-%toVersion%'. " +
        "For example '--script php-migration:7.1-to-8.0'.")
    }

    return mapOf(FROM_LEVEL_PROPERTY to versions[0], TO_LEVEL_PROPERTY to versions[1])
  }

  override fun createScript(
    config: QodanaConfig,
    messageReporter: QodanaMessageReporter,
    contextFactory: QodanaRunContextFactory,
    parameters: UnvalidatedParameters
  ): QodanaScript {
    // need to use Any here, as jackson would prefer to use doubles for these numbers when possible
    // so 7.1 is Double, 7.1.0 is String
    val fromLevel = parameters.require<Any>(FROM_LEVEL_PROPERTY).toString().toPhpLanguageLevel()
    val toLevel = parameters.require<Any>(TO_LEVEL_PROPERTY).toString().toPhpLanguageLevel()
    return PhpMigrationScript(config, messageReporter, contextFactory, fromLevel, toLevel)
  }
}

/** Reports the problems that differ when migrating a PHP project from one language level to another. */
class PhpMigrationScript(
  config: QodanaConfig,
  messageReporter: QodanaMessageReporter,
  contextFactory: QodanaRunContextFactory,
  private val fromLevel: PhpLanguageLevel,
  private val toLevel: PhpLanguageLevel
) : ComparingScript(config, messageReporter, contextFactory, AnalysisKind.OTHER) {
  private lateinit var prevLevel: PhpLanguageLevel

  override suspend fun setUpAll(runContext: QodanaRunContext) {
    prevLevel = PhpProjectConfigurationFacade.getInstance(runContext.project).languageLevel
  }

  override suspend fun tearDownAll(runContext: QodanaRunContext) = setLevel(prevLevel, runContext.project)
  override suspend fun setUpBefore(runContext: QodanaRunContext) = setLevel(fromLevel, runContext.project)
  override suspend fun setUpAfter(runContext: QodanaRunContext) = setLevel(toLevel, runContext.project)

  private suspend fun setLevel(level: PhpLanguageLevel, project: Project) {
    writeAction {
      PhpProjectConfigurationFacade.getInstance(project).languageLevel = level
    }
  }

  override suspend fun runBefore(report: SarifReport, run: Run, runContext: QodanaRunContext) {
    runTaskAndLogTime("First stage of php-migration analysis: run inspections with language level ${fromLevel.presentableName}") {
      super.runBefore(report, run, runContext)
    }
  }

  override suspend fun runAfter(report: SarifReport, run: Run, runContext: QodanaRunContext): QodanaScriptResult {
    return runTaskAndLogTime("Second stage of php-migration analysis: run inspections with language level ${toLevel.presentableName}") {
      super.runAfter(report, run, runContext)
    }
  }
}
