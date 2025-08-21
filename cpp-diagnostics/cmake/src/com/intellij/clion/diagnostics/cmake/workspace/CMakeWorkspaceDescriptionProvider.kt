package com.intellij.clion.diagnostics.cmake.workspace

import com.jetbrains.cidr.cpp.cmake.workspace.CMakeWorkspace
import com.jetbrains.cidr.cpp.diagnostics.CdIndenter
import com.jetbrains.cidr.cpp.diagnostics.workspace.WorkspaceDescriptionProvider
import com.jetbrains.cidr.project.workspace.CidrWorkspace

class CMakeWorkspaceDescriptionProvider: WorkspaceDescriptionProvider {
  override fun describe(workspace: CidrWorkspace, log: CdIndenter): Boolean {
    if (workspace !is CMakeWorkspace) return false

    log.put("Auto reload enabled: ${workspace.settings.isAutoReloadEnabled}")

    val profiles = workspace.profileInfos
    for (profileInfo in profiles) {
      // "profile" is the data as written in CMake options.
      // "profileInfo" is the processed data ready to use by other clients
      val profile = profileInfo.profile
      log.scope {
        log.put("Profile: ${profile.name}")
        log.scope {
          log.put("buildType: ${profile.buildType}")
          log.put("toolchainName: ${profile.toolchainName}")
          log.put("effective toolchain: ${profileInfo.environment?.toolchain?.name ?: "UNKNOWN"}")
          log.put("generationOptions: ${profile.generationOptions}")
          log.put("generationDir: ${profile.generationDir}")
          log.put("effective generation dir: ${profileInfo.generationDir}")
          log.put("buildOptions: ${profile.buildOptions}")
        }
      }
    }

    return true
  }
}