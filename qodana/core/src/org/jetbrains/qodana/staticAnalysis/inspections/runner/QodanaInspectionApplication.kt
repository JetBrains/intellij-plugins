// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.qodana.staticAnalysis.inspections.runner

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.intellij.codeInspection.InspectionApplicationException
import com.intellij.ide.plugins.DisabledPluginsState
import com.intellij.ide.plugins.PluginManager
import com.intellij.internal.statistic.eventLog.EventLogConfiguration
import com.intellij.internal.statistic.eventLog.StatisticsEventLogProviderUtil
import com.intellij.internal.statistic.eventLog.validator.storage.persistence.EventLogMetadataSettingsPersistence
import com.intellij.internal.statistic.updater.StatisticsValidationUpdatedService
import com.intellij.internal.statistic.updater.updateValidationRules
import com.intellij.openapi.application.ApplicationInfo
import com.intellij.openapi.application.ex.ApplicationManagerEx
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.util.application
import com.intellij.util.io.createParentDirectories
import kotlinx.coroutines.*
import org.jetbrains.annotations.VisibleForTesting
import org.jetbrains.qodana.QodanaBundle
import org.jetbrains.qodana.cloudclient.asSuccess
import org.jetbrains.qodana.cloudclient.v1.QDCloudSchema
import org.jetbrains.qodana.license.checkLicense
import org.jetbrains.qodana.license.printLicenseInfo
import org.jetbrains.qodana.publisher.PublishResult
import org.jetbrains.qodana.publisher.schemas.UploadedReport
import org.jetbrains.qodana.runActivityWithTiming
import org.jetbrains.qodana.staticAnalysis.StaticAnalysisDispatchers
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaConfig
import org.jetbrains.qodana.staticAnalysis.inspections.config.addQodanaAnalysisConfig
import org.jetbrains.qodana.staticAnalysis.inspections.config.copyConfigToLog
import org.jetbrains.qodana.staticAnalysis.inspections.config.removeQodanaAnalysisConfig
import org.jetbrains.qodana.staticAnalysis.inspections.runner.startup.DefaultRunContextFactory
import org.jetbrains.qodana.staticAnalysis.inspections.runner.startup.QodanaRunContextFactory
import org.jetbrains.qodana.staticAnalysis.script.QodanaScriptFactory
import org.jetbrains.qodana.staticAnalysis.stat.InspectionEventsCollector.QodanaActivityKind
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import kotlin.system.exitProcess
import kotlin.time.Duration.Companion.minutes

private const val QODANA_PLUGINS_THIRD_PARTY_ACCEPT = "idea.qodana.thirdpartyplugins.accept"

private const val OPEN_IN_IDE_METADATA_JSON = "open-in-ide.json"

