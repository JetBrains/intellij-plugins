package com.jetbrains.cidr.cpp.diagnostics

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.jetbrains.cidr.cpp.diagnostics.workspace.WorkspaceDescriptionProvider
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
        WorkspaceDescriptionProvider.describe(workspace, log)
      }
    }
  }
  return log.result
}

fun collectOCWorkspaceEvents(project: Project): String {
  return project.service<CdWorkspaceEvents>().getResult()
}

