package name.kropp.intellij.makefile

import com.intellij.execution.Executor
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
  var filename: String = ""
  var target: String = ""

  override fun checkConfiguration() {
  }

  override fun getConfigurationEditor() = MakefileRunConfigurationEditor(project)

  override fun writeExternal(element: Element?) {
    super.writeExternal(element)
    val child = element!!.getOrCreate("makefile")
    child.setAttribute("filename", filename)
    child.setAttribute("target", target)
  }

  override fun readExternal(element: Element?) {
    super.readExternal(element)
    val child = element?.getChild("makefile")
    filename = child?.getAttributeValue("filename") ?: ""
    target = child?.getAttributeValue("target") ?: ""
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
            .withParameters(args)
        val processHandler = ColoredProcessHandler(cmd)
        ProcessTerminatedListener.attach(processHandler)
        return processHandler
      }
    }
  }
}