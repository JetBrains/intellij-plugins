package com.intellij.javascript.karma.debug

import com.google.common.collect.HashBiMap
import com.intellij.execution.RunProfileStarter
import com.intellij.execution.configurations.RunProfile
import com.intellij.execution.configurations.RunProfileState
import com.intellij.execution.configurations.RunnerSettings
import com.intellij.execution.executors.DefaultDebugExecutor
import com.intellij.execution.runners.AsyncGenericProgramRunner
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.runners.ExecutionUtil
import com.intellij.javascript.debugger.execution.RemoteDebuggingFileFinder
import com.intellij.javascript.debugger.impl.DebuggableFileFinder
import com.intellij.javascript.karma.execution.KarmaConsoleView
import com.intellij.javascript.karma.execution.KarmaRunConfiguration
import com.intellij.javascript.karma.server.KarmaServer
import com.intellij.javascript.karma.util.KarmaUtil
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.newvfs.ManagingFS
import com.intellij.util.Urls
import com.jetbrains.javascript.debugger.LOG
import com.jetbrains.javascript.debugger.execution.runProfileStarter
import com.jetbrains.javascript.debugger.execution.startSession
import org.jetbrains.concurrency.Promise
import org.jetbrains.concurrency.resolvedPromise

private val LOG = Logger.getInstance(KarmaDebugProgramRunner::class.java)

class KarmaDebugProgramRunner : AsyncGenericProgramRunner<RunnerSettings>() {
  override fun getRunnerId() = "KarmaJavaScriptTestRunnerDebug"

  override fun canRun(executorId: String, profile: RunProfile) = DefaultDebugExecutor.EXECUTOR_ID == executorId && profile is KarmaRunConfiguration

  override fun prepare(environment: ExecutionEnvironment, state: RunProfileState): Promise<RunProfileStarter> {
    FileDocumentManager.getInstance().saveAllDocuments()

    val executionResult = state.execute(environment.executor, this) ?: return resolvedPromise(null)
    val consoleView = KarmaConsoleView.get(executionResult, state) ?: return resolvedPromise(KarmaUtil.createDefaultRunProfileStarter(executionResult))
    val karmaServer = consoleView.karmaExecutionSession.karmaServer
    if (karmaServer.areBrowsersReady()) {
      val browserSelector = KarmaDebugBrowserSelector(karmaServer.capturedBrowsers, environment, consoleView)
      val browser = browserSelector.selectDebugEngine() ?: return resolvedPromise(KarmaUtil.createDefaultRunProfileStarter(executionResult))
      return browser.debugEngine.prepareDebugger(environment.project, browser.webBrowser)
        .then {
          runProfileStarter { state, environment ->
            startSession(environment) {
              val debugEngine = browser.debugEngine
              val debugProcess = debugEngine.createDebugProcess(it, browser.webBrowser, getDebuggableFileFinder(karmaServer), Urls.newFromEncoded(karmaServer.formatUrl("/debug.html")), executionResult, true)
              debugProcess.elementsInspectorEnabled = false
              debugProcess.setLayouter(consoleView.createDebugLayouter(debugProcess))
              debugProcess
            }.runContentDescriptor
          }
        }
    }
    else {
      return resolvedPromise(runProfileStarter { state, environment ->
        val descriptor = KarmaUtil.createDefaultDescriptor(executionResult, environment)
        karmaServer.onBrowsersReady { ExecutionUtil.restartIfActive(descriptor) }
        descriptor
      })
    }
  }
}

private fun getDebuggableFileFinder(karmaServer: KarmaServer): DebuggableFileFinder {
  val mappings = HashBiMap.create<String, VirtualFile>()
  val karmaConfig = karmaServer.karmaConfig
  if (karmaConfig != null) {
    val systemDependentBasePath = FileUtil.toSystemDependentName(karmaConfig.basePath)
    val basePath = LocalFileSystem.getInstance().findFileByPath(systemDependentBasePath)
    if (basePath != null && basePath.isValid) {
      val baseUrl = if (karmaConfig.isWebpack) "webpack:///." else karmaServer.formatUrlWithoutUrlRoot("/base")
      mappings.put(baseUrl, basePath)
    }
  }
  if (SystemInfo.isWindows) {
    val roots = ManagingFS.getInstance().localRoots
    for (root in roots) {
      val key = karmaServer.formatUrlWithoutUrlRoot("/absolute" + root.name)
      if (mappings.containsKey(key)) {
        LOG.warn("Duplicate mapping for Karma debug: " + key)
      }
      else {
        mappings.put(key, root)
      }
    }
  }
  else {
    val roots = ManagingFS.getInstance().localRoots
    if (roots.size == 1) {
      mappings.put(karmaServer.formatUrlWithoutUrlRoot("/absolute"), roots[0])
    }
  }
  return RemoteDebuggingFileFinder(mappings, null)
}