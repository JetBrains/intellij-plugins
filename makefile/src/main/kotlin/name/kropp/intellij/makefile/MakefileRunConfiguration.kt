package name.kropp.intellij.makefile

import com.intellij.execution.Executor
import com.intellij.execution.configuration.EnvironmentVariablesData
import com.intellij.execution.configurations.*
import com.intellij.execution.process.ColoredProcessHandler
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessTerminatedListener
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.application.PathMacros
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.project.Project
import com.intellij.util.getOrCreate
import org.jdom.Element
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
    val makePath = ServiceManager.getService(project, MakefileProjectSettings::class.java).settings?.path ?: DEFAULT_MAKE_PATH
    return object : CommandLineState(executionEnvironment) {
      override fun startProcess(): ProcessHandler {
        val params = ParametersList()
        params.addParametersString(arguments)
        params.addAll("-f", filename)
        if (!target.isEmpty()) {
          params.addParametersString(target)
        }
        val workDirectory = if (workingDirectory.isNotEmpty()) resolveMacros(workingDirectory) else File(filename).parent
        val cmd = GeneralCommandLine()
            .withExePath(makePath)
            .withWorkDirectory(workDirectory)
            .withEnvironment(environmentVariables.envs)
            .withParentEnvironmentType(if (environmentVariables.isPassParentEnvs) GeneralCommandLine.ParentEnvironmentType.CONSOLE else GeneralCommandLine.ParentEnvironmentType.NONE)
            .withParameters(params.list)
        val processHandler = ColoredProcessHandler(cmd)
        ProcessTerminatedListener.attach(processHandler)
        return processHandler
      }
    }
  }

  private fun resolveMacros(str: String): String {
    val macrosRegex = Regex("""\$([^\\$]*)\$""")
    val match = macrosRegex.matchEntire(str)
    if (match != null) {
      return PathMacros.getInstance().getValue(match.groupValues[1]) ?: return str
    }
    return str
  }
}