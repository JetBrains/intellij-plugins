package com.jetbrains.lang.makefile

import com.intellij.execution.ExecutionException
import com.intellij.execution.Executor
import com.intellij.execution.configuration.EnvironmentVariablesData
import com.intellij.execution.configurations.*
import com.intellij.execution.configurations.GeneralCommandLine.ParentEnvironmentType
import com.intellij.execution.process.ColoredProcessHandler
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessTerminatedListener
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.wsl.WSLCommandLineOptions
import com.intellij.execution.wsl.WslPath
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PathMacroManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NlsSafe
import com.intellij.openapi.vfs.encoding.EncodingManager
import com.intellij.util.EnvironmentUtil
import com.intellij.util.concurrency.annotations.RequiresEdt
import org.jdom.Element
import org.jetbrains.annotations.NonNls
import org.jetbrains.plugins.terminal.LocalTerminalCustomizer
import java.nio.file.Paths

class MakefileRunConfiguration(project: Project, factory: MakefileRunConfigurationFactory, name: String) : LocatableConfigurationBase<RunProfileState>(project, factory, name) {
  var filename: @NlsSafe String = ""
  var target: @NlsSafe String = ""
  var workingDirectory: @NlsSafe String = ""
  var environmentVariables: EnvironmentVariablesData = EnvironmentVariablesData.DEFAULT
  var arguments: @NlsSafe String = ""

  private companion object {
    private val LOGGER = logger<MakefileRunConfiguration>()

    const val MAKEFILE = "makefile"
    const val FILENAME = "filename"
    const val TARGET = "target"
    const val WORKING_DIRECTORY = "workingDirectory"
    const val ARGUMENTS = "arguments"

    private const val SWITCH_FILE = "-f"

    private const val SWITCH_DIRECTORY = "-C"
  }

  override fun checkConfiguration() {
  }

  override fun getConfigurationEditor() = MakefileRunConfigurationEditor(project)

  override fun writeExternal(element: Element) {
    super.writeExternal(element)
    val child = element.getOrCreateChild(MAKEFILE)
    child.setAttribute(FILENAME, filename)
    child.setAttribute(TARGET, target)
    child.setAttribute(WORKING_DIRECTORY, workingDirectory)
    child.setAttribute(ARGUMENTS, arguments)
    environmentVariables.writeExternal(child)
  }

  override fun readExternal(element: Element) {
    super.readExternal(element)
    val child = element.getChild(MAKEFILE)
    if (child != null) {
      filename = child.getAttributeValue(FILENAME) ?: ""
      target = child.getAttributeValue(TARGET) ?: ""
      workingDirectory = child.getAttributeValue(WORKING_DIRECTORY) ?: ""
      arguments = child.getAttributeValue(ARGUMENTS) ?: ""
      environmentVariables = EnvironmentVariablesData.readExternal(child)
    }
  }

  @RequiresEdt
  override fun getState(executor: Executor, executionEnvironment: ExecutionEnvironment): RunProfileState {
    val makeSettings = project.getService(MakefileProjectSettings::class.java).settings
    val makePath = makeSettings?.path ?: DEFAULT_MAKE_PATH
    val useCygwin = makeSettings?.useCygwin ?: false

    return object : CommandLineState(executionEnvironment) {
      override fun startProcess(): ProcessHandler {
        val cmd = newCommandLine(makePath, useCygwin)

        val processHandler = ColoredProcessHandler(cmd)
        processHandler.setShouldKillProcessSoftly(true)
        ProcessTerminatedListener.attach(processHandler)
        return processHandler
      }
    }
  }

  /**
   * WSL-specific corner cases (Windows 10+):
   *
   * - Non-WSL paths (Cygwin, MinGW): the remote _Make_ path is `null`.
   * - Missing WSL distributions (`\\wsl$\Missing`): the remote _Make_ path is non-`null`
   *   (an exception will be thrown later).
   * - WSL not installed (no `wsl.exe` in `%PATH%`): the remote _Make_ path is non-`null`
   *   (an exception will be thrown later).
   *
   * @throws ExecutionException if WSL is requested but is not installed, or the
   *   requested Linux distribution is missing.
   */
  @RequiresEdt
  @Throws(ExecutionException::class)
  private fun newCommandLine(localMakePath: String, useCygwin: Boolean): GeneralCommandLine =
    when (val remoteMakePath = WslPath.parseWindowsUncPath(windowsUncPath = localMakePath)) {
      null -> newCommandLineLocal(localMakePath, useCygwin)
      else -> newCommandLineWsl(remoteMakePath)
    }

