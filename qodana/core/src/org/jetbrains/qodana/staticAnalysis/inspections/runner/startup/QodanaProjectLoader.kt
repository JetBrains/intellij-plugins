package org.jetbrains.qodana.staticAnalysis.inspections.runner.startup

import com.intellij.codeInspection.InspectionsBundle
import com.intellij.conversion.ConversionListener
import com.intellij.conversion.ConversionService
import com.intellij.diagnostic.ThreadDumper
import com.intellij.ide.CommandLineInspectionProgressReporter
import com.intellij.ide.CommandLineInspectionProjectAsyncConfigurator
import com.intellij.ide.CommandLineInspectionProjectConfigurator
import com.intellij.ide.impl.PatchProjectUtil
import com.intellij.ide.impl.ProjectUtil
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.application.WriteIntentReadAction
import com.intellij.openapi.application.writeAction
import com.intellij.openapi.components.serviceAsync
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.blockingContext
import com.intellij.openapi.progress.util.ProgressIndicatorBase
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.project.configuration.ConfigurationResult
import com.intellij.openapi.project.configuration.HeadlessLogging
import com.intellij.openapi.project.configuration.awaitCompleteProjectConfiguration
import com.intellij.openapi.startup.StartupManager
import com.intellij.openapi.util.Predicates
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.util.io.findOrCreateFile
import com.intellij.openapi.util.registry.Registry
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.platform.backend.observation.Observation
import kotlinx.coroutines.*
import org.jetbrains.qodana.runActivityWithTiming
import org.jetbrains.qodana.staticAnalysis.StaticAnalysisDispatchers
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaConfig
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaException
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaMessageReporter
import org.jetbrains.qodana.staticAnalysis.stat.InspectionEventsCollector.QodanaActivityKind
import org.jetbrains.qodana.staticAnalysis.workflow.QodanaWorkflowExtension
import java.nio.file.Path
import java.util.function.Predicate
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.io.path.writeText
import kotlin.time.Duration.Companion.minutes

private const val LOG_CONFIGURATION_ACTIVITIES_PERIOD_MINUTES = "qodana.log.configuration.period.minutes"
private const val REGISTRY_STARTUP_TIMEOUT_MINUTES = "batch.inspections.startup.activities.timeout"

class QodanaProjectLoader(private val reporter: QodanaMessageReporter) {
  suspend fun openProject(config: QodanaConfig): Project {
    QodanaWorkflowExtension.callBeforeProjectOpened(config)

    return runActivityWithTiming(QodanaActivityKind.PROJECT_OPENING) {
      doOpen(config)
    }
  }

  private suspend fun doOpen(config: QodanaConfig): Project {
    val projectPath = config.projectPath
    val vfsProject = withContext(StaticAnalysisDispatchers.IO) {
      LocalFileSystem.getInstance().refreshAndFindFileByPath(
        FileUtil.toSystemIndependentName(projectPath.toString())
      )
    }
    if (vfsProject == null) {
      throw QodanaException(InspectionsBundle.message("inspection.application.file.cannot.be.found", projectPath))
    }
    reporter.reportMessageNoLineBreak(1, InspectionsBundle.message("inspection.application.opening.project"))

    tryConvertProject(projectPath)

    val project = ProjectUtil.openOrImportAsync(projectPath) ?: throw QodanaException(
      InspectionsBundle.message("inspection.application.unable.open.project")
    )

    awaitStartupActivities(project)
    writeAction { VirtualFileManager.getInstance().refreshWithoutFileWatcher(false) }

    reporter.reportMessage(1, InspectionsBundle.message("inspection.done"))

    return project
  }

  suspend fun configureProjectWithConfigurators(config: QodanaConfig, project: Project) {
    runConfigurators(config, project)
    configureProject(config, project)
  }

  suspend fun configureProject(config: QodanaConfig, project: Project) {
    runActivityWithTiming(QodanaActivityKind.PROJECT_CONFIGURATION) {
      doConfigure(project)
    }
    QodanaWorkflowExtension.callAfterConfiguration(config, project)
  }

  private suspend fun runConfigurators(config: QodanaConfig, project: Project) {
    blockOn(StaticAnalysisDispatchers.IO) {
      runApplicableConfigurators(config.projectPath) { ctx -> preConfigureProject(project, ctx) }
    }

    runApplicableConfigurators(config.projectPath) { ctx ->
      if (this is CommandLineInspectionProjectAsyncConfigurator) {
        configureProjectAsync(project, ctx)
      }
      else {
        // In tests, we're on EDT with write-intent, in prod we're on Dispatchers.Default without any lock
        // The go configurator wants to run blocking code via invokeAndWait.
        // In tests, we have to call blockingContext in the current (=EDT) thread else we deadlock,
        // but in prod we must not block the current thread
        val dispatcher = if (ApplicationManager.getApplication().isUnitTestMode) EmptyCoroutineContext else StaticAnalysisDispatchers.IO
        blockOn(dispatcher) { configureProject(project, ctx) }
      }
    }
  }


  private suspend fun doConfigure(project: Project) {
    coroutineScope {
      val loggingJob = installLogger()
      val result = project.awaitCompleteProjectConfiguration {
        reporter.reportMessage(1, it)
      }
      dumpThreadsAfterConfiguration()
      loggingJob.cancel()
      if (result is ConfigurationResult.Failure) {
        throw QodanaException(result.message)
      }
    }

    if (ApplicationManager.getApplication().isUnitTestMode) { // should throw away
      blockOn(EmptyCoroutineContext) { PatchProjectUtil.patchProject(project) }
    } else {
      blockOn(StaticAnalysisDispatchers.UI) { PatchProjectUtil.patchProject(project) }
      waitForInvokeLaterActivities()
    }
  }

