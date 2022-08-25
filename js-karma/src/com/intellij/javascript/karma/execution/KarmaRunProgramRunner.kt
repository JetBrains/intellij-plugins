package com.intellij.javascript.karma.execution

import com.intellij.execution.ExecutionException
import com.intellij.execution.configurations.RunProfile
import com.intellij.execution.configurations.RunProfileState
import com.intellij.execution.configurations.RunnerSettings
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.execution.process.NopProcessHandler
import com.intellij.execution.runners.*
import com.intellij.execution.ui.RunContentDescriptor
import com.intellij.javascript.karma.util.KarmaUtil
import com.intellij.openapi.fileEditor.FileDocumentManager

class KarmaRunProgramRunner : GenericProgramRunner<RunnerSettings>() {
  override fun getRunnerId(): String {
    return "KarmaJavaScriptTestRunnerRun"
  }

  override fun canRun(executorId: String, profile: RunProfile): Boolean {
    return DefaultRunExecutor.EXECUTOR_ID == executorId && profile is KarmaRunConfiguration
  }

  @Throws(ExecutionException::class)
  override fun doExecute(state: RunProfileState, environment: ExecutionEnvironment): RunContentDescriptor? {
    FileDocumentManager.getInstance().saveAllDocuments()
    val executionResult = state.execute(environment.executor, this) ?: return null
    val consoleView = KarmaConsoleView.get(executionResult, state)
    val descriptor = KarmaUtil.createDefaultDescriptor(executionResult, environment)
    if (consoleView == null) {
      return descriptor
    }
    if (executionResult.processHandler is NopProcessHandler) {
      consoleView.karmaServer.onBrowsersReady { ExecutionUtil.restartIfActive(descriptor) }
    }
    else {
      RerunTestsNotification.showRerunNotification(environment.contentToReuse, executionResult.executionConsole)
    }
    RerunTestsAction.register(descriptor)
    return descriptor
  }
}