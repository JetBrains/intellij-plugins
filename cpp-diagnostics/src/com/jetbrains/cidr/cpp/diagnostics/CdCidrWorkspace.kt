package com.jetbrains.cidr.cpp.diagnostics

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.jetbrains.cidr.cpp.cmake.workspace.CMakeWorkspace
import com.jetbrains.cidr.project.workspace.CidrWorkspaceManager

/**
 * CidrWorkspace represents a buildsystem-specific data, and makes sure IntelliJ project model and OCWorkspace stays in sync with it
 */
fun collectCidrWorkspaces(project: Project): String {
  val log = CdIndenter()
  val workspaces = CidrWorkspaceManager.getInstance(project).initializedWorkspaces
  log.put("Workspaces: ", workspaces.size)
  log.scope {
    for (workspace in workspaces) {
      log.put(workspace.javaClass)
      log.scope {
        log.put("Project path: ${workspace.projectPath}")
        log.put("Content root: ${workspace.contentRoot}")
        if (workspace is CMakeWorkspace) {
          describeCMakeWorkspace(workspace, log)
        }
        // todo: describe other build systems
      }
    }
  }
  return log.result
}

fun describeCMakeWorkspace(workspace: CMakeWorkspace, log: CdIndenter) {
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
}

fun collectOCWorkspaceEvents(project: Project): String {
  return project.service<CdWorkspaceEvents>().getResult()
}

