package com.jetbrains.cidr.cpp.embedded.platformio

import com.intellij.execution.DefaultExecutionTarget
import com.intellij.execution.ExecutionTarget
import com.intellij.execution.Executor
import com.intellij.execution.configurations.CommandLineState
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.TargetAwareRunProfile
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.jetbrains.cidr.cpp.embedded.platformio.project.PlatformioBuildConfigurationHelper
import com.jetbrains.cidr.cpp.embedded.platformio.project.builds.PlatformioBuildConfiguration
import com.jetbrains.cidr.cpp.embedded.platformio.project.builds.PlatformioBuildTarget
import com.jetbrains.cidr.cpp.execution.CLionRunConfiguration
import com.jetbrains.cidr.execution.CidrBuildConfigurationHelper
import com.jetbrains.cidr.execution.CidrCommandLineState
import com.jetbrains.cidr.execution.CidrExecutableDataHolder
import com.jetbrains.cidr.execution.ExecutableData
import com.jetbrains.cidr.lang.workspace.OCResolveConfiguration
import javax.swing.JComponent
import javax.swing.JPanel

class PlatformioDebugConfiguration(project: Project, configurationFactory: ConfigurationFactory) :
  CLionRunConfiguration<PlatformioBuildConfiguration, PlatformioBuildTarget>
  (project, configurationFactory, "Debug"),
  CidrExecutableDataHolder, TargetAwareRunProfile {

  private var executableData: ExecutableData? = null
  override fun getExecutableData() = executableData

  override fun setExecutableData(executableData: ExecutableData?) {
    this.executableData = executableData
  }

  override fun getConfigurationEditor(): SettingsEditor<PlatformioDebugConfiguration> =
    object : SettingsEditor<PlatformioDebugConfiguration>() {
      override fun resetEditorFrom(s: PlatformioDebugConfiguration) {}

      override fun applyEditorTo(s: PlatformioDebugConfiguration) {}

      override fun createEditor(): JComponent = JPanel()
    }

  override fun canRunOn(target: ExecutionTarget): Boolean {
    return DefaultExecutionTarget.INSTANCE != target
  }

  override fun getResolveConfiguration(target: ExecutionTarget): OCResolveConfiguration? = null

  override fun getState(executor: Executor, env: ExecutionEnvironment): CommandLineState {
    val launcher = PlatformioLauncher(env, this)
    return CidrCommandLineState(env, launcher)
  }

  override fun getHelper(): CidrBuildConfigurationHelper<PlatformioBuildConfiguration, PlatformioBuildTarget> =
    PlatformioBuildConfigurationHelper(project)

}

