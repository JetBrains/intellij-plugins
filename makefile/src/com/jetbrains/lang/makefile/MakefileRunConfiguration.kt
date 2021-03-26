package com.jetbrains.lang.makefile

import com.intellij.execution.Executor
import com.intellij.execution.configuration.EnvironmentVariablesData
import com.intellij.execution.configurations.*
import com.intellij.execution.process.ColoredProcessHandler
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessTerminatedListener
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.components.PathMacroManager
import com.intellij.openapi.project.Project
import com.intellij.util.EnvironmentUtil
import com.intellij.util.getOrCreate
import org.jdom.Element
import org.jetbrains.plugins.terminal.LocalTerminalCustomizer
import java.io.File

class MakefileRunConfiguration(project: Project, factory: MakefileRunConfigurationFactory, name: String) : LocatableConfigurationBase<RunProfileState>(project, factory, name) {
  var filename = ""
  var target = ""
  var workingDirectory = ""
  var environmentVariables: EnvironmentVariablesData = EnvironmentVariablesData.DEFAULT
  var arguments = ""

  private companion object {
    const val MAKEFILE = "makefile"
    const val FILENAME = "filename"
    const val TARGET = "target"
    const val WORKING_DIRECTORY = "workingDirectory"
    const val ARGUMENTS = "arguments"
  }

  override fun checkConfiguration() {
  }

  override fun getConfigurationEditor() = MakefileRunConfigurationEditor(project)

  override fun writeExternal(element: Element) {
    super.writeExternal(element)
    val child = element.getOrCreate(MAKEFILE)
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

  override fun getState(executor: Executor, executionEnvironment: ExecutionEnvironment): RunProfileState? {
    val makeSettings = project.getService(MakefileProjectSettings::class.java).settings
    val makePath = makeSettings?.path ?: DEFAULT_MAKE_PATH
    return object : CommandLineState(executionEnvironment) {
      override fun startProcess(): ProcessHandler {
        val params = ParametersList()
        params.addParametersString(arguments)
        val macroManager = PathMacroManager.getInstance(project)
        val path = macroManager.expandPath(filename)
        params.addAll("-f", path)
        if (target.isNotEmpty()) {
          params.addParametersString(target)
        }

        val workDirectory = if (workingDirectory.isNotEmpty()) macroManager.expandPath(workingDirectory) else File(path).parent

        val parentEnvs = if (environmentVariables.isPassParentEnvs) EnvironmentUtil.getEnvironmentMap() else emptyMap<String,String>()
        val envs = parentEnvs + environmentVariables.envs.toMutableMap()
        var command = arrayOf(makePath) + params.array
        try {
          for (customizer in LocalTerminalCustomizer.EP_NAME.extensions) {
            try {
              command = customizer.customizeCommandAndEnvironment(project, command, envs)
            } catch (e: Throwable) {
            }
          }
        } catch (e: Throwable) {
          // optional dependency
        }

        val cmd = PtyCommandLine()
            .withUseCygwinLaunch(makeSettings?.useCygwin ?: false)
            .withExePath(command[0])
            .withWorkDirectory(workDirectory)
            .withEnvironment(envs)
            .withParentEnvironmentType(GeneralCommandLine.ParentEnvironmentType.NONE)
            .withParameters(command.slice(1 until command.size))

        val processHandler = ColoredProcessHandler(cmd)
        processHandler.setShouldKillProcessSoftly(true)
        ProcessTerminatedListener.attach(processHandler)
        return processHandler
      }
    }
  }
}