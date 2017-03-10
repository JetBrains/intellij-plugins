package name.kropp.intellij.makefile

import com.intellij.execution.Executor
import com.intellij.execution.configuration.EnvironmentVariablesData
import com.intellij.execution.configurations.CommandLineState
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.configurations.LocatableConfigurationBase
import com.intellij.execution.configurations.RunProfileState
import com.intellij.execution.process.ColoredProcessHandler
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessTerminatedListener
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.project.Project
import com.intellij.util.getOrCreate
import org.jdom.Element
import java.io.File

class MakefileRunConfiguration(project: Project, factory: MakefileRunConfigurationFactory, name: String) : LocatableConfigurationBase(project, factory, name) {
  var filename = ""
  var target = ""
  var environmentVariables = EnvironmentVariablesData.DEFAULT

  private companion object {
    const val MAKEFILE = "makefile"
    const val FILENAME = "filename"
    const val TARGET = "target"
  }

  override fun checkConfiguration() {
  }

  override fun getConfigurationEditor() = MakefileRunConfigurationEditor(project)

  override fun writeExternal(element: Element?) {
    super.writeExternal(element)
    val child = element!!.getOrCreate(MAKEFILE)
    child.setAttribute(FILENAME, filename)
    child.setAttribute(TARGET, target)
    environmentVariables.writeExternal(child)
  }

  override fun readExternal(element: Element?) {
    super.readExternal(element)
    val child = element?.getChild(MAKEFILE)
    if (child != null) {
      filename = child.getAttributeValue(FILENAME) ?: ""
      target = child.getAttributeValue(TARGET) ?: ""
      environmentVariables = EnvironmentVariablesData.readExternal(child)
    }
  }

  override fun getState(executor: Executor, executionEnvironment: ExecutionEnvironment): RunProfileState? {
    val makePath = ServiceManager.getService(project, MakefileProjectSettings::class.java).settings?.path ?: DEFAULT_MAKE_PATH
    return object : CommandLineState(executionEnvironment) {
      override fun startProcess(): ProcessHandler {
        val args = mutableListOf("-f", filename)
        if (!target.isNullOrEmpty()) {
          args += target
        }
        val cmd = GeneralCommandLine()
            .withExePath(makePath)
            .withWorkDirectory(File(filename).parent)
            .withEnvironment(environmentVariables.envs)
            .withParentEnvironmentType(if (environmentVariables.isPassParentEnvs) GeneralCommandLine.ParentEnvironmentType.CONSOLE else GeneralCommandLine.ParentEnvironmentType.NONE)
            .withParameters(args)
        val processHandler = ColoredProcessHandler(cmd)
        ProcessTerminatedListener.attach(processHandler)
        return processHandler
      }
    }
  }
}