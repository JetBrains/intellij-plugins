package com.jetbrains.cidr.cpp.embedded.platformio.ui

import com.intellij.clion.profiles.CLionProfile
import com.intellij.clion.profiles.CLionProfileGroup
import com.intellij.execution.ExecutionTargetManager
import com.intellij.execution.RunManager
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.openapi.project.Project
import com.jetbrains.cidr.cpp.embedded.platformio.ClionEmbeddedPlatformioBundle
import com.jetbrains.cidr.cpp.embedded.platformio.PlatformioConfigurable
import com.jetbrains.cidr.cpp.embedded.platformio.project.PlatformioExecutionTarget
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.NonNls

internal class PlatformioProfileGroup : CLionProfileGroup {
  override val id: String = "platformio"
  override val name: @Nls String = ClionEmbeddedPlatformioBundle.message("platformio.environments")
  override val configurableId: @NonNls String = PlatformioConfigurable.ID
  override val configurableLabel: @Nls String = ClionEmbeddedPlatformioBundle.message("platformio.settings")

  override fun getProfiles(runConfiguration: RunConfiguration?, project: Project): List<CLionProfile> {
    return ExecutionTargetManager.getInstance(project).getTargetsFor(runConfiguration)
      .filterIsInstance<PlatformioExecutionTarget>().map { it.toCLionProfile() }
  }

  override fun getSelectedProfile(project: Project): CLionProfile? {
    val target = ExecutionTargetManager.getInstance(project).findTarget(project.selectedRunConfiguration()) as? PlatformioExecutionTarget
    return target?.toCLionProfile()
  }

  override fun setSelectedProfile(profileId: String, project: Project) {
    val target = ExecutionTargetManager.getInstance(project).getTargetsFor(project.selectedRunConfiguration()).find { it.id == profileId } ?: return
    ExecutionTargetManager.getInstance(project).activeTarget = target
  }

  override fun isApplicable(runConfiguration: RunConfiguration?, project: Project): Boolean {
    return runConfiguration != null && PlatformioExecutionTarget.isPlatformioRunConfiguration(runConfiguration)
  }
}

private fun Project.selectedRunConfiguration(): RunConfiguration? = RunManager.getInstance(this).selectedConfiguration?.configuration
private fun PlatformioExecutionTarget.toCLionProfile(): CLionProfile = CLionProfile(id, displayName)
