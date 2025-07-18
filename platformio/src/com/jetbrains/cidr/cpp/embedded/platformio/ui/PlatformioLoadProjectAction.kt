package com.jetbrains.cidr.cpp.embedded.platformio.ui

import com.intellij.openapi.externalSystem.model.ProjectSystemId
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.jetbrains.cidr.cpp.embedded.platformio.PlatformioFileType
import com.jetbrains.cidr.cpp.embedded.platformio.project.ID
import com.jetbrains.cidr.cpp.embedded.platformio.project.PlatformioWorkspace
import com.jetbrains.cidr.cpp.external.system.actions.AbstractCLionExternalLoadProjectAction

class PlatformioLoadProjectAction() : AbstractCLionExternalLoadProjectAction() {
  override val systemId: ProjectSystemId = ID

  override fun performLink(project: Project, projectPath: VirtualFile) {
    PlatformioWorkspace.linkProject(project, projectPath)
  }

  override fun canLinkFile(file: VirtualFile): Boolean = PlatformioFileType.isFileOfType(file)
}