package com.jetbrains.plugins.meteor.runner

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.configurations.ParametersList
import com.intellij.execution.filters.TextConsoleBuilder
import com.intellij.execution.filters.UrlFilter
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.ide.browsers.BrowserStarter
import com.intellij.javascript.debugger.CommandLineDebugConfigurator
import com.intellij.javascript.debugger.execution.DebuggableProcessState
import com.intellij.javascript.nodejs.NodeCommandLineUtil
import com.intellij.javascript.nodejs.NodeConsoleAdditionalFilter
import com.jetbrains.plugins.meteor.settings.MeteorSettings

internal class MeteorRunProfileState(configuration: MeteorRunConfiguration, environment: ExecutionEnvironment) : DebuggableProcessState<MeteorRunConfiguration>(configuration, environment) {
  override fun configureCommandLine(commandLine: GeneralCommandLine, debugPort: Int) {
    super.configureCommandLine(commandLine, debugPort)
    val settings = MeteorSettings.getInstance()!!
    if (settings.isStartOnce) {
      commandLine.addParameter("--once")
    }

    val parameters = configuration.programParameters
    if (!parameters.isNullOrEmpty()) {
      commandLine.addParameters(*ParametersList.parse(parameters))
    }
    if (debugPort != -1) {
      configureDebugArg(commandLine, debugPort)
    }
  }

  private fun configureDebugArg(commandLine: GeneralCommandLine, debugPort: Int) {
    val commandLineParameters = commandLine.parametersList.parameters

    val indexOfInspect = commandLineParameters.indexOf(NodeCommandLineUtil.INSPECT)
    if (indexOfInspect >= 0) {
      commandLine.parametersList[indexOfInspect] = NodeCommandLineUtil.INSPECT_EQ + debugPort
      return
    }

    val indexOfInspectBrk = commandLineParameters.indexOf(NodeCommandLineUtil.INSPECT_BRK)
    if (indexOfInspectBrk >= 0) {
      commandLineParameters[indexOfInspectBrk] = NodeCommandLineUtil.INSPECT_BRK_EQ + debugPort
      return
    }

    val indexOfDebug = commandLineParameters.indexOf(NodeCommandLineUtil.DEBUG)
    if (indexOfDebug >= 0) {
      commandLineParameters.removeAt(indexOfDebug)
      commandLine.environment.put("NODE_OPTIONS", NodeCommandLineUtil.DEBUG_EQ + debugPort)
      return
    }

    val indexOfDebugBrk = commandLineParameters.indexOf(NodeCommandLineUtil.DEBUG_BRK)
    if (indexOfDebugBrk >= 0) {
      commandLineParameters.removeAt(indexOfDebugBrk)
      commandLine.environment.put("NODE_OPTIONS", NodeCommandLineUtil.DEBUG_BRK_EQ + debugPort)
      return
    }

    if (configuration.isNode8) {
      commandLine.addParameter(NodeCommandLineUtil.INSPECT_BRK_EQ + debugPort)
    }
    else {
      commandLine.environment.put("NODE_OPTIONS", NodeCommandLineUtil.DEBUG_BRK_EQ + debugPort)
    }
  }

  override fun createProcessHandler(commandLine: GeneralCommandLine, configurator: CommandLineDebugConfigurator?): MeteorMainProcessHandler {
    val handler = MeteorMainProcessHandler(commandLine)
    BrowserStarter(configuration, configuration.startBrowserSettings, handler).start()
    return handler
  }

  override fun addConsoleFilters(builder: TextConsoleBuilder) {
    val workingDirectory = configuration.effectiveWorkingDirectory
    builder.addFilter(NodeConsoleAdditionalFilter(environment.project, workingDirectory))
    builder.addFilter(UrlFilter())
    builder.addFilter(MeteorErrorFilter(environment.project, workingDirectory))
  }

  fun getProcessHandler(port: Int) = startProcess(GeneralCommandLine(), port).blockingGet(0) as MeteorMainProcessHandler
}
