package com.jetbrains.cidr.cpp.embedded.platformio.project

import com.intellij.execution.ExecutionTarget
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.openapi.util.NlsSafe
import com.intellij.openapi.util.registry.Registry
import com.jetbrains.cidr.cpp.embedded.platformio.PlatformioDebugConfiguration
import com.jetbrains.cidr.cpp.embedded.platformio.project.builds.PlatformioBuildTarget
import com.jetbrains.cidr.cpp.execution.compound.CidrCompoundRunConfiguration
import icons.ClionEmbeddedPlatformioIcons
import javax.swing.Icon

class PlatformioExecutionTarget(@NlsSafe private val myId: String) : ExecutionTarget() {
  override fun getId(): String = this.myId

  override fun getDisplayName(): String = myId

  override fun getIcon(): Icon = ClionEmbeddedPlatformioIcons.LogoPlatformIO
  override fun canRun(configuration: RunConfiguration): Boolean {
    return isPlatformioRunConfiguration(configuration)
  }

  override fun isExternallyManaged(): Boolean {
    return Registry.get("intellij.clion.profiles.enabled").asBoolean()
  }

  companion object {
    fun isPlatformioRunConfiguration(configuration: RunConfiguration): Boolean {
      return when(configuration) {
        is PlatformioDebugConfiguration -> true
        is CidrCompoundRunConfiguration -> configuration.buildTarget is PlatformioBuildTarget
        else -> false
      }
    }
  }
}