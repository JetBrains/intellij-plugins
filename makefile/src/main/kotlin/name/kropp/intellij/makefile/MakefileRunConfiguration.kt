package name.kropp.intellij.makefile

import com.intellij.execution.Executor
import com.intellij.execution.configurations.CommandLineState
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.configurations.RunConfigurationBase
import com.intellij.execution.configurations.RunProfileState
import com.intellij.execution.process.OSProcessHandler
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessTerminatedListener
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.project.Project
import com.intellij.util.getOrCreate
import org.jdom.Element

class MakefileRunConfiguration(project: Project, factory: MakefileRunConfigurationFactory, name: String) : RunConfigurationBase(project, factory, name) {
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
    return object : CommandLineState(executionEnvironment) {
      override fun startProcess(): ProcessHandler {
        val cmd = GeneralCommandLine()
            .withExePath("/usr/bin/make")
            .withWorkDirectory(project.basePath)
            .withParameters("-f", filename, target)
        val processHandler = OSProcessHandler(cmd)
        ProcessTerminatedListener.attach(processHandler)
        return processHandler
      }
    }
  }
}