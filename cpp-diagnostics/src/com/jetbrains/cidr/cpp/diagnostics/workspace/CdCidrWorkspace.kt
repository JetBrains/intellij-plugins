package com.jetbrains.cidr.cpp.diagnostics

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.registry.Registry
import com.jetbrains.cidr.cpp.diagnostics.model.CidrWorkspacesSection
import com.jetbrains.cidr.cpp.diagnostics.model.OCWorkspaceEventsSection
import com.jetbrains.cidr.cpp.diagnostics.model.WorkspaceInfo
import com.jetbrains.cidr.cpp.diagnostics.workspace.WorkspaceDescriptionProvider
import com.jetbrains.cidr.project.workspace.CidrWorkspaceManager

/**
 * CidrWorkspace represents a buildsystem-specific data, and makes sure IntelliJ project model and OCWorkspace stays in sync with it
 */
fun collectCidrWorkspaces(project: Project): CidrWorkspacesSection {
  val workspaces = CidrWorkspaceManager.getInstance(project).workspaces
  val infos = workspaces.map { (workspace, state) ->
    WorkspaceInfo(
      className = workspace.javaClass.toString(),
      state = state,
      projectPath = "${workspace.projectPath}",
      contentRoot = "${workspace.contentRoot}",
      sections = if (state.exists) WorkspaceDescriptionProvider.describe(workspace) else null
    )
  }
  return CidrWorkspacesSection(infos)
}

fun collectOCWorkspaceEvents(project: Project): OCWorkspaceEventsSection {
  val enabled = Registry.get("cpp.diagnostics.track.events").asBoolean()
  val text = project.service<CdWorkspaceEvents>().getResult()
  return OCWorkspaceEventsSection(enabled = enabled, rawText = text)
}

