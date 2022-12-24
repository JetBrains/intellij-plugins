package com.jetbrains.cidr.cpp.embedded.platformio.project.builds

import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectModelExternalSource
import com.jetbrains.cidr.cpp.embedded.platformio.project.ID
import com.jetbrains.cidr.execution.CidrBuildConfiguration
import com.jetbrains.cidr.execution.build.CidrBuildConfigurationProvider

class PlatformioBuildConfigurationProvider : CidrBuildConfigurationProvider {
  override fun getBuildableConfigurations(project: Project): List<CidrBuildConfiguration> = PlatformioBuildConfigurations

  override fun getCleanableConfigurations(project: Project): List<CidrBuildConfiguration> = getBuildableConfigurations(project)
}

object PlatformioBuildConfiguration : CidrBuildConfiguration {
  override fun getName(): String = "PlatformIO"

  override fun getExternalSource(): ProjectModelExternalSource {
    return object : ProjectModelExternalSource {
      override fun getDisplayName(): String =
        ID.readableName

      override fun getId(): String = ID.id
    }
  }
}

val PlatformioBuildConfigurations: List<PlatformioBuildConfiguration> =
  listOf(PlatformioBuildConfiguration)