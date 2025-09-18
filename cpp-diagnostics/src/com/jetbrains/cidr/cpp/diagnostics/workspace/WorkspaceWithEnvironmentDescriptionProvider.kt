package com.jetbrains.cidr.cpp.diagnostics.workspace

import com.jetbrains.cidr.cpp.diagnostics.model.ToolchainsNamesSection
import com.jetbrains.cidr.cpp.diagnostics.model.WorkspaceSection
import com.jetbrains.cidr.cpp.toolchains.CPPEnvironment
import com.jetbrains.cidr.project.workspace.CidrWorkspace
import com.jetbrains.cidr.project.workspace.WorkspaceWithEnvironment

class WorkspaceWithEnvironmentDescriptionProvider: WorkspaceDescriptionProvider {
  override fun describe(workspace: CidrWorkspace): WorkspaceSection? {
    if (workspace !is WorkspaceWithEnvironment) {
      return null
    }

    val names = workspace.getEnvironment()
      .filterIsInstance<CPPEnvironment>()
      .map { it.toolchain.name }

    return ToolchainsNamesSection(names)
  }
}