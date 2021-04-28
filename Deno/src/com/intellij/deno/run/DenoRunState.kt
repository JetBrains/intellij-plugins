package com.intellij.deno.run

import com.intellij.execution.DefaultExecutionResult
import com.intellij.execution.ExecutionResult
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.filters.TextConsoleBuilderImpl
import com.intellij.execution.impl.ConsoleViewImpl
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.ui.ConsoleView
import com.intellij.execution.util.ProgramParametersConfigurator
import com.intellij.execution.util.ProgramParametersUtil
import com.intellij.javascript.debugger.CommandLineDebugConfigurator
import com.intellij.javascript.debugger.execution.DebuggableProcessState
import com.intellij.javascript.nodejs.NodeCommandLineUtil
import com.intellij.javascript.nodejs.debug.NodeDebugCommandLineConfigurator
import com.intellij.javascript.nodejs.debug.NodeLocalDebuggableRunProfileState
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.terminal.TerminalExecutionConsole
import org.jetbrains.concurrency.Promise

class DenoRunState(environment: ExecutionEnvironment, runConfiguration: DenoRunConfiguration) :
  NodeLocalDebuggableRunProfileState,
  DebuggableProcessState<DenoRunConfiguration>(runConfiguration, environment) {

  override fun execute(configurator: CommandLineDebugConfigurator?): Promise<ExecutionResult> =
    startProcess(NodeCommandLineUtil.createCommandLine(), configurator)
      .then { DefaultExecutionResult(createConsole(it), it) }

  override fun createProcessHandler(commandLine: GeneralCommandLine, configurator: CommandLineDebugConfigurator?): ProcessHandler {
    val handler = NodeCommandLineUtil.createProcessHandler(commandLine, true, configurator)
    NodeCommandLineUtil.transferUseInspectorProtocol(commandLine, handler)
    return handler
  }

  override fun startProcess(commandLine: GeneralCommandLine, configurator: CommandLineDebugConfigurator?)
    : Promise<ProcessHandler> {
    val extraArgs = ProgramParametersConfigurator.expandMacrosAndParseParameters(configuration.programParameters)

    commandLine.addParameters(extraArgs)

    var hasInspect = false
    for (extraArg in extraArgs) {
      if (extraArg.contains("--inspect-brk") || extraArg.contains("--inspect")) hasInspect = true
    }
    NodeCommandLineUtil.useInspectorProtocol(commandLine, true)

    if (!hasInspect && configurator is NodeDebugCommandLineConfigurator) {
      val debugPort = configurator.debugPort
      commandLine.addParameter("--inspect-brk=127.0.0.1:$debugPort")
    }
    NodeCommandLineUtil.configureUsefulEnvironment(commandLine)

    val appFilePath = ProgramParametersUtil.expandPathAndMacros(inputPath,
                                                                ProgramParametersUtil.getModule(configuration),
                                                                environment.project)

    if (appFilePath.isNotEmpty()) {
      commandLine.addParameters(appFilePath)
    }

    return super.startProcess(commandLine, configurator)
  }

  private fun createConsole(processHandler: ProcessHandler): ConsoleView {
    val consoleBuilder = object : TextConsoleBuilderImpl(environment.project) {
      override fun createConsole(): ConsoleView {
        if (NodeCommandLineUtil.shouldUseTerminalConsole(processHandler)) {
          return TerminalExecutionConsole(project, processHandler)
        }
        return object : ConsoleViewImpl(environment.project, scope, isViewer, true) {
          override fun getData(dataId: String): Any? {
            return super.getData(dataId) ?: if (LangDataKeys.RUN_PROFILE.`is`(dataId)) environment.runProfile else null
          }
        }
      }
    }
    addConsoleFilters(consoleBuilder)
    val console = consoleBuilder.console
    console.attachToProcess(processHandler)
    return console
  }

}