package com.jetbrains.cidr.cpp.embedded.platformio

import com.intellij.clion.embedded.debugger.peripheralview.SvdPanel.Companion.registerPeripheralTab
import com.intellij.clion.embedded.execution.custom.McuResetAction.Companion.addResetMcuAction
import com.intellij.execution.CantRunException.CustomProcessedCantRunException
import com.intellij.execution.ExecutionException
import com.intellij.execution.configurations.CommandLineState
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.filters.ConsoleFilterProvider
import com.intellij.execution.filters.Filter
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.ui.ConsoleView
import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.execution.ui.ExecutionConsole
import com.intellij.execution.ui.RunnerLayoutUi
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.components.service
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.ui.content.Content
import com.intellij.xdebugger.XDebugSession
import com.intellij.xdebugger.ui.XDebugTabLayouter
import com.jetbrains.cidr.ArchitectureType
import com.jetbrains.cidr.cpp.embedded.platformio.project.PlatformioCliBuilder
import com.jetbrains.cidr.cpp.embedded.platformio.ui.notifyUploadUnavailable
import com.jetbrains.cidr.cpp.execution.CLionLauncher
import com.jetbrains.cidr.cpp.execution.debugger.backend.CLionGDBDriverConfiguration
import com.jetbrains.cidr.cpp.toolchains.CPPDebugger
import com.jetbrains.cidr.cpp.toolchains.CPPEnvironment
import com.jetbrains.cidr.cpp.toolchains.TrivialNativeToolchain
import com.jetbrains.cidr.execution.CidrPathWithOffsetConsoleFilter
import com.jetbrains.cidr.execution.TrivialRunParameters
import com.jetbrains.cidr.execution.debugger.CidrDebugProcess
import com.jetbrains.cidr.execution.debugger.backend.DebuggerDriver
import com.jetbrains.cidr.execution.debugger.backend.DebuggerDriverConfiguration
import com.jetbrains.cidr.execution.runOnEDT
import org.jetbrains.annotations.SystemIndependent
import java.io.File
import kotlin.io.path.Path

class PlatformioLauncher(
  executionEnvironment: ExecutionEnvironment,
  configuration: PlatformioDebugConfiguration
) : CLionLauncher(executionEnvironment, configuration) {

  override fun emulateTerminal(environment: CPPEnvironment, isDebugMode: Boolean): Boolean = false
  override fun getRunFileAndEnvironment(): Nothing = throw UnsupportedOperationException()

  override fun createCommandLine(
    state: CommandLineState,
    runFile: File,
    environment: CPPEnvironment,
    usePty: Boolean,
    emulateTerminal: Boolean
  ): Nothing = throw UnsupportedOperationException()

  @Throws(ExecutionException::class)
  override fun createProcess(state: CommandLineState): ProcessHandler {
    return runOnEDT {
      val actionManager = ActionManager.getInstance()
      val uploadAction = actionManager.getAction("target-platformio-upload")
      if (uploadAction != null) {
        actionManager.tryToExecute(uploadAction, null, null, null, true)
      }
      else {
        notifyUploadUnavailable(project)
      }
      throw CustomProcessedCantRunException()
    }
  }

  @Throws(ExecutionException::class)
  override fun createDebugProcess(state: CommandLineState, session: XDebugSession): CidrDebugProcess {
    PlatformioUsagesCollector.DEBUG_START_EVENT_ID.log(getProject())

    val toolchain = TrivialNativeToolchain.forDebugger(CPPDebugger.customGdb("pio"))
    val debuggerDriverConfiguration: DebuggerDriverConfiguration =
      object : CLionGDBDriverConfiguration(project, toolchain) {
        @Throws(ExecutionException::class)
        override fun createDriverCommandLine(driver: DebuggerDriver, architectureType: ArchitectureType): GeneralCommandLine {
          return PlatformioCliBuilder(false, project, true, true)
            .withParams("debug", "--interface=gdb", "--interpreter=mi2", "-x", ".pioinit", "--iex", "set mi-async on")
            .withGdbHomeCompatibility()
            .withRedirectErrorStream(true)
            .build()
        }
      }
    val projectPath: @SystemIndependent String? = project.basePath
    val vfs = LocalFileSystem.getInstance()
    if (projectPath == null ||
        vfs.findFileByPath(projectPath)?.findChild(PlatformioFileType.FILE_NAME) == null
    ) {
      throw ExecutionException(ClionEmbeddedPlatformioBundle.message("file.is.not.found", PlatformioFileType.FILE_NAME))
    }
    val commandLine = GeneralCommandLine("").withWorkingDirectory(Path(projectPath))
    val parameters = TrivialRunParameters(debuggerDriverConfiguration, commandLine, ArchitectureType.UNKNOWN)
    val consoleCopyFilter = ConsoleFilterProvider { arrayOf(
      Filter { s, _ ->
        session.getConsoleView().print(s, ConsoleViewContentType.NORMAL_OUTPUT)
        null
      })
    }

    val defaultSvdLocation = project.service<PlatformioService>().svdPath
    return runOnEDT {
        object : CidrDebugProcess(parameters, session, state.consoleBuilder,
                                  consoleCopyFilter) {
          override fun createTabLayouter(): XDebugTabLayouter {
            val gdbDebugProcess: CidrDebugProcess = this
            val innerLayouter = super.createTabLayouter()

            return object : XDebugTabLayouter() {
              override fun registerConsoleContent(ui: RunnerLayoutUi, console: ExecutionConsole): Content {
                return innerLayouter.registerConsoleContent(ui, console)
              }

              override fun registerAdditionalContent(ui: RunnerLayoutUi) {
                innerLayouter.registerAdditionalContent(ui)
                registerPeripheralTab(gdbDebugProcess, ui, defaultSvdLocation)
              }
            }
          }

          override fun createConsole(): ConsoleView = super.createConsole().apply {
            addMessageFilter(CidrPathWithOffsetConsoleFilter(project, null, Path(projectPath)))
          }

          @Throws(ExecutionException::class)
          override fun doLoadTarget(driver: DebuggerDriver): DebuggerDriver.Inferior = with(driver) {
            val tempInferior = loadForRemote("", null, null, mutableListOf())
            return object : DebuggerDriver.Inferior(tempInferior.id) {
              @Throws(ExecutionException::class)
              override fun startImpl(): Long {
                return 0
              }

              @Throws(ExecutionException::class)
              override fun detachImpl() {
                tempInferior.detach()
              }

              @Throws(ExecutionException::class)
              override fun destroyImpl(): Boolean {
                return tempInferior.destroy()
              }
            }
          }
        }
      }
  }

  @Throws(ExecutionException::class)
  override fun collectAdditionalActions(
    state: CommandLineState,
    processHandler: ProcessHandler,
    console: ExecutionConsole,
    actions: MutableList<in AnAction?>
  ) {
    super.collectAdditionalActions(state, processHandler, console, actions)
    addResetMcuAction(actions, processHandler, "pio_reset_halt_target")
  }
}
