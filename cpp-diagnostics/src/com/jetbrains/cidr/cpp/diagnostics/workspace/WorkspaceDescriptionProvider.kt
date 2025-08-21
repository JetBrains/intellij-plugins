package com.jetbrains.cidr.cpp.diagnostics.workspace

import com.intellij.openapi.extensions.ExtensionPointName
import com.jetbrains.cidr.cpp.diagnostics.CdIndenter
import com.jetbrains.cidr.project.workspace.CidrWorkspace

interface WorkspaceDescriptionProvider {
  fun describe(workspace: CidrWorkspace, log: CdIndenter): Boolean

  companion object {
    val EP_NAME: ExtensionPointName<WorkspaceDescriptionProvider> = ExtensionPointName.create<WorkspaceDescriptionProvider>(
      "com.intellij.clion.diagnostics.workspaceDescriptionProvider"
    )

    fun describe(workspace: CidrWorkspace, log: CdIndenter): Boolean {
      for (provider in EP_NAME.extensionList) {
        if (provider.describe(workspace, log)) {
          return true
        }
      }

      return false
    }
  }
}