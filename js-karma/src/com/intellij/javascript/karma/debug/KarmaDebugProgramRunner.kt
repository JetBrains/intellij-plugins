// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.javascript.karma.debug

import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap
import com.intellij.execution.ExecutionException
import com.intellij.execution.ExecutionResult
import com.intellij.execution.configurations.RunProfile
import com.intellij.execution.configurations.RunProfileState
import com.intellij.execution.configurations.RunnerSettings
import com.intellij.execution.executors.DefaultDebugExecutor
import com.intellij.execution.process.NopProcessHandler
import com.intellij.execution.process.OSProcessHandler
import com.intellij.execution.runners.AsyncProgramRunner
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.runners.ExecutionUtil
import com.intellij.execution.ui.RunContentDescriptor
import com.intellij.ide.browsers.BrowserFamily
import com.intellij.ide.browsers.BrowserSettings
import com.intellij.ide.browsers.WebBrowserManager
import com.intellij.javascript.debugger.DebuggableFileFinder
import com.intellij.javascript.debugger.JavaScriptDebugProcess
import com.intellij.javascript.debugger.RemoteDebuggingFileFinder
import com.intellij.javascript.karma.KarmaBundle
import com.intellij.javascript.karma.execution.KarmaConsoleView
import com.intellij.javascript.karma.execution.KarmaRunConfiguration
import com.intellij.javascript.karma.execution.KarmaRunProgramRunner
import com.intellij.javascript.karma.server.KarmaServer
import com.intellij.javascript.karma.util.KarmaUtil
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NlsContexts
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.util.registry.Registry
import com.intellij.openapi.util.text.HtmlChunk
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.newvfs.ManagingFS
import com.intellij.util.ConcurrencyUtil
import com.intellij.util.SingleAlarm
import com.intellij.util.Url
import com.intellij.util.Urls.newFromEncoded
import com.intellij.xdebugger.XDebugProcess
import com.intellij.xdebugger.XDebugProcessStarter
import com.intellij.xdebugger.XDebugSession
import com.intellij.xdebugger.XDebuggerManager
import com.jetbrains.debugger.wip.BrowserChromeDebugProcess
import com.jetbrains.debugger.wip.WipRemoteVmConnection
import org.jetbrains.concurrency.Promise
import org.jetbrains.concurrency.resolvedPromise
import org.jetbrains.debugger.connection.VmConnection
import java.io.IOException
import java.net.InetSocketAddress
import java.nio.charset.StandardCharsets
import javax.swing.event.HyperlinkEvent
import javax.swing.event.HyperlinkListener

class KarmaDebugProgramRunner : AsyncProgramRunner<RunnerSettings>() {
  override fun getRunnerId(): String = "KarmaJavaScriptTestRunnerDebug"

  override fun canRun(executorId: String, profile: RunProfile): Boolean =
    DefaultDebugExecutor.EXECUTOR_ID == executorId && profile is KarmaRunConfiguration

  @Throws(ExecutionException::class)
  override fun execute(environment: ExecutionEnvironment, state: RunProfileState): Promise<RunContentDescriptor?> {
    return KarmaRunProgramRunner.executeAsync(environment, state).thenAsync { executionResult ->
      val consoleView = KarmaConsoleView.get(executionResult, state) ?: return@thenAsync resolvedPromise(
        KarmaUtil.createDefaultDescriptor(executionResult, environment))
      if (executionResult.processHandler is NopProcessHandler) {
        return@thenAsync resolvedPromise(KarmaUtil.createDefaultDescriptor(executionResult, environment).also {
          consoleView.karmaServer.onPortBound { ExecutionUtil.restartIfActive(it) }
        })
      }
      val debuggableWebBrowser = Handler.getChromeInfo(environment.project)
      return@thenAsync debuggableWebBrowser.debugEngine.prepareDebugger(environment.project, debuggableWebBrowser.webBrowser).then {
        Handler.createDescriptor(environment, executionResult, consoleView, debuggableWebBrowser)
      }
    }
  }

  private class ChromeRequiredException(private val project: Project): ExecutionException(message()), HyperlinkListener {
    override fun hyperlinkUpdate(e: HyperlinkEvent) {
      if (e.eventType == HyperlinkEvent.EventType.ACTIVATED) {
        ShowSettingsUtil.getInstance().showSettingsDialog(project, BrowserSettings::class.java)
      }
    }

    companion object {
      fun message(): @NlsContexts.DialogMessage String {
        return KarmaBundle.message("debug.debugging_available_in_chrome_only.dialog.message",
                                   HtmlChunk.link("", BrowserSettings().displayName).toString())
      }
    }
  }

  private object Handler { // not a companion object to load less bytecode simultaneously with KarmaDebugProgramRunner
    @Throws(ExecutionException::class)
    fun getChromeInfo(project: Project): DebuggableWebBrowser {
      val browser = WebBrowserManager.getInstance().getFirstBrowserOrNull(BrowserFamily.CHROME) ?: throw ChromeRequiredException(project)
      return DebuggableWebBrowser.create(browser) ?: throw ExecutionException(
        KarmaBundle.message("debug.cannot_find_chrome.dialog.message"))
    }

