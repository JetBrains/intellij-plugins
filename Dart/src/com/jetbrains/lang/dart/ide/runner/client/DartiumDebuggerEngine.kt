package com.jetbrains.lang.dart.ide.runner.client

import com.intellij.CommonBundle
import com.intellij.execution.ExecutionResult
import com.intellij.execution.configurations.RuntimeConfigurationError
import com.intellij.ide.browsers.WebBrowser
import com.intellij.javascript.debugger.DebuggableFileFinder
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.text.StringUtil
import com.intellij.util.PairFunction
import com.intellij.util.Url
import com.intellij.xdebugger.XDebugSession
import com.jetbrains.debugger.wip.ChromeDebugProcess
import com.jetbrains.debugger.wip.ChromeLocalDebuggerEngine
import com.jetbrains.lang.dart.DartBundle
import com.jetbrains.lang.dart.sdk.DartConfigurable

class DartiumDebuggerEngine : ChromeLocalDebuggerEngine() {

  override val browser: WebBrowser?
    get() = DartiumUtil.getDartiumBrowser()

  override fun createDebugProcess(session: XDebugSession,
                                  browser: WebBrowser,
                                  fileFinder: DebuggableFileFinder,
                                  initialUrl: Url?,
                                  executionResult: ExecutionResult?,
                                  usePreliminaryPage: Boolean): ChromeDebugProcess {
    val debugProcess = super.createDebugProcess(session, browser, fileFinder, initialUrl, executionResult, usePreliminaryPage)
    debugProcess.processBreakpointConditionsAtIdeSide = true
    debugProcess.setBreakpointLanguageHint(
      PairFunction { breakpoint, location ->
        val result =
          if (StringUtil.endsWithIgnoreCase(if (breakpoint == null) location!!.url.path else breakpoint.fileUrl, ".dart"))
            "dart"
          else
            null
        if (LOG.isDebugEnabled) {
          LOG.debug(breakpoint.toString() + ", " + location!!.url + " " + result)
        }
        result
      })
    return debugProcess
  }

  @Throws(RuntimeConfigurationError::class)
  override fun checkAvailability(project: Project) {
    if (DartiumUtil.getDartiumBrowser() == null) {
      throw RuntimeConfigurationError(DartBundle.message("dartium.not.configured", CommonBundle.settingsActionPath())
      ) { DartConfigurable.openDartSettings(project) }
    }
  }

  override fun isBrowserSupported(browser: WebBrowser): Boolean {
    return browser == DartiumUtil.getDartiumBrowser()
  }

  companion object {
    private val LOG = Logger.getInstance(DartiumDebuggerEngine::class.java)
  }
}
