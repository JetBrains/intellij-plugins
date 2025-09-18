package com.jetbrains.cidr.cpp.diagnostics.workspace

import com.intellij.openapi.extensions.ExtensionPointName
import com.jetbrains.cidr.cpp.diagnostics.model.WorkspaceSection
import com.jetbrains.cidr.project.workspace.CidrWorkspace

interface WorkspaceDescriptionProvider {
  fun describe(workspace: CidrWorkspace): WorkspaceSection?

  companion object {
    val EP_NAME: ExtensionPointName<WorkspaceDescriptionProvider> = ExtensionPointName.create<WorkspaceDescriptionProvider>(
      "com.intellij.clion.diagnostics.workspaceDescriptionProvider"
    )

    fun describe(workspace: CidrWorkspace): List<WorkspaceSection> {
      val result = mutableListOf<WorkspaceSection>()
      for (provider in EP_NAME.extensionList) {
        provider.describe(workspace)?.let { result += it }
      }
      return result
    }
  }
}