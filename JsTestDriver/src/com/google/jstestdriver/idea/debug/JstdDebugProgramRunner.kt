package com.google.jstestdriver.idea.debug

import com.google.jstestdriver.idea.execution.JstdRunConfiguration
import com.google.jstestdriver.idea.execution.JstdRunProfileState
import com.google.jstestdriver.idea.execution.JstdRunProgramRunner
import com.google.jstestdriver.idea.execution.settings.JstdRunSettings
import com.google.jstestdriver.idea.rt.TestRunner
import com.google.jstestdriver.idea.server.JstdServer
import com.google.jstestdriver.idea.server.JstdServerRegistry
import com.google.jstestdriver.idea.server.ui.JstdToolWindowManager
import com.intellij.execution.ExecutionException
import com.intellij.execution.RunProfileStarter
import com.intellij.execution.RunnerRegistry
import com.intellij.execution.configurations.RunProfile
import com.intellij.execution.configurations.RunProfileState
import com.intellij.execution.configurations.RunnerSettings
import com.intellij.execution.executors.DefaultDebugExecutor
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.runners.AsyncGenericProgramRunner
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.ui.RunContentDescriptor
import com.intellij.ide.browsers.BrowserFamily
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.util.Url
import com.intellij.util.Urls
import com.intellij.xdebugger.XDebuggerManager
import com.jetbrains.javascript.debugger.JavaScriptDebugProcess
import com.jetbrains.javascript.debugger.execution.runProfileStarter
import com.jetbrains.javascript.debugger.execution.xDebugProcessStarter
import org.jetbrains.concurrency.Promise
import org.jetbrains.concurrency.resolvedPromise
import java.io.File
import java.io.PrintWriter

private val DEBUG_RUNNER_ID = JstdDebugProgramRunner::class.java.simpleName
private var IS_AVAILABLE_CACHE: Boolean? = null

class JstdDebugProgramRunner : AsyncGenericProgramRunner<RunnerSettings>() {
  companion object {
    val isAvailable: Boolean
      get() {
        var isAvailable = IS_AVAILABLE_CACHE
        if (isAvailable != null) {
          return isAvailable
        }
        val registry = RunnerRegistry.getInstance()
        isAvailable = registry.findRunnerById(DEBUG_RUNNER_ID) != null
        IS_AVAILABLE_CACHE = isAvailable
        return isAvailable
      }

    private fun prepareWithServer(project: Project, server: JstdServer, runSettings: JstdRunSettings): Promise<RunProfileStarter?> {
      if (server.isReadyForRunningTests) {
        val debugBrowserInfo = JstdDebugBrowserInfo.build(server, runSettings)
        if (debugBrowserInfo == null) {
          return resolvedPromise(runProfileStarter { state, environment -> throw ExecutionException("Please capture Chrome or Firefox and try again") })
        }
        else {
          return debugBrowserInfo.debugEngine.prepareDebugger(project, debugBrowserInfo.browser)
            .then { MyDebugStarter(server, debugBrowserInfo) }
        }
      }
      else {
        return resolvedPromise(JstdRunProgramRunner.JstdRunStarter(server, true))
      }
    }

    private fun resumeJstdClientRunning(processHandler: ProcessHandler) {
      // process's input stream will be closed on process termination
      @SuppressWarnings("IOResourceOpenedButNotSafelyClosed", "ConstantConditions")
      val writer = PrintWriter(processHandler.processInput)
      writer.println(TestRunner.DEBUG_SESSION_STARTED)
      writer.flush()
    }
  }

  override fun getRunnerId() = DEBUG_RUNNER_ID

  override fun canRun(executorId: String, profile: RunProfile) = DefaultDebugExecutor.EXECUTOR_ID == executorId && profile is JstdRunConfiguration

  override fun prepare(environment: ExecutionEnvironment, state: RunProfileState): Promise<RunProfileStarter?> {
    val jstdState = JstdRunProfileState.cast(state)
    val runSettings = jstdState.runSettings
    if (runSettings.isExternalServerType) {
      throw ExecutionException("Local JsTestDriver server running in IDE required for tests debugging")
    }

    val jstdToolWindowManager = JstdToolWindowManager.getInstance(environment.project)
    jstdToolWindowManager.setAvailable(true)
    val server = JstdServerRegistry.getInstance().server
    if (server != null && !server.isStopped) {
      return prepareWithServer(environment.project, server, runSettings)
    }

    return jstdToolWindowManager.restartServer()
      .thenAsync { server -> if (server == null) resolvedPromise<RunProfileStarter?>(null) else prepareWithServer(environment.project, server, runSettings) }
  }

  private class MyDebugStarter(private val myServer: JstdServer, private val myDebugBrowserInfo: JstdDebugBrowserInfo) : RunProfileStarter() {
    override fun execute(state: RunProfileState, environment: ExecutionEnvironment): RunContentDescriptor? {
      val browser = myDebugBrowserInfo.browser
      val url: Url?
      if (browser.family == BrowserFamily.CHROME) {
        url = Urls.newHttpUrl("127.0.0.1:" + myDebugBrowserInfo.serverSettings.port, myDebugBrowserInfo.path)
      }
      else {
        url = null
      }
      FileDocumentManager.getInstance().saveAllDocuments()
      val jstdState = JstdRunProfileState.cast(state)
      val executionResult = jstdState.executeWithServer(myServer)
      myDebugBrowserInfo.fixIfChrome(executionResult.processHandler)

      val configFile = File(jstdState.runSettings.configFile)
      val fileFinder = JstdDebuggingFileFinderProvider(configFile, myServer).provideFileFinder()
      val session = XDebuggerManager.getInstance(environment.project).startSession(environment, xDebugProcessStarter {
        val debugEngine = myDebugBrowserInfo.debugEngine
        val process = debugEngine.createDebugProcess(it, browser, fileFinder, url, executionResult, false)
        process.elementsInspectorEnabled = false
        process
      })

      // must be here, after all breakpoints were queued
      (session.debugProcess as JavaScriptDebugProcess<*>).connection.executeOnStart(Runnable {
        val runnable = Runnable { resumeJstdClientRunning(executionResult.processHandler) }

        if (ApplicationManager.getApplication().isReadAccessAllowed) {
          ApplicationManager.getApplication().executeOnPooledThread(runnable)
        }
        else {
          runnable.run()
        }
      })
      return session.runContentDescriptor
    }
  }
}
