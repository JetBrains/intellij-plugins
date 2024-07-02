// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.runtime

import com.intellij.CommonBundle
import com.intellij.execution.*
import com.intellij.execution.configuration.EnvironmentVariablesComponent
import com.intellij.execution.configurations.*
import com.intellij.execution.process.KillableColoredProcessHandler
import com.intellij.execution.process.OSProcessHandler
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessTerminatedListener
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.ui.ConsoleView
import com.intellij.execution.util.ProgramParametersUtil
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.InvalidDataException
import com.intellij.openapi.util.WriteExternalException
import com.intellij.platform.ide.progress.runWithModalProgressBlocking
import com.intellij.util.xmlb.XmlSerializer
import org.intellij.terraform.config.actions.TerraformInitCommandFilter
import org.intellij.terraform.config.actions.isPathExecutable
import org.intellij.terraform.config.actions.isTerraformExecutable
import org.intellij.terraform.config.util.TFExecutor
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.install.getBinaryName
import org.jdom.Element
import org.jetbrains.annotations.Nls

class TerraformRunConfiguration(
  project: Project,
  factory: ConfigurationFactory,
  name: String?,
  override var envFilePaths: List<String>,
) :
  LocatableConfigurationBase<Any?>(project, factory, name), CommonProgramRunConfigurationParameters, EnvFilesOptions, DumbAware {
  private var programParameters: String? = ""
  private var directory: String = ""
  private val myEnvs: MutableMap<String, String> = LinkedHashMap()
  private var passParentEnvs: Boolean = true

  override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration?> {
    return TerraformRunConfigurationEditor()
  }

  @Throws(ExecutionException::class)
  override fun getState(executor: Executor, env: ExecutionEnvironment): RunProfileState? {
    val error = error
    if (!isTerraformExecutable(project)) {
      return null
    }
    if (error != null) {
      throw ExecutionException(error)
    }

    return object : CommandLineState(env) {
      @Throws(ExecutionException::class)
      override fun startProcess(): ProcessHandler {
        val handler: OSProcessHandler = KillableColoredProcessHandler(createCommandLine())
        ProcessTerminatedListener.attach(handler)
        return handler
      }

      override fun createConsole(executor: Executor): ConsoleView? {
        val consoleView = super.createConsole(executor) ?: return null
        consoleView.addMessageFilter(TerraformInitCommandFilter(project, parameters.workingDirectory))
        return consoleView
      }

      @Throws(ExecutionException::class)
      private fun createCommandLine(): GeneralCommandLine {
        val parameters = parameters

        return TFExecutor.`in`(project)
          .withPresentableName(HCLBundle.message("terraform.run.configuration.name"))
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

          ProgramParametersUtil.configureConfiguration(params, this@TerraformRunConfiguration)

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
  }

  @Throws(RuntimeConfigurationException::class)
  override fun checkConfiguration() {
    if (directory.isBlank()) {
      val exception = RuntimeConfigurationException(HCLBundle.message("run.configuration.no.working.directory.specified"))
      exception.setQuickFix(Runnable { workingDirectory = project.basePath })
      throw exception
    }

    if (!isPathExecutable(terraformPath)) {
      val exception = RuntimeConfigurationException(
        HCLBundle.message("run.configuration.terraform.path.incorrect", terraformPath),
        CommonBundle.getErrorTitle()
      )
      exception.setQuickFix(Runnable {
        val settings = TerraformProjectSettings.getInstance(project)
        settings.terraformPath = with(TerraformPathDetector.getInstance(project)) {
          detectedPath ?: run {
            runWithModalProgressBlocking(project, HCLBundle.message("progress.title.detecting.terraform.executable")) {
              detect()
            }
            detectedPath ?: getBinaryName()
          }
        }
      })
      throw exception
    }

    val error = error
    if (error != null) {
      throw RuntimeConfigurationException(error)
    }
  }

  private val error: @Nls String?
    get() {
      if (directory.isBlank()) {
        return HCLBundle.message("run.configuration.no.working.directory.specified")
      }
      if (terraformPath.isBlank()) {
        return HCLBundle.message("run.configuration.no.terraform.specified")
      }
      if (!isPathExecutable(terraformPath)) {
        return HCLBundle.message("run.configuration.terraform.path.incorrect", terraformPath)
      }
      return null
    }

  private val terraformPath
    get() = TerraformPathDetector.getInstance(project).actualTerraformPath

  override fun setProgramParameters(value: String?) {
    programParameters = value
  }

  override fun getProgramParameters(): String? {
    return programParameters
  }

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