class QodanaInspectionApplication(
  val config: QodanaConfig,
  val projectApi: QDCloudLinterProjectApi?, // null if no token
) {
  private val reporter = QodanaMessageReporter.DEFAULT

  suspend fun startup() {
    config.license = checkLicense()
    if (!config.skipPreamble) printLicenseInfo(config.license)

    try {
      if (!config.skipPreamble) {
        printProductHeader()
        printAppInfo()
      }
      run()
    }
    catch (e: InspectionApplicationException) {
      reporter.reportError(e)
      exitProcess(1)
    }
    catch (e: QodanaException) {
      LOG.error(e)
      reporter.reportError(e)
      exitProcess(1)
    }
    catch (e: ProcessCanceledException) {
      reporter.reportError((e.cause as? QodanaCancellationException) ?: e)
      exitProcess(1)
    }
    catch (e: Throwable) {
      LOG.error(e)
      reporter.reportError(e)
      exitProcess(1)
    }

    ApplicationManagerEx.getApplicationEx().exit(true, true)
  }

  @VisibleForTesting
  fun constructQodanaRunner(contextFactory: QodanaRunContextFactory, messageReporter: QodanaMessageReporter = reporter) =
    QodanaRunner(
      QodanaScriptFactory.buildScript(config, contextFactory, messageReporter),
      config,
      messageReporter
    )

  private fun printAppInfo() {
    val appInfo = ApplicationInfo.getInstance()
    reporter.reportMessage(1, QodanaBundle.message(
      "qodana.application.startup",
      "${appInfo.fullApplicationName} (build ${appInfo.build.asString()})"
    ))
  }

  private suspend fun run() {
    val status = runActivityWithTiming(QodanaActivityKind.LINTER_EXECUTION) {
      waitForStatisticsSchemaUpdate()
      checkPlugins()
      supervisorScope {
        val contextFactory = DefaultRunContextFactory(reporter, config, this)
        val runner = constructQodanaRunner(contextFactory)
        launchRunner(runner)
        this.coroutineContext.job.cancelChildren()
        runner.sarifRun.firstExitStatus
      }
    }
    if (status.code != 0) {
      reporter.reportError(status.description)
    }
  }

  private suspend fun waitForStatisticsSchemaUpdate() {
    try {
      withTimeout(1.minutes) {
        // wait for scheduled updated to not accidentally trigger some race condition
        service<StatisticsValidationUpdatedService>().updatedDeferred.join()
        StatisticsEventLogProviderUtil.getEventLogProviders().forEach {
          EventLogMetadataSettingsPersistence.getInstance().setLastModified(it.recorderId, 0)
        }
        updateValidationRules()
      }
    }
    catch (_: TimeoutCancellationException) {
      LOG.warn("Failed to update statistics schema")
    }
  }

  @VisibleForTesting
  suspend fun launchRunner(runner: QodanaRunner) {
    copyConfigToLog(config)
    try {
      try {
        application.addQodanaAnalysisConfig(config)
        runner.run()
      }
      finally {
        application.removeQodanaAnalysisConfig()
      }
    }
    catch (e: Throwable) {
      if (e !is CancellationException) {
        LOG.error(e)
      }
      throw e
    }
    finally {
      withContext(NonCancellable) {
        runner.writeFullSarifReport()
        runner.writeShortSarifReport()
      }
      LOG.info("sessionId: " + EventLogConfiguration.getInstance().getOrCreate("FUS").sessionId)
    }

    if (!config.skipResultOutput) {
      val openInIdeCloudMetadata = publishResultsToCloudIfNeeded()
      if (openInIdeCloudMetadata != null) {
        val openInIdeMetadata = OpenInIdeMetadata(
          openInIdeCloudMetadata,
          OpenInIdeMetadata.Vcs(runner.sarif.runs?.firstOrNull()?.versionControlProvenance?.firstOrNull()?.repositoryUri?.toString())
        )
        writeOpenInIdeMetadata(openInIdeMetadata, config.outPath)
      }
    }
  }

  private suspend fun publishResultsToCloudIfNeeded(): OpenInIdeMetadata.Cloud? {
    if (projectApi == null) return null

    return coroutineScope {
      val uploadedReportDeferred: Deferred<UploadedReport?> = async {
        when (val result = publishToCloud(projectApi.api, config.outPath)) {
          is PublishResult.Success -> return@async result.uploadedReport
          is PublishResult.Error -> {
            reporter.reportError(result.message)
            reporter.reportError(result.exception)
            null
          }
        }
      }
      val projectPropertiesDeferred: Deferred<QDCloudSchema.Project?> = async {
        projectApi.api.getProjectProperties().asSuccess()
      }
      val uploadedReport = uploadedReportDeferred.await()
      if (uploadedReport == null) {
        projectPropertiesDeferred.cancel()
        return@coroutineScope null
      }
      reporter.reportMessage(1, "The report is uploaded to ${uploadedReport.reportLink}")

      val projectProperties = projectPropertiesDeferred.await()

      return@coroutineScope OpenInIdeMetadata.Cloud(
        url = uploadedReport.reportLink,
        reportId = uploadedReport.reportId,
        projectId = projectProperties?.id,
        projectName = projectProperties?.name,
        host = projectApi.frontendUrl
      )
    }
  }

  private suspend fun writeOpenInIdeMetadata(openInIdeMetadata: OpenInIdeMetadata, path: Path) {
    val json = jacksonObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(openInIdeMetadata)
    runInterruptible(StaticAnalysisDispatchers.IO) {
      try {
        path.createParentDirectories()
        Files.writeString(path.resolve(OPEN_IN_IDE_METADATA_JSON), json)
      }
      catch (e: IOException) {
        thisLogger().error("Failed to write $openInIdeMetadata", e)
      }
    }
  }

  @Suppress("unused")
  private class OpenInIdeMetadata(
    val cloud: Cloud,
    val vcs: Vcs
  ) {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    class Cloud(
      val url: String,
      val reportId: String,
      val projectId: String?,
      val projectName: String?,
      val host: String?,
    )

    @JsonInclude(JsonInclude.Include.NON_NULL)
    class Vcs(
      val origin: String?,
    )
  }

  private fun checkPlugins() {
    val plugins = DisabledPluginsState.getRequiredPlugins()
    val pluginIds = plugins.joinToString("\n") { "- ${it.idString}" }
    if (pluginIds.isNotEmpty() && System.getProperties().getProperty(QODANA_PLUGINS_THIRD_PARTY_ACCEPT).equals("true", ignoreCase = true)) {
      reporter.reportMessage(1, QodanaBundle.message("third.party.plugins.title"))
      reporter.reportMessage(1, pluginIds)
      reporter.reportMessage(1, QodanaBundle.message("third.party.plugins.privacy.note.text"))
    }

    for (pluginId in plugins) {
      if (!PluginManager.isPluginInstalled(pluginId)) {
        throw QodanaException("Required plugin '$pluginId' is not installed")
      }
    }
  }

  companion object {
    private val LOG = logger<QodanaInspectionApplication>()
  }
}
