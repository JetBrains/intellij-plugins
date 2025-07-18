package com.jetbrains.cidr.cpp.embedded.platformio.ui

import com.intellij.openapi.components.service
import com.intellij.openapi.externalSystem.model.ProjectSystemId
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.jetbrains.cidr.cpp.embedded.platformio.PlatformioFileType
import com.jetbrains.cidr.cpp.embedded.platformio.project.ID
import com.jetbrains.cidr.cpp.embedded.platformio.project.PlatformioProjectSettings
import com.jetbrains.cidr.cpp.embedded.platformio.project.PlatformioWorkspace
import com.jetbrains.cidr.cpp.external.system.actions.AbstractCLionExternalLoadProjectAction
import com.jetbrains.cidr.cpp.external.system.linkExternalProject

class PlatformioLoadProjectAction() : AbstractCLionExternalLoadProjectAction() {
  override val systemId: ProjectSystemId = ID

  override fun performLink(project: Project, projectPath: VirtualFile) {
    val settings = PlatformioProjectSettings.default()
    settings.externalProjectPath = projectPath.path
    val workspace = project.service<PlatformioWorkspace>()
    linkExternalProject(project, ID, settings, workspace)
  }

  override fun canLinkFile(file: VirtualFile): Boolean = PlatformioFileType.isFileOfType(file)
}