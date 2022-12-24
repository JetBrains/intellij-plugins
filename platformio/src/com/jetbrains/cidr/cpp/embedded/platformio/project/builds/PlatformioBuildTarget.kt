package com.jetbrains.cidr.cpp.embedded.platformio.project.builds

import com.jetbrains.cidr.execution.CidrBuildTarget
import icons.ClionEmbeddedPlatformioIcons
import javax.swing.Icon

class PlatformioBuildTarget(private val projectName: String) : CidrBuildTarget<PlatformioBuildConfiguration> {
  override fun getName(): String = "PlatformIO"

  override fun getProjectName(): String = projectName

  override fun getIcon(): Icon = ClionEmbeddedPlatformioIcons.Platformio

  override fun isExecutable(): Boolean = true

  override fun getBuildConfigurations(): List<PlatformioBuildConfiguration> =
    PlatformioBuildConfigurations
}