  @RequiresEdt
  private fun newCommandLineLocal(localMakePath: @NlsSafe String, useCygwin: Boolean): GeneralCommandLine {
    val macroManager = PathMacroManager.getInstance(project)

    val localMakefile = macroManager.expandPath(filename)

    val localWorkDirectory = when {
      workingDirectory.isNotEmpty() -> macroManager.expandPath(workingDirectory)
      else -> Paths.get(localMakefile).parent?.toString()
    }

    val makeSwitches = makeSwitches(localMakefile, localWorkDirectory)

    val environment = environment()
    var command = arrayOf(localMakePath) + makeSwitches.array
    command = customizeCommandAndEnvironment(command, environment)

    return command.toCommandLine(::PtyCommandLine,
                                 localWorkDirectory,
                                 environment,
                                 useCygwin)
  }

  /**
   * @throws ExecutionException if WSL is not installed, or the requested Linux
   *   distribution is missing.
   */
  @RequiresEdt
  @Throws(ExecutionException::class)
  private fun newCommandLineWsl(remoteMakePath: WslPath): GeneralCommandLine {
    val distribution = remoteMakePath.distribution

    /*-
     * It is possible to set a non-root default user on a per-distribution
     * basis by creating a /etc/wsl.conf file with the following content:
     *
     * [user]
     * default=alice
     *
     * and restarting the guest VM. For details, see
     * <https://docs.microsoft.com/en-us/windows/wsl/wsl-config#user>.
     *
     * To facilitate debugging file access problems (when a regular user tries
     * to modify a file created by root), we should at least log the user's home,
     * or, better, display it in the event log (once per WSL distribution).
     */
    LOGGER.debugInBackground {
      @Suppress("LongLine")
      "The current user's home within the ${distribution.msId} WSL distribution is ${distribution.userHome}. Edit /etc/wsl.conf to change the default user."
    }

    val macroManager = PathMacroManager.getInstance(project)

    val localMakefile = macroManager.expandPath(filename)
    val remoteMakefile = distribution.getWslPath(localMakefile)

    val localWorkDirectory = when {
      workingDirectory.isNotEmpty() -> macroManager.expandPath(workingDirectory)
      else -> Paths.get(localMakefile).parent?.toString()
    }
    /*
     * Non-null as long as the local one is non-null, even if incorrect
     * distribution is passed or `wsl.exe` goes missing.
     */
    val remoteWorkDirectory = localWorkDirectory?.let(distribution::getWslPath)

    val makeSwitches = makeSwitches(remoteMakefile, remoteWorkDirectory)

    val environment = environment()
    var command = arrayOf(remoteMakePath.linuxPath) + makeSwitches.array
    command = customizeCommandAndEnvironment(command, environment)

    /*-
     * Two reasons for using a GeneralCommandLine here:
     *
     * 1. the child `wsl.exe` process may terminate with
     *    "The COM+ registry database detected a system error" message;
     * 2. the output of Make may be wrapped at 80th column, having extra
     *    line breaks where it shouldn't.
     */
    val localCommandLine = command.toCommandLine(::GeneralCommandLine,
                                                 localWorkDirectory,
                                                 environment,
                                                 useCygwinLaunch = false)

    /*
     * Currently, setting the remote work directory has no effect if
     * `executeCommandInShell` is `false` (otherwise it would simply result in
     * running `/bin/sh -c cd .. && make`).
     */
    val wslOptions = WSLCommandLineOptions()
      .setLaunchWithWslExe(true)
      .setExecuteCommandInShell(false)
      .setRemoteWorkingDirectory(remoteWorkDirectory)
      .setPassEnvVarsUsingInterop(true)

    return distribution.patchCommandLine(localCommandLine,
                                         project,
                                         wslOptions)
  }

