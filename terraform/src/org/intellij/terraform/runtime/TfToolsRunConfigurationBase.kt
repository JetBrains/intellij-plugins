// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.runtime

import com.intellij.CommonBundle
import com.intellij.execution.*
import com.intellij.execution.configuration.EnvironmentVariablesComponent
import com.intellij.execution.configurations.*
import com.intellij.execution.filters.Filter
import com.intellij.execution.process.KillableColoredProcessHandler
import com.intellij.execution.process.OSProcessHandler
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessTerminatedListener
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.runners.ProgramRunner
import com.intellij.execution.ui.ConsoleView
import com.intellij.execution.util.ProgramParametersUtil
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.InvalidDataException
import com.intellij.openapi.util.WriteExternalException
import com.intellij.platform.ide.progress.runWithModalProgressBlocking
import com.intellij.util.text.findTextRange
import com.intellij.util.xmlb.XmlSerializer
import org.intellij.terraform.config.actions.TfActionService
import org.intellij.terraform.config.util.TfExecutor
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.install.TfToolType
import org.jdom.Element
import kotlin.io.path.Path
import kotlin.io.path.notExists

internal abstract class TfToolsRunConfigurationBase(
  project: Project,
  factory: ConfigurationFactory,
  name: String?,
  override var envFilePaths: List<String>,
  val toolType: TfToolType,
) : RunConfigurationBase<Any?>(project, factory, name), CommonProgramRunConfigurationParameters, EnvFilesOptions, DumbAware {

  private var directory: String = project.basePath ?: ""
  private val myEnvs: MutableMap<String, String> = LinkedHashMap()
  private var passParentEnvs: Boolean = true

  internal var commandType: TfCommand = TfCommand.CUSTOM
  internal var globalOptions: String = ""
  internal var passGlobalOptions: Boolean = false
  internal var programArguments: String = ""

  private val toolPath
    get() = toolType.getToolSettings(project).toolPath

  @Throws(ExecutionException::class)
  override fun getState(executor: Executor, env: ExecutionEnvironment): RunProfileState? {
    try {
      checkWorkingDirAndThrow()
    }
    catch (e: Exception) {
      logger<TfToolsRunConfigurationBase>().debug("Run configuration state failure for tool: ${toolType.displayName} folder ${directory}, command: ${commandType}", e)
      throw ExecutionException(e.message, e)
    }
    return TfToolCommandLineState(project, this, env, toolType)
  }

  @Throws(RuntimeConfigurationException::class)
  override fun checkConfiguration() {
    checkWorkingDirAndThrow()
    checkExecutableAndThrow()
  }

  private fun checkExecutableAndThrow() {
    if (!ToolPathDetector.getInstance(this.project).isExecutable(Path(toolPath))) {
      val exception = RuntimeConfigurationException(
        HCLBundle.message("run.configuration.terraform.path.incorrect", toolPath.ifEmpty { toolType.executableName }, toolType.displayName),
        CommonBundle.getErrorTitle()
      )
      exception.setQuickFix(Runnable {
        runWithModalProgressBlocking(project, HCLBundle.message("progress.title.detecting.terraform.executable", toolType.displayName)) {
          ToolPathDetector.getInstance(project).detectAndVerifyTool(toolType, true)
        }
      })
      throw exception
    }
  }

  private fun checkWorkingDirAndThrow() {
    if (workingDirectory.isNullOrEmpty()) {
      val exception = RuntimeConfigurationException(HCLBundle.message("run.configuration.no.working.directory.specified"))
      exception.setQuickFix(Runnable { workingDirectory = project.basePath })
      throw exception
    }
    if (workingDirectory?.let { Path(it).notExists() } == true) {
      val exception = RuntimeConfigurationException(HCLBundle.message("run.configuration.working.directory.doesnt.exist", workingDirectory))
      exception.setQuickFix(Runnable { workingDirectory = project.basePath })
      throw exception
    }
  }

  override fun setProgramParameters(value: String?): Unit = Unit

  override fun getProgramParameters(): String = listOf(
    if (passGlobalOptions) globalOptions else "",
    commandType.command,
    programArguments
  ).joinToString(" ") { it.trim() }.trim()

  override fun setWorkingDirectory(value: String?) {
    directory = ExternalizablePath.urlValue(value)
  }

  override fun getWorkingDirectory(): String? {
    return ExternalizablePath.localPathValue(directory)
  }

  override fun setPassParentEnvs(passParentEnvs: Boolean) {
    this.passParentEnvs = passParentEnvs
  }

  override fun getEnvs(): Map<String, String> {
    return myEnvs
  }

  override fun setEnvs(envs: Map<String, String>) {
    myEnvs.clear()
    myEnvs.putAll(envs)
  }

  override fun isPassParentEnvs(): Boolean {
    return passParentEnvs
  }

  @Throws(InvalidDataException::class)
  override fun readExternal(element: Element) {
    super.readExternal(element)
    XmlSerializer.deserializeInto(this, element)
    EnvironmentVariablesComponent.readExternal(element, envs)
  }

  @Throws(WriteExternalException::class)
  override fun writeExternal(element: Element) {
    super.writeExternal(element)
    XmlSerializer.serializeInto(this, element)
    EnvironmentVariablesComponent.writeExternal(element, envs)
  }
}

