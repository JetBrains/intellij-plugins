package com.intellij.clion.diagnostics.cmake.workspace

import com.jetbrains.cidr.cpp.cmake.workspace.CMakeWorkspace
import com.jetbrains.cidr.cpp.diagnostics.model.CMakeProfileInfo
import com.jetbrains.cidr.cpp.diagnostics.model.CMakeSection
import com.jetbrains.cidr.cpp.diagnostics.model.WorkspaceSection
import com.jetbrains.cidr.cpp.diagnostics.workspace.WorkspaceDescriptionProvider
import com.jetbrains.cidr.project.workspace.CidrWorkspace

class CMakeWorkspaceDescriptionProvider: WorkspaceDescriptionProvider {
  override fun describe(workspace: CidrWorkspace): WorkspaceSection? {
    if (workspace !is CMakeWorkspace) return null

    val profiles = workspace.profileInfos.map { profileInfo ->
      val profile = profileInfo.profile
      CMakeProfileInfo(
        name = profile.name,
        buildType = profile.buildType,
        toolchainName = profile.toolchainName,
        effectiveToolchain = profileInfo.environment?.toolchain?.name,
        generationOptions = profile.generationOptions,
        generationDir = profile.generationDir?.toString(),
        effectiveGenerationDir = profileInfo.generationDir.toString(),
        buildOptions = profile.buildOptions
      )
    }

    return CMakeSection(
      autoReloadEnabled = workspace.settings.isAutoReloadEnabled,
      profiles = profiles
    )
  }
}