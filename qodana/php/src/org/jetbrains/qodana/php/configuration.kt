package org.jetbrains.qodana.php

import com.intellij.diagnostic.ThreadDumper
import com.intellij.execution.process.ProcessAdapter
import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.process.ProcessHandler
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.coroutineToIndicator
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.jetbrains.php.composer.actions.ComposerInstallAction
import com.jetbrains.php.composer.actions.ComposerOptionsManager
import com.jetbrains.php.composer.execution.executable.ExecutableComposerExecution
import com.jetbrains.php.composer.json.ComposerInstallNotifier
import com.jetbrains.php.roots.PhpDetectPsrRootsFinishedService
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withContext
import org.jetbrains.qodana.staticAnalysis.StaticAnalysisDispatchers

private const val TIMEOUT_IN_MINUTES: Long = 10

// we assume that Composer is pre-installed in container and is available by calling `.composer`
private const val DEFAULT_COMPOSER_EXECUTABLE_PATH = "composer"
private val LOG = Logger.getInstance("org.jetbrains.qodana.php.configuration")

suspend fun configurePhpProject(project: Project) {
  waitForPhpDetectPsrRoots(project)
  runComposerInstallIfNeeded(project)
}

private suspend fun waitForPhpDetectPsrRoots(project: Project) {
  try {
    PhpDetectPsrRootsFinishedService.getInstance(project).finishedDeferred.await()
  }
  catch (e: TimeoutCancellationException) {
    val threads = ThreadDumper.dumpThreadsToString()
    throw RuntimeException("Cannot process startup activities in $TIMEOUT_IN_MINUTES minutes. Thread dumps: $threads", e)
  }
}

private suspend fun runComposerInstallIfNeeded(project: Project) {
  val composerConfig = ComposerInstallNotifier.getComposerFileWithoutVendor(project) ?: return
  val commandExecutor = ComposerInstallAction.createExecutor(
    project,
    LogPrintingExecutableComposerExecution(),
    composerConfig,
    ComposerOptionsManager.DEFAULT_COMMAND_LINE_OPTIONS,
    null,
    false
  )
  LOG.info("Running 'composer install...'")
  withContext(StaticAnalysisDispatchers.IO) {
    coroutineToIndicator {
      commandExecutor.doRun(ProgressManager.getGlobalProgressIndicator())
    }
  }
}

private class LogPrintingExecutableComposerExecution : ExecutableComposerExecution(DEFAULT_COMPOSER_EXECUTABLE_PATH) {
  override fun createProcessHandler(project: Project,
                                    workingDir: String?,
                                    command: MutableList<String>,
                                    commandText: String): ProcessHandler {
    val handler = super.createProcessHandler(project, workingDir, command, commandText)
    handler.addProcessListener(object : ProcessAdapter() {
      override fun onTextAvailable(event: ProcessEvent, outputType: Key<*>) {
        LOG.info(event.text)
        print(event.text)
      }
    })
    return handler
  }
}