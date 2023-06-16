package com.jetbrains.cidr.cpp.embedded.platformio.project

import com.intellij.execution.RunnerAndConfigurationSettings
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.jetbrains.cidr.cpp.embedded.platformio.PlatformioConfigurationType
import com.jetbrains.cidr.cpp.embedded.platformio.PlatformioDebugConfiguration
import com.jetbrains.cidr.cpp.embedded.platformio.PlatformioService
import com.jetbrains.cidr.cpp.embedded.platformio.project.builds.PlatformioBuildConfiguration
import com.jetbrains.cidr.cpp.embedded.platformio.project.builds.PlatformioBuildTarget
import com.jetbrains.cidr.cpp.execution.manager.CLionRunConfigurationManagerHelper
import com.jetbrains.cidr.execution.CidrBuildConfiguration
import com.jetbrains.cidr.execution.CidrBuildConfigurationHelper
import com.jetbrains.cidr.execution.CidrBuildTarget
import java.util.function.Predicate

class PlatformioBuildConfigurationHelper(private val project: Project) : CidrBuildConfigurationHelper<PlatformioBuildConfiguration, PlatformioBuildTarget>() {
  override fun getTargets(): List<PlatformioBuildTarget> {
    return project.service<PlatformioService>().buildConfigurationTargets
  }
}

object PlatformioRunConfigurationManagerHelper : CLionRunConfigurationManagerHelper {
  override fun getBuildHelper(project: Project): PlatformioBuildConfigurationHelper =
    PlatformioBuildConfigurationHelper(project)

  override fun getConfigurationFactory(buildHelper: CidrBuildConfigurationHelper<out CidrBuildConfiguration, out CidrBuildTarget<*>>,
                                       buildTarget: CidrBuildTarget<*>): ConfigurationFactory =
    PlatformioConfigurationType().factory

  override fun getRunConfigurationTypeFilter(): Predicate<RunnerAndConfigurationSettings> {
    return Predicate { settings: RunnerAndConfigurationSettings ->
      settings.configuration is PlatformioDebugConfiguration
    }
  }
}