  private fun CoroutineScope.installLogger(): Job {
    return launch {
      launch {
        HeadlessLogging.loggingFlow().collect { (severity, message) ->
          when (severity) {
            HeadlessLogging.SeverityKind.Info -> reporter.reportMessage(2, message.representation())
            HeadlessLogging.SeverityKind.Warning -> reporter.reportMessage(1, message.representation())
            HeadlessLogging.SeverityKind.Fatal -> when (message) {
              is HeadlessLogging.Message.Exception -> reporter.reportError(message.exception)
              is HeadlessLogging.Message.Plain -> reporter.reportError(message.message)
            }
          }
        }
      }
      launch {
        while (true) {
          val logPeriodMinutes = System.getProperty(LOG_CONFIGURATION_ACTIVITIES_PERIOD_MINUTES, "10").toInt()
          delay(logPeriodMinutes.minutes)
          logger<QodanaProjectLoader>().info(
            buildString {
              appendLine("Awaited configuration activities:")
              appendLine(Observation.dumpAwaitedActivitiesToString())
            }
          )
        }
      }
    }
  }

  suspend fun closeProject(config: QodanaConfig, project: Project) {
    QodanaWorkflowExtension.callBeforeProjectClose(project)
    val service = serviceAsync<ProjectManager>()
    blockOn(StaticAnalysisDispatchers.UI) {
      WriteIntentReadAction.run {
        service.closeAndDispose(project)
      }
    }
    QodanaWorkflowExtension.callAfterProjectClosed(config)
  }

  fun dumpThreadsAfterConfiguration() {
    val dump = ThreadDumper.getThreadDumpInfo(ThreadDumper.getThreadInfos(), false)
    Path.of(PathManager.getLogPath(), "qodana").findOrCreateFile("thread-dump-after-project-configuration.txt").apply {
      writeText(dump.rawDump)
    }
  }

  private suspend fun tryConvertProject(projectPath: Path) {
    val listener = object : ConversionListener {
      override fun successfullyConverted(backupDir: Path) =
        reporter.reportMessage(1, InspectionsBundle.message(
          "inspection.application.project.was.successfully.converted.old.project.files.were.saved.to.0",
          backupDir.toString()))

      override fun cannotWriteToFiles(readonlyFiles: List<Path>) = throw QodanaException(
        InspectionsBundle.message("inspection.application.cannot.convert.the.project.the.following.files.are.read.only.0",
                                  readonlyFiles.joinToString(separator = ";"))
      )

      override fun conversionNeeded() =
        reporter.reportMessage(1, InspectionsBundle.message("inspection.application.project.has.older.format.and.will.be.converted"))

      override fun error(message: String) = throw QodanaException(
        InspectionsBundle.message("inspection.application.cannot.convert.project.0", message)
      )
    }

    val service = serviceAsync<ConversionService>()
    if (service.convertSilently(projectPath, listener).openingIsCanceled()) {
      // other error cases are reported synchronously via conversion listener
      throw QodanaException("Project conversion was cancelled")
    }
  }

  private inline fun runApplicableConfigurators(
    projectPath: Path,
    f: CommandLineInspectionProjectConfigurator.(CommandLineInspectionProjectConfigurator.ConfiguratorContext) -> Unit
  ) {
    val ctx = object : CommandLineInspectionProjectConfigurator.ConfiguratorContext {
      override fun getLogger(): CommandLineInspectionProgressReporter = reporter
      override fun getProgressIndicator(): ProgressIndicator = ProgressIndicatorBase()
      override fun getProjectPath(): Path = projectPath
      override fun getFilesFilter(): Predicate<Path> = Predicates.alwaysTrue()
      override fun getVirtualFilesFilter(): Predicate<VirtualFile> = Predicates.alwaysTrue()
    }

    CommandLineInspectionProjectConfigurator.EP_NAME
      .extensionList
      .filter { it.isApplicable(ctx) }
      .forEach { it.f(ctx) }
  }

  private suspend fun awaitStartupActivities(project: Project) {
    reporter.reportMessage(3, "Waiting for startup activities")
    // 180 minutes seem way too long, but is what the old code does
    val timeout = Registry.intValue(REGISTRY_STARTUP_TIMEOUT_MINUTES, 180).minutes

    val t = withTimeoutOrNull(timeout) {
      project.serviceAsync<StartupManager>().allActivitiesPassedFuture.join()
      waitForInvokeLaterActivities()
      reporter.reportMessage(3, "Startup activities finished")
    }
    if (t == null) {
      error("Cannot process startup activities in $timeout. " +
            "You can try to increase $REGISTRY_STARTUP_TIMEOUT_MINUTES registry value. " +
            "Thread Dump:\n${ThreadDumper.dumpThreadsToString()}")
    }
  }

  private suspend fun waitForInvokeLaterActivities(repetitions: Int = 3) = repeat(repetitions) {
    withContext(StaticAnalysisDispatchers.UI) { yield() }
  }

  private suspend fun <T> blockOn(context: CoroutineContext, action: () -> T) =
    withContext(context) { blockingContext(action) }
}