internal class TfToolCommandLineState(
  private val project: Project,
  private val configParams: TfToolsRunConfigurationBase,
  env: ExecutionEnvironment,
  val toolType: TfToolType,
) : CommandLineState(env) {

  override fun execute(executor: Executor, runner: ProgramRunner<*>): ExecutionResult {
    val isToolDetected = runWithModalProgressBlocking(project, HCLBundle.message("progress.title.detecting.terraform.executable", toolType.displayName)) {
      ToolPathDetector.getInstance(project).detectAndVerifyTool(toolType, false)
    }
    if (!isToolDetected) {
      showIncorrectPathNotification(project, toolType)
      throw ProcessCanceledException()
    }
    return super.execute(executor, runner)
  }

  @Throws(ExecutionException::class)
  override fun startProcess(): ProcessHandler {
    val handler: OSProcessHandler = KillableColoredProcessHandler(createCommandLine())
    ProcessTerminatedListener.attach(handler)
    return handler
  }

  override fun createConsole(executor: Executor): ConsoleView? {
    val consoleView = super.createConsole(executor) ?: return null
    consoleView.addMessageFilter(TfToolInitCommandFilter(project, parameters.workingDirectory, toolType))
    return consoleView
  }

  @Throws(ExecutionException::class)
  private fun createCommandLine(): GeneralCommandLine {
    val parameters = parameters

    return TfExecutor.`in`(project, toolType)
      .withPresentableName(HCLBundle.message("terraform.run.configuration.name", toolType.displayName))
      .withWorkDirectory(parameters.workingDirectory)
      .withParameters(parameters.programParametersList.parameters)
      .withPassParentEnvironment(parameters.isPassParentEnvs)
      .withExtraEnvironment(parameters.env.handleEnvVar())
      .showOutputOnError()
      .createCommandLine()
  }

  private val parameters: SimpleProgramParameters
    get() {
      val params = SimpleProgramParameters()

      ProgramParametersUtil.configureConfiguration(params, configParams)

      return params
    }

  private fun Map<String, String>.handleEnvVar(): Map<String, String> = this.mapValues { (_, value) ->
    if (value.startsWith('$')) {
      System.getenv(value.substring(1)) ?: value
    }
    else {
      value
    }
  }
}

private class TfToolInitCommandFilter(
  val project: Project,
  val directory: String,
  toolType: TfToolType,
) : Filter {

  val command: String = "${toolType.getBinaryName()} init"

  override fun applyFilter(line: String, entireLength: Int): Filter.Result? {
    if (!line.contains(command)) return null
    val textRange = line.findTextRange(command) ?: return null
    return Filter.Result(entireLength - line.length + textRange.startOffset,
                         entireLength - line.length + textRange.endOffset
    ) {
      project.service<TfActionService>().scheduleTerraformInit(directory, notifyOnSuccess = true)
    }
  }
}
