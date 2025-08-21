package com.jetbrains.cidr.cpp.diagnostics.workspace

import com.jetbrains.cidr.cpp.diagnostics.CdIndenter
import com.jetbrains.cidr.cpp.toolchains.CPPEnvironment
import com.jetbrains.cidr.project.workspace.CidrWorkspace
import com.jetbrains.cidr.project.workspace.WorkspaceWithEnvironment

class WorkspaceWithEnvironmentDescriptionProvider: WorkspaceDescriptionProvider {
  override fun describe(workspace: CidrWorkspace, log: CdIndenter): Boolean {
    if (workspace !is WorkspaceWithEnvironment) {
      return false
    }

    log.put("Toolchains:")
    log.scope {
      workspace.getEnvironment()
        .filterIsInstance<CPPEnvironment>()
        .forEach { log.put(it.toolchain.name) }
    }

    return false
  }
}