    @Throws(ExecutionException::class)
    fun createDescriptor(environment: ExecutionEnvironment,
                         executionResult: ExecutionResult,
                         consoleView: KarmaConsoleView,
                         debuggableWebBrowser: DebuggableWebBrowser): RunContentDescriptor {
      val karmaServer = consoleView.karmaServer
      val url = newFromEncoded(karmaServer.formatUrl("/"))
      val fileFinder = getDebuggableFileFinder(karmaServer)
      val session = XDebuggerManager.getInstance(environment.project).startSession(
        environment,
        object : XDebugProcessStarter() {
          override fun start(session: XDebugSession): XDebugProcess {
            val debugProcess = createDebugProcess(session, karmaServer, fileFinder, executionResult, debuggableWebBrowser, url)
            debugProcess.scriptsCanBeReloaded = true
            debugProcess.addFirstLineBreakpointPattern("\\.browserify$")
            debugProcess.elementsInspectorEnabled = false
            debugProcess.setConsoleMessagesSupportEnabled(false)
            debugProcess.setLayouter(consoleView.createDebugLayouter(debugProcess))
            karmaServer.onBrowsersReady {
              openConnectionIfRemoteDebugging(karmaServer, debugProcess.connection)
              val resumeTestRunning = ConcurrencyUtil.once { resumeTestRunning(executionResult.processHandler as OSProcessHandler) }
              val alarm = SingleAlarm(resumeTestRunning, 5000)
              alarm.request()
              debugProcess.connection.executeOnStart { vm ->
                if (Registry.`is`("js.debugger.break.on.first.statement.karma")) {
                  vm.breakpointManager.setBreakOnFirstStatement()
                }
                alarm.cancelAllRequests()
                resumeTestRunning.run()
              }
            }
            return debugProcess
          }
        }
      )
      return KarmaUtil.withReusePolicy(session.runContentDescriptor, karmaServer)
    }

    private fun createDebugProcess(session: XDebugSession,
                                   karmaServer: KarmaServer,
                                   fileFinder: DebuggableFileFinder,
                                   executionResult: ExecutionResult,
                                   debuggableWebBrowser: DebuggableWebBrowser,
                                   url: Url): JavaScriptDebugProcess<VmConnection<*>> {
      val karmaConfig = karmaServer.karmaConfig
      if (karmaConfig != null && karmaConfig.remoteDebuggingPort > 0) {
        // Passing an url without parameters (e.g. http://localhost:9876) is enough to find a page url with parameters,
        // e.g. http://localhost:9876/?id=98544599. Thanks to `Urls.equals(url, pageUrl, ..., ignoreParameters=true)` in
        // com.jetbrains.debugger.wip.WipRemoteVmConnection.
        //
        // Opening connection is postponed until browsers are ready (WEB-33076).
        return BrowserChromeDebugProcess(session, fileFinder, WipRemoteVmConnection(url, null), executionResult)
      }
      val debugEngine = debuggableWebBrowser.debugEngine
      val browser = debuggableWebBrowser.webBrowser
      // If a capturing page was open, but not connected (e.g. it happens after karma server restart),
      // reload it to capture. Otherwise, (no capturing page was open), reloading shouldn't harm.
      val reloadPage = !karmaServer.areBrowsersReady()
      return debugEngine.createDebugProcess(session, browser, fileFinder, url, executionResult, reloadPage)
    }

    private fun openConnectionIfRemoteDebugging(server: KarmaServer, connection: VmConnection<*>) {
      val config = server.karmaConfig
      if (config != null && config.remoteDebuggingPort > 0 && connection is WipRemoteVmConnection) {
        connection.open(InetSocketAddress(config.hostname, config.remoteDebuggingPort))
      }
    }

    private fun getDebuggableFileFinder(karmaServer: KarmaServer): DebuggableFileFinder {
      val mappings: BiMap<String, VirtualFile> = HashBiMap.create()
      val karmaConfig = karmaServer.karmaConfig
      if (karmaConfig != null) {
        val basePath = LocalFileSystem.getInstance().findFileByPath(karmaConfig.basePath)
        if (basePath != null && basePath.isValid) {
          mappings[karmaServer.formatUrlWithoutUrlRoot("/base")] = basePath
        }
      }
      val roots = ManagingFS.getInstance().localRoots
      if (SystemInfo.isWindows) {
        for (root in roots) {
          val key = karmaServer.formatUrlWithoutUrlRoot("/absolute" + root.name)
          if (mappings.containsKey(key)) {
            logger<KarmaDebugProgramRunner>().warn("Duplicate mapping for Karma debug: $key")
          }
          else {
            mappings[key] = root
          }
        }
      }
      else {
        if (roots.size == 1) {
          mappings[karmaServer.formatUrlWithoutUrlRoot("/absolute")] = roots[0]
        }
      }
      return RemoteDebuggingFileFinder(mappings, null)
    }

    private fun resumeTestRunning(processHandler: OSProcessHandler) {
      val process = processHandler.process
      if (process.isAlive) {
        try {
          val processInput = process.outputStream
          processInput.write("resume-test-running\n".toByteArray(StandardCharsets.UTF_8))
          processInput.flush()
        }
        catch (e: IOException) {
          logger<KarmaDebugProgramRunner>().warn("process.isAlive()=" + process.isAlive, e)
        }
      }
    }
  }
}