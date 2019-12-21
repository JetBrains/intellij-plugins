package name.kropp.intellij.makefile

import com.intellij.execution.*
import com.intellij.execution.configuration.*
import com.intellij.execution.configurations.*
import com.intellij.execution.process.*
import com.intellij.execution.runners.*
import com.intellij.openapi.components.*
import com.intellij.openapi.project.*
import com.intellij.util.*
import org.jdom.*
import java.io.*

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
        val macroManager = PathMacroManager.getInstance(project)
        val path = macroManager.expandPath(filename)
        params.addAll("-f", path)
        if (!target.isEmpty()) {
          params.addParametersString(target)
        }
        val workDirectory = if (workingDirectory.isNotEmpty()) macroManager.expandPath(workingDirectory) else File(path).parent
        val cmd = PtyCommandLine()
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
}