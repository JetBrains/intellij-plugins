package com.intellij.javascript.karma.execution

import com.intellij.execution.ExecutionResult
import com.intellij.execution.configurations.RunProfile
import com.intellij.execution.configurations.RunProfileState
import com.intellij.execution.configurations.RunnerSettings
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.execution.process.NopProcessHandler
import com.intellij.execution.runners.*
import com.intellij.execution.ui.RunContentDescriptor
import com.intellij.javascript.karma.util.KarmaUtil
import com.intellij.javascript.nodejs.debug.NodeDebuggableRunProfileState
import com.intellij.javascript.nodejs.debug.NodeDebuggableRunProfileState.Companion.execute
import com.intellij.openapi.fileEditor.FileDocumentManager
import org.jetbrains.concurrency.Promise
import org.jetbrains.concurrency.resolvedPromise

class KarmaRunProgramRunner : AsyncProgramRunner<RunnerSettings>() {

  override fun getRunnerId(): String = "KarmaJavaScriptTestRunnerRun"

  override fun canRun(executorId: String, profile: RunProfile): Boolean =
    DefaultRunExecutor.EXECUTOR_ID == executorId && profile is KarmaRunConfiguration

  override fun execute(environment: ExecutionEnvironment, state: RunProfileState): Promise<RunContentDescriptor?> {
    return executeAsync(environment, state).then { executionResult ->
      val consoleView = KarmaConsoleView.get(executionResult, state)
      val descriptor = KarmaUtil.createDefaultDescriptor(executionResult, environment)
      if (consoleView == null) {
        return@then descriptor
      }
      if (executionResult.processHandler is NopProcessHandler) {
        consoleView.karmaServer.onBrowsersReady { ExecutionUtil.restartIfActive(descriptor) }
      }
      else {
        RerunTestsNotification.showRerunNotification(environment.contentToReuse, executionResult.executionConsole)
        RerunTestsAction.register(descriptor)
      }
      descriptor
    }
  }

  companion object {
    fun executeAsync(environment: ExecutionEnvironment, state: RunProfileState): Promise<ExecutionResult> {
      FileDocumentManager.getInstance().saveAllDocuments()
      return if (state is NodeDebuggableRunProfileState) {
        state.execute(environment.project, environment.runProfile, null)
      }
      else {
        resolvedPromise(state.execute(environment.executor, environment.runner)!!)
      }
    }
  }
}