  private fun makeSwitches(makefile: @NlsSafe String?,
                           workDirectory: @NlsSafe String?): ParametersList {
    val makeSwitches = ParametersList()

    if (makefile != null) {
      makeSwitches.addAll(SWITCH_FILE, makefile)
    }

    if (workDirectory != null) {
      makeSwitches.addAll(SWITCH_DIRECTORY, workDirectory)
    }

    /*
     * Pass extra arguments *after* -f/-C so that the user can override those.
     */
    makeSwitches.addParametersString(arguments)

    if (target.isNotEmpty()) {
      makeSwitches.addParametersString(target)
    }

    return makeSwitches
  }

  private fun environment(): MutableMap<@NlsSafe String, @NlsSafe String> {
    val parentEnvironment = when {
      environmentVariables.isPassParentEnvs -> EnvironmentUtil.getEnvironmentMap()
      else -> emptyMap()
    }
    return (parentEnvironment + environmentVariables.envs).toMutableMap()
  }

  /**
   * Starting `wsl.exe` with an empty parent environment ([ParentEnvironmentType.NONE])
   * **and** an empty (or almost empty) own environment (i. e. when the "Include
   * system environment variables" box is un-checked in the run configuration
   * settings) while also using a [PtyCommandLine] (instead of a regular
   * [GeneralCommandLine]) results in the process failure, with the only line
   * logged to stdout:
   *
   * > The COM+ registry database detected a system error
   *
   * Of course, we can switch to [ParentEnvironmentType.CONSOLE] (which is
   * enough for WSL), but that defies the whole purpose of having a checkbox in
   * the UI.
   *
   * @param commandLineInit the no-arg constructor of either [GeneralCommandLine]
   *   or any of its descendants.
   * @see ParentEnvironmentType
   * @see GeneralCommandLine
   * @see PtyCommandLine
   */
  private fun Array<@NlsSafe String>.toCommandLine(commandLineInit: () -> GeneralCommandLine,
                                                   workDirectory: @NlsSafe String?,
                                                   environment: Map<@NlsSafe String, @NlsSafe String>,
                                                   useCygwinLaunch: Boolean): GeneralCommandLine =
    commandLineInit()
      .withUseCygwinLaunchEx(useCygwinLaunch)
      .withExePath(this[0])
      .withWorkDirectory(workDirectory)
      .withEnvironment(environment)
      .withParentEnvironmentType(ParentEnvironmentType.NONE)
      .withParameters(slice(1 until size))
      .withCharset(EncodingManager.getInstance().defaultConsoleEncoding)

  /**
   * A useful extension which works uniformly for both [PtyCommandLine] and
   * [GeneralCommandLine].
   *
   * @see PtyCommandLine.withUseCygwinLaunch
   */
  private fun GeneralCommandLine.withUseCygwinLaunchEx(useCygwinLaunch: Boolean) =
    when (this) {
      is PtyCommandLine -> withUseCygwinLaunch(useCygwinLaunch)
      else -> this
    }

  @RequiresEdt
  private fun customizeCommandAndEnvironment(command: Array<@NlsSafe String>,
                                             environment: MutableMap<@NlsSafe String, @NlsSafe String>): Array<@NlsSafe String> {
    /*
     * The result of last successful invocation, needed for the fail-safe scenario.
     */
    var lastCommand = command

    return try {
      LocalTerminalCustomizer.EP_NAME.extensions.fold(command) { acc, customizer ->
        try {
          customizer.customizeCommandAndEnvironment(project, null, acc, environment)
        }
        catch (_: Throwable) {
          acc
        }.also {
          /*
           * Remember the result of last successful invocation.
           */
          lastCommand = it
        }
      }
    }
    catch (_: Throwable) {
      // optional dependency
      lastCommand
    }
  }

  @RequiresEdt
  private inline fun Logger.debugInBackground(crossinline lazyMessage: () -> @NonNls String) {
    if (isDebugEnabled) {
      ApplicationManager.getApplication().executeOnPooledThread {
        debug(lazyMessage())
      }
    }
  }
}
