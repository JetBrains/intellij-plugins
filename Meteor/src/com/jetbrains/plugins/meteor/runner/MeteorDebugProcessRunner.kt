package com.jetbrains.plugins.meteor.runner

import com.intellij.execution.DefaultExecutionResult
import com.intellij.execution.ExecutionException
import com.intellij.execution.configurations.RunProfile
import com.intellij.execution.configurations.RunProfileState
import com.intellij.execution.configurations.RunnerSettings
import com.intellij.execution.executors.DefaultDebugExecutor
import com.intellij.execution.filters.UrlFilter
import com.intellij.execution.process.ProcessAdapter
import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.runners.GenericProgramRunner
import com.intellij.execution.ui.RunContentDescriptor
import com.intellij.javascript.debugger.JavaScriptDebugProcess
import com.intellij.javascript.nodejs.NodeConsoleAdditionalFilter
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.util.Key
import com.intellij.xdebugger.XDebugSession
import com.intellij.xdebugger.XDebuggerManager
import com.jetbrains.plugins.meteor.settings.MeteorSettings
import org.jetbrains.debugger.connection.RemoteVmConnection
import java.net.InetSocketAddress

class MeteorDebugProcessRunner : GenericProgramRunner<RunnerSettings>() {
  override fun getRunnerId(): String {
    return "MeteorDebugRunner"
  }

  override fun canRun(executorId: String, profile: RunProfile): Boolean {
    return DefaultDebugExecutor.EXECUTOR_ID == executorId && profile is MeteorRunConfiguration
  }

  @Throws(ExecutionException::class)
  override fun doExecute(state: RunProfileState, environment: ExecutionEnvironment): RunContentDescriptor {
    FileDocumentManager.getInstance().saveAllDocuments()
    val profileState = state as MeteorRunProfileState
    val configuration = environment.runProfile as MeteorRunConfiguration

    if (isOnceEnabled) {
      val socketAddress = configuration.computeDebugAddress(state)
      val mainProcessHandler = profileState.getProcessHandler(socketAddress.port)
      val session = createSession(environment, socketAddress, DefaultExecutionResult(null, mainProcessHandler))
      return session.runContentDescriptor
    }

    var mainProcessHandler: MeteorMainProcessHandler? = null
    val oldContentDescriptor = environment.contentToReuse
    if (oldContentDescriptor != null && oldContentDescriptor.processHandler != null
        && oldContentDescriptor.processHandler is MeteorDebuggableProcessHandler) {
      val possibleMainProcessHandler = (oldContentDescriptor.processHandler as MeteorDebuggableProcessHandler).mainHandler

      if (isAliveProcessHandler(possibleMainProcessHandler)) {
        mainProcessHandler = possibleMainProcessHandler
      }
    }

    val socketAddress = if (mainProcessHandler == null) configuration.computeDebugAddress(state) else mainProcessHandler.socketAddress

    if (mainProcessHandler == null) {
      mainProcessHandler = profileState.getProcessHandler(socketAddress.port)
      mainProcessHandler.socketAddress = socketAddress
    }


    val debuggableProcessHandler = MeteorDebuggableProcessHandler(mainProcessHandler)
    val executionResult = DefaultExecutionResult(null, debuggableProcessHandler)

    val session = createSession(environment, socketAddress, executionResult)
    val descriptor = session.runContentDescriptor
    val view = session.consoleView

    val workingDirectory = configuration.effectiveWorkingDirectory
    view.addMessageFilter(NodeConsoleAdditionalFilter(environment.project, workingDirectory))
    view.addMessageFilter(UrlFilter())
    view.addMessageFilter(MeteorErrorFilter(environment.project, workingDirectory))

    val process = session.debugProcess as JavaScriptDebugProcess<*>
    debuggableProcessHandler.setVmConnection(process.connection as RemoteVmConnection)
    debuggableProcessHandler.setRunContentDescriptor(descriptor)
    debuggableProcessHandler.createTextMessagesListener()
    if (!mainProcessHandler.isStartNotified) {
      mainProcessHandler.startNotify()
    }

    return descriptor
  }

  companion object {
    private val isOnceEnabled: Boolean
      get() = MeteorSettings.getInstance().isStartOnce

    @Throws(ExecutionException::class)
    private fun createSession(environment: ExecutionEnvironment,
                              socketAddress: InetSocketAddress,
                              executionResult: DefaultExecutionResult): XDebugSession {
      val configuration = environment.runProfile as MeteorRunConfiguration
      val starter = MeteorDebugProcessStarter(configuration.isNode8,
                                              configuration.createFileFinder(environment.project) as MeteorFileFinder,
                                              socketAddress,
                                              executionResult)
      val session = XDebuggerManager.getInstance(environment.project).startSession(environment, starter)
      return session
    }

    fun getListenerForMainProcess(debugProcessHandler: MeteorDebuggableProcessHandler): ProcessAdapter {
      return object : ProcessAdapter() {
        override fun processWillTerminate(event: ProcessEvent, willBeDestroyed: Boolean) {
          if (!debugProcessHandler.isCalledDestroyParent) {
            debugProcessHandler.destroyProcess()
          }
        }

        override fun onTextAvailable(event: ProcessEvent, outputType: Key<*>) {
          debugProcessHandler.notifyTextAvailable(event.text, outputType)
        }
      }
    }

    fun isAliveProcessHandler(processHandler: ProcessHandler): Boolean {
      return !processHandler.isProcessTerminating && !processHandler.isProcessTerminated
    }
  }
